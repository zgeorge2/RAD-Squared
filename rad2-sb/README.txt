Starting the SB:

To run the LAX node:
java -jar rad2-sb-1.0-SNAPSHOT.jar server --akka.conf=application_akka_LAX.conf --server.port=9080
To run the NYC node, just change the --akka.conf value to application_akka_NYC.conf and the port to
9090 (or other), etc.

Requests -- 
Creds - admin/admin
Content-Type - application/json

Zipkin - zipkin jar in bundled in code-base.
Start the server - java -jar zipkin.jar
Port:9411
