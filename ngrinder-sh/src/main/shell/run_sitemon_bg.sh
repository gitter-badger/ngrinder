#!/bin/sh
curpath=`dirname $0`
nohup ${curpath}/run_sitemon.sh $@ > /dev/null & 2>&1
