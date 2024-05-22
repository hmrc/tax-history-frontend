#!/usr/bin/env bash

sbt clean scalafmtAll scalastyleAll compile coverage Test/test A11y/test coverageOff coverageReport dependencyUpdates
