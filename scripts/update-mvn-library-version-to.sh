#!/bin/sh

version=$1

mvn versions:set -DnewVersion=$version
mvn versions:commit

echo "NOTE!"
echo "Remember to also search for and replace the old version nr manually, for example in README.md"
