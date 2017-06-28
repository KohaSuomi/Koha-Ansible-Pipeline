#!/usr/bin/env perl

use 5.22.0;

use Test::More;

ok(`scripts/example.pl --clover --tar --all`,
   "Example script executed successfully");

ok(-e 't/resultsDir',
   "Results dir created");

ok(-e 't/resultsDir/clover',
   "Clover dir created");

ok(-e 't/resultsDir/clover/clover.xml',
   "Clover report created");

ok(-e 't/resultsDir/junit',
   "Junit dir created");

ok(-e 't/resultsDir/junit/t.t.01-unit.xml',
   "Junit unit test result created");

ok(-e 't/resultsDir/junit/t.t.02-xt.xml',
   "Junit xt test result created");

ok(-e 't/resultsDir/junit/t.t.03-integration.xml',
   "Junit integration test result created");
