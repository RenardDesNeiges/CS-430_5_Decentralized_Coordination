cd "/Users/renard/Documents/etudes/EPFLMA1/IntelligentAgents/exercises/CS-430_5_Decentralized_Coordination/src/auction"
./build.sh
status=$?
echo $status
if test $status -eq 0
then
	echo "Running tests"; ./runSim.sh
else
	echo "Compilation failed, no test run"
fi