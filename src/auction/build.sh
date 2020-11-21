cd "/Users/renard/Documents/etudes/EPFLMA1/IntelligentAgents/exercises/CS-430_5_Decentralized_Coordination/src/auction"
javac -cp "../logist/logist.jar:../logist/lib*.jar:." -d "bin"  src/agent/*.java
status=$?
[ $status -eq 0 ] && echo "Build Successfull"; exit 0 || echo "Build Failed"; exit 1