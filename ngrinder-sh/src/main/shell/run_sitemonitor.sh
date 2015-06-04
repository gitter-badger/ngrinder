#!/bin/sh
curpath=`dirname $0`
cd ${curpath}
java -server -cp "lib/ngrinder-core-NGRINDER-VERSION.jar:lib/*" org.ngrinder.NGrinderAgentStarter --mode=sitemonitor --command=run $@