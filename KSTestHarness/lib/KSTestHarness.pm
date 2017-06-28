package KSTestHarness;

# Copyright 2017 KohaSuomi
#
# This file is part of Koha-Ansible-Pipeline.
#

=NAME

KSTestHarness

=SYNOPSIS

Runs given test files and generates clover and junit test reports to the given directory.

Automatically sorts given test files by directory and deduplicates them.

=cut

use Modern::Perl;
use Carp;
use autodie;
$Carp::Verbose = 'true'; #die with stack trace
use English; #Use verbose alternatives for perl's strange $0 and $\ etc.
use Getopt::Long qw(:config no_ignore_case);
use Try::Tiny;
use Scalar::Util qw(blessed);
use Cwd;

use IPC::Cmd;
use File::Basename;
use TAP::Harness::JUnit;
use Params::Validate qw(:all);

=head2 new

@PARAMS HashRef: {
          resultsDir =>
          tar        =>
          clover     =>
          testFiles  =>
          dryRun     =>
          verbose    =>
        }

=cut

my $validationTestFilesCallbacks = {
  'files exist' => sub {
    die "not an array" unless (ref($_[0]) eq 'ARRAY');
    die "is empty" unless (scalar(@{$_[0]}));

    my @errors;
    foreach my $file (@{$_[0]}) {
      push(@errors, "$file is not readable") unless (-r $file);
    }
    return 1 unless @errors;
    die "files are not readable:\n".join("\n",@errors);
  },
};
my $validationNew = {
  resultsDir => {
    callbacks => {
      'resultsDir is writable' => sub {
        if ($_[0]) {
          return (-w $_[0]);
        }
        else {
          return 1 if (-w File::Basename::dirname($0));
          die "No --results-dir was passed, so defaulting to the directory of the program used to call me '".File::Basename::dirname($0)."'. Unfortunately that directory is not writable by this process and I don't know where to save the test deliverables."
        }
      },
    },
  },
  tar => {default => 0},
  clover => {default => 0},
  dryRun => {default => 0},
  verbose => {default => 0},
  testFiles => {
    callbacks => $validationTestFilesCallbacks,
  },
};
sub new {
#  $validationTestFilesCallbacks->{$_}(['/tmp']) for (keys(%$validationTestFilesCallbacks));
  my $class = shift;
  my $params = validate(@_, $validationNew);

  my $self = {};
  bless($self, $class);
  $self->{_params} = $params;
  $self->setResultsDir( $params->{resultsDir} );
  $self->setTestFiles( $params->{testFiles} );
  return $self;
}

sub run {
  my ($self) = @_;

#  $self->changeWorkingDir();
  $self->prepareTestResultDirectories();
  $self->clearCoverDb() if $self->isClover();
  $self->runharness();
  $self->createCoverReport() if $self->isClover();
  $self->tar() if $self->isTar();
#  $self->revertWorkingDir();
}

=head2 changeWorkingDir

Change to the given --results-dir
or to the directory of the calling script.

=cut

sub changeWorkingDir {
  my ($self) = @_;

  $self->{oldWorkingDir} = Cwd::getcwd();
  chdir $self->{_params}->{resultsDir} || File::Basename::dirname($0);
}

sub revertWorkingDir {
  my ($self) = @_;

  die "\$self->{oldWorkingDir} is not known when reverting to the old working directory?? This should never happen!!" unless $self->{oldWorkingDir};
  chdir $self->{oldWorkingDir};
}

sub prepareTestResultDirectories {
  my ($self) = @_;
  $self->getTestResultFileAndDirectoryPaths($self->{resultsDir});
  mkdir $self->{testResultsDir} unless -d $self->{testResultsDir};
  $self->_shell("rm", "-r $self->{junitDir}")  if -e $self->{junitDir};
  $self->_shell("rm", "-r $self->{cloverDir}") if -e $self->{cloverDir};
  mkdir $self->{junitDir} unless -d $self->{junitDir};
  mkdir $self->{cloverDir} unless -d $self->{cloverDir};
  unlink $self->{testResultsArchive} if -e $self->{testResultsArchive};
}

=head2 getTestResultFileAndDirectoryPaths
@STATIC

Injects paths to the given HASHRef.

Used to share all relevant paths centrally with no need to duplicate

=cut

sub getTestResultFileAndDirectoryPaths {
  my ($hash, $resultsDir) = @_;
  $hash->{testResultsDir} = $resultsDir.'/testResults';
  $hash->{testResultsArchive} = 'testResults.tar.gz';
  $hash->{junitDir} =  $hash->{testResultsDir}.'/junit';
  $hash->{cloverDir} = $hash->{testResultsDir}.'/clover';
  $hash->{cover_dbDir} = $hash->{testResultsDir}.'/cover_db';
  $hash->{archivableDirs} = [$hash->{junitDir}, $hash->{cloverDir}];
}

=head2 clearCoverDb

Empty previous coverage test results

=cut

sub clearCoverDb {
  my ($self) = @_;
  $self->_shell('cover', "-delete $self->{cover_dbDir}");
}

=head2 createCoverReport

Create Clover coverage reports

=cut

sub createCoverReport {
  my ($self) = @_;
  $self->_shell('cover', "-report clover -outputdir $self->{cloverDir} $self->{cover_dbDir}");
}

=head2 tar

Create a tar.gz-package out of test deliverables

=cut

sub tar {
  my ($self) = @_;
  $self->_shell('tar', "-czf $self->{testResultsArchive} @{$self->{archivableDirs}}");
}

=head2 runharness

Runs all given test files

=cut

sub runharness {
  my ($self) = @_;
  my $filesByDir = $self->{testFilesByDir};

  foreach my $dir (sort keys %$filesByDir) {
      my @tests = sort @{$filesByDir->{$dir}};
      unless (scalar(@tests)) {
          carp "\@tests is empty?";
      }

      ##Prepare test harness params
      my $dirToPackage = $dir;
      $dirToPackage =~ s!^\./!!; #Drop leading "current"-dir chars
      $dirToPackage =~ s!/!\.!gsm; #Change directories to dot-separated packages
      my $xmlfile = $self->{testResultsDir}.'/junit'.'/'.$dirToPackage.'.xml';
      my @exec = (
          $EXECUTABLE_NAME,
          '-w',
      );
      push(@exec, "-MDevel::Cover=-db,$self->{cover_dbDir},-silent,1,-coverage,all") if $self->isClover();

      if ($self->{dryRun}) {
          print "TAP::Harness::JUnit would run tests with this config:\nxmlfile => $xmlfile\npackage => $dirToPackage\nexec => @exec\ntests => @tests\n";
      }
      else {
          my $harness = TAP::Harness::JUnit->new({
              xmlfile => $xmlfile,
#                package => $dirToPackage,
              package => "",
              verbosity => 1,
              namemangle => 'perl',
              exec       => \@exec,
          });
          $harness->runtests(@tests);
      }
  }
}

sub isClover {
  return shift->{_params}->{clover};
}
sub isTar {
  return shift->{_params}->{tar};
}
sub verbosity {
  return shift->{_params}->{verbose};
}

sub setResultsDir {
  my ($self, $resultsDir) = @_;

  $self->{resultsDir} = $self->{_params}->{resultsDir} || Cwd::getcwd();
}

sub setTestFiles {
  my ($self, $testFiles) = validate_pos(@_, {isa => __PACKAGE__}, {callbacks => $validationTestFilesCallbacks});

  $self->{testFilesByDir} = _sortFilesByDir($testFiles);
}
sub _sortFilesByDir {
    my ($files) = @_;
    unless (ref($files) eq 'ARRAY') {
        carp "\$files is not an ARRAYRef";
    }
    unless (scalar(@$files)) {
        carp "\$files is an ampty array?";
    }

    #deduplicate files
    my (%seen, @files);
    @files = grep !$seen{$_}++, @$files;

    #Sort by dirs
    my %dirsWithFiles;
    foreach my $f (@files) {
        my $dir = File::Basename::dirname($f);
        $dirsWithFiles{$dir} = [] unless $dirsWithFiles{$dir};
        push (@{$dirsWithFiles{$dir}}, $f);
    }
    return \%dirsWithFiles;
}

sub _shell {
  my ($self, $program, @params) = @_;
  my $programPath = IPC::Cmd::can_run($program) or die "$program is not installed!";
  my $cmd = "$programPath @params";

  if ($self->{dryRun}) {
    print "$cmd\n";
  }
  else {
    my( $success, $error_message, $full_buf, $stdout_buf, $stderr_buf ) =
        IPC::Cmd::run( command => $cmd, verbose => 0 );
    my $exitCode = ${^CHILD_ERROR_NATIVE} >> 8;
    my $killSignal = ${^CHILD_ERROR_NATIVE} & 127;
    my $coreDumpTriggered = ${^CHILD_ERROR_NATIVE} & 128;
    die "Shell command: $cmd\n  exited with code '$exitCode'. Killed by signal '$killSignal'.".(($coreDumpTriggered) ? ' Core dumped.' : '')."\nERROR MESSAGE: $error_message\nSTDOUT:\n@$stdout_buf\nSTDERR:\n@$stderr_buf\nCWD:".Cwd::getcwd()
        if $exitCode != 0;
    print "CMD: $cmd\nERROR MESSAGE: $error_message\nSTDOUT:\n@$stdout_buf\nSTDERR:\n@$stderr_buf\nCWD:".Cwd::getcwd() if $self->verbosity() > 0;
    return "@$full_buf";
  }
}

1;
