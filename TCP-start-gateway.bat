@echo off
set ROOT=%~dp0
start "TCP Gateway :8080" cmd /k "java -jar "%ROOT%paxos-gateway\target\paxos-gateway-1.0-SNAPSHOT-jar-with-dependencies.jar" --protocol=tcp --port=8080"
