#!/usr/bin/env bash

sbt clean scalafmtAll scalastyleAll compile coverage test coverageOff coverageReport A11y/test dependencyUpdates
