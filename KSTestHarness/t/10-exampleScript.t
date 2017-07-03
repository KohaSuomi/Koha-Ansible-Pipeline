#!/usr/bin/perl

#$ENV{TEST_VERBOSE} = 1;

use Modern::Perl;

use Test::More;

use Cwd;
use IPC::Cmd;
use File::Slurp;

use KSTestHarness;

my $testResultsDir = 't';
my $p = {};
KSTestHarness::getTestResultFileAndDirectoryPaths($p, $testResultsDir);

subtest "Execute example script", sub {
  my $cmd = "/usr/bin/perl -Ilib scripts/example.pl --clover --tar --all --junit --results-dir $testResultsDir";
  my( $success, $error_message, $full_buf, $stdout_buf, $stderr_buf ) =
          IPC::Cmd::run( command => $cmd, verbose => 0 );
  if ($ENV{TEST_VERBOSE}) {
    print "CMD: $cmd\nERROR MESSAGE: $error_message\nSTDOUT:\n@$stdout_buf\nSTDERR:\n@$stderr_buf\nCWD:".Cwd::getcwd();
  }


  if ($success) {
    ok($success,
       "Example script executed successfully");
  }
  else { #Trigger a failing test with description of the error
    is("ERROR MESSAGE: $error_message\nSTDOUT:\n@$stdout_buf\nSTDERR:\n@$stderr_buf\nCWD:".Cwd::getcwd(), undef,
       "Example script executed successfully");
    BAIL_OUT('Example script execution failed, so no point in verifying test results');
  }
};


subtest "Clover tests", sub {
  ok(-e $p->{cloverDir},
     "Clover dir created");

  ok(-e $p->{cloverDir}.'/clover.xml',
     "Clover report created");

  ok(my $contents = File::Slurp::read_file($p->{cloverDir}.'/clover.xml'),
     "Clover report slurped");

  like($contents, qr/<coverage generated="\d+" clover="\d+\.\d+"/,
     "Looks like a Clover xml-file");
};


subtest "Junit tests", sub {
  ok(-e 't/testResults/junit',
     "Junit dir created");

  ok(-e $p->{junitDir}.'/t.t.xml',
     "Junit unit test result created");

  ok(-e $p->{junitDir}.'/t.t.xt.xml',
     "Junit xt test result created");

  ok(-e $p->{junitDir}.'/t.t.integration.xml',
     "Junit integration test result created");

  ok(my $contents = File::Slurp::read_file($p->{junitDir}.'/t.t.integration.xml'),
     "Junit integration report slurped");

  like($contents, qr/\Q<testsuite name="t.t.integration.03-integration_t"\E/,
     "Looks like a Junit xml-file");
};

done_testing;
