use Modern::Perl;
use utf8;
binmode STDOUT, ":utf8";
binmode STDIN, ":utf8";


use Module::Build;

my $class = Module::Build->subclass(
  class => "Module::Build::Custom",
  code => <<'SUBCLASS' );

use Cwd;

my $cwd = Cwd::getcwd();
my $scriptsInstallDir = '/usr/local/bin';
my $configInstallDir = '/etc/SQLAnon';

sub ACTION_install {
$DB::single=1;
  my $self = shift;
  $self->SUPER::ACTION_install;

  ###Install sqlanonymize to PATH
  require File::Copy;
  $self->log_info( "Copying the 'sqlanonymize'-program to \$PATH\n" );
  File::Copy::copy('scripts/sqlanonymize', "$scriptsInstallDir/sqlanonymize");
  $self->log_info( "Making 'sqlanonymize'-program executable\n" );
  chmod(0755, "$scriptsInstallDir/sqlanonymize");

  ###Install configuration files
  unless (-d $configInstallDir) {
    $self->log_info( "Creating '$configInstallDir'\n" );
    mkdir $configInstallDir;
  }
  unless (-e "$configInstallDir/SQLAnon.conf") {
    $self->log_info( "Creating '$configInstallDir/SQLAnon.conf'\n" );
    File::Copy::copy('config/SQLAnon.conf', "$configInstallDir/SQLAnon.conf");
    $self->_reconfigure("$configInstallDir/SQLAnon.conf", 'fakeNameListsDir.*$',       "fakeNameListsDir => '$cwd/fakeNameLists',");
    $self->_reconfigure("$configInstallDir/SQLAnon.conf", 'anonymizationRulesFile.*$', "anonymizationRulesFile => '$configInstallDir/anonymizations.csv',");
  }
  else {
    $self->log_info( "'$configInstallDir/SQLAnon.conf' exists\n" );
  }
  unless (-e "$configInstallDir/log4perl.conf") {
    $self->log_info( "Creating '$configInstallDir/log4perl.conf'\n" );
    File::Copy::copy('config/log4perl.conf', "$configInstallDir/log4perl.conf");
  }
  else {
    $self->log_info( "'$configInstallDir/log4perl.conf' exists\n" );
  }
  unless (-e "$configInstallDir/anonymizations.csv") {
    $self->log_info( "Creating '$configInstallDir/anonymizations.csv'\n" );
    File::Copy::copy('config/anonymizations.csv', "$configInstallDir/anonymizations.csv");
  }
  else {
    $self->log_info( "'$configInstallDir/anonymizations.csv' exists\n" );
  }
}

sub ACTION_test {
  my $self = shift;

  $self->do_system( '/usr/bin/prove -Ilib t' );
}

sub ACTION_commit {
  #This is just a useless reminder to extend more features
  my $self = shift;

  $self->depends_on("test");
  $self->log_warn( "#I am a useless commit action stub#" );
}


sub _reconfigure {
  my ($self, $file, $regexp, $replacement) = @_;
  #$regexp =      "\Q$regexp\E";
  $replacement = "\Q$replacement\E";
  $self->do_system("/bin/sed -i        \"s/$regexp/$replacement/\"                         $file");
}

SUBCLASS

$class->new(
  module_name => 'KSTestHarness',
  dist_author => 'kivilahtio',
  dist_abstract => 'Hide complexities of configuring Clover and Junit',

  build_requires => {
    'Modern::Perl',                     => '> 0.01',
  },
  requires => {
    'Test::MockModule'                  => '> 0.01',
    'TAP::Harness::JUnit'               => '> 0.01',
    'Params::Validate'                  => '> 0.01',
  },
)->create_build_script;

