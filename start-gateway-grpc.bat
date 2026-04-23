@echo off
set ROOT=%~dp0
start "Gateway gRPC :8080" cmd /k "java -jar "%ROOT%paxos-gateway\target\paxos-gateway-1.0-SNAPSHOT-jar-with-dependencies.jar" --protocol=grpc --port=8080"
