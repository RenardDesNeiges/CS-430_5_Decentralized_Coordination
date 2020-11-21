cd "/Users/renard/Documents/etudes/EPFLMA1/IntelligentAgents/exercises/CS-430_5_Decentralized_Coordination/src/auction"
status=$?
[ $status -eq 0 ] && echo "Running tests"; ./build.sh; ./runSim.sh || echo "No tests were run"; exit 1

