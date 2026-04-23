@echo off
set ROOT=%~dp0
start "TCP Acceptor :10001" cmd /k "java -jar "%ROOT%paxos-acceptor\target\paxos-acceptor-1.0-SNAPSHOT-jar-with-dependencies.jar" --protocol=tcp --port=10001 --gateway=localhost:8080"
start "TCP Acceptor :10002" cmd /k "java -jar "%ROOT%paxos-acceptor\target\paxos-acceptor-1.0-SNAPSHOT-jar-with-dependencies.jar" --protocol=tcp --port=10002 --gateway=localhost:8080"
start "TCP Acceptor :10003" cmd /k "java -jar "%ROOT%paxos-acceptor\target\paxos-acceptor-1.0-SNAPSHOT-jar-with-dependencies.jar" --protocol=tcp --port=10003 --gateway=localhost:8080"
