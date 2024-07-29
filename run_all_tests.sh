#!/usr/bin/env bash

sbt clean scalafmtAll compile coverage test coverageOff coverageReport A11y/test dependencyUpdates
