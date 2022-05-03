#!/bin/bash
sm --stop TAX_HISTORY_FRONTEND
sm --stop AGENT_ACCESS_CONTROL
sm --start AGENT_ACCESS_CONTROL -r 0.81.0
sbt run
