javac -cp "../logist/logist.jar:../logist/lib*.jar:." -d "bin"  src/dumbfuck_agent/*.java
status=$?
[ $status -eq 0 ] && echo "Build Successfull"; exit 0 || echo "Build Failed"; exit 1