#!/bin/sh
curpath=`dirname $0`
nohup ${curpath}/run_sitemonitor.sh $@ > /dev/null & 2>&1
