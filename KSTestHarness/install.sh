#!/bin/bash

perl Build.PL
./Build
sudo ./Build installdeps
sudo ./Build install
sudo ./Build realclean

