#!/bin/bash

echo "Testing Jenkins2 Pipeline shared groovy libraries"
echo ""
echo "Starting"
echo ""

MODE=testing groovy -cp ./src:./src/groovy-json-2.4.1.jar test/test.groovy

