use Modern::Perl;
use utf8;
binmode STDOUT, ":utf8";
binmode STDIN, ":utf8";


use Module::Build;

my $class = Module::Build->subclass(
  class => "Module::Build::Custom",
  code => <<'SUBCLASS' );

sub ACTION_test {
  my $self = shift;

  $self->do_system( '/usr/bin/prove -Ilib t' );
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
    'IPC::Cmd'                          => '> 0.01',
    'Devel::Cover'                      => '> 0.01',
    'Devel::Cover::Report::Clover'      => '> 0.01',
    'Test::MockModule'                  => '> 0.01',
    'TAP::Harness::JUnit'               => '> 0.01',
    'Params::Validate'                  => '> 0.01',
  },
)->create_build_script;

