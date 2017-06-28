#!/bin/bash
#
# IN THIS FILE
#
# Execute the Pipeline code test suite with proper environemnt and deps
#
#


# groovy.json.JsonSlurperClassic is missing, so we include it
# PIPE_DEBUG = 2 or 1
PIPE_DEBUG=2 groovy -cp lib/groovy-json-2.4.1.jar testrun.groovy


