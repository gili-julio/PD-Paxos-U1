@echo off
set ROOT=%~dp0
start "TCP Proposer :9001" cmd /k "java -jar "%ROOT%paxos-proposer\target\paxos-proposer-1.0-SNAPSHOT-jar-with-dependencies.jar" --protocol=tcp --port=9001 --gateway=localhost:8080"
start "TCP Proposer :9002" cmd /k "java -jar "%ROOT%paxos-proposer\target\paxos-proposer-1.0-SNAPSHOT-jar-with-dependencies.jar" --protocol=tcp --port=9002 --gateway=localhost:8080"
