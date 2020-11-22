./build.sh
status=$?
echo $status
if test $status -eq 0
then
	echo "Running tests"; ./runSim.sh
else
	echo "Compilation failed, no test run"
fi