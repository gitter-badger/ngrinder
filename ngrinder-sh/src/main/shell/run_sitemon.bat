@ECHO OFF
SET basedir=%~dp0
CD %basedir%

java -server -cp "lib/ngrinder-core-NGRINDER-VERSION.jar;lib/*" org.ngrinder.NGrinderAgentStarter --mode sitemon --command run %*