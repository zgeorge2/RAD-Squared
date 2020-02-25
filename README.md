###Pre-requisites:
Download and install 
1. java 11 (Tested with openjdk version "11.0.5" 2019-10-15)
2. Maven 3.6.x (Tested with Apache Maven 3.6.3)
3. haproxy (Tested with HA-Proxy version 2.0.10 2019/11/25 - https://haproxy.org/)

### Build:
`$ mvn clean `

`$ mvn package`

### Starting the HAProxy
`$ haproxy -f rad2-sb/config/haproxy.conf`

### Starting the RAD^2 Cluster

##### To run a node (say, LAX)(For NYC or SEA, change the --akka.conf & --server.port accordingly). 
##### Select profile as basic or dev. 
`$ cd <RAD^2 repo root dir>`

`$ java -jar ./rad2-sb/target/rad2-sb-1.0-SNAPSHOT.jar 
    -Dspring.profiles.active=basic -Xms512m -Xmx4096m -Djava.net.preferIPv4Stack=true
    -javaagent:~/.rad2/lib/aspectjweaver.jar -XX:+HeapDumpOnOutOfMemoryError
    -Dcom.sun.management.jmxremote.port=9081 -Dspring.output.ansi.enabled=always
    -Dspring.jmx.enabled=true -Dspring.liveBeansView.mbeanDomain
    -Dspring.application.admin.enabled=true -Dfile.encoding=UTF-8 com.rad2.sb.app.SBApplication
    --server.port=9080 --akka.conf=application_akka_LAX.conf`

##### Postman (or other http client) --
######  Default credentials - admin/admin for the dev profile
######  Default credentials - basic/basic for the basic profile
###### Content-Type - application/json

 

