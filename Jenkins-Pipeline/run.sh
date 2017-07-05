#!/bin/bash
#
# IN THIS FILE
#
# Execute the Pipeline code test suite with proper environemnt and deps
#
#


PROJECT=Hetula

PROJECT=$PROJECT MODE=testing groovy -cp ../lib/groovy-json-2.4.1.jar testrun.groovy


