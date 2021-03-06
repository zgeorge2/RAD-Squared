### Pre-requisites:
Download and install 
 * java 11 (Tested with openjdk version "11.0.5" 2019-10-15)
 * Maven 3.6.x (Tested with Apache Maven 3.6.3)
 * haproxy (Tested with HA-Proxy version 2.0.10 2019/11/25 - https://haproxy.org/)

### Build:
`$ mvn clean `

`$ mvn package`

### Starting the HAProxy
`$ haproxy -f rad2-sb/config/haproxy.conf`

### Starting the RAD^2 Cluster

 - To run a node (say, LAX)(For NYC or SEA, change the --akka.conf & --server.port accordingly). 
 - Select profile as basic or dev.

`$ cd <RAD^2 repo root dir>`

`$ java -jar ./rad2-sb/target/rad2-sb-1.0-SNAPSHOT.jar 
    -Dspring.profiles.active=basic -Xms512m -Xmx4096m -Djava.net.preferIPv4Stack=true
    -javaagent:~/.rad2/lib/aspectjweaver.jar -XX:+HeapDumpOnOutOfMemoryError
    -Dcom.sun.management.jmxremote.port=9081 -Dspring.output.ansi.enabled=always
    -Dspring.jmx.enabled=true -Dspring.liveBeansView.mbeanDomain
    -Dspring.application.admin.enabled=true -Dfile.encoding=UTF-8 com.rad2.sb.app.SBApplication
    --server.port=9080 --akka.conf=application_akka_LAX.conf`

##### Postman (or other http client) --
 - Default credentials - `admin/admin` for the `dev` profile
 - Default credentials - `basic/basic` for the `basic` profile
 - Content-Type - `application/json`

###### DBeaver (Community Edition) (or other Database Viewer)
Download & Install DBeaver from https://dbeaver.io/download/
For connection setup, search for "Apache Ignite" in the "All" category. 
Choose and add port 10800. You can leave the rest of the unfilled entries as-is.
Start at least one node of the RAD^2 Cluster. 
Now test the connection in DBeaver. 
It may fail because the right driver version isn't present. 
A prompt will come up to auto-download the  correct version. Click Ok 
Now re-test the connection. 


##### TERRAFORM/AWS CLUSTER DEPLOYMENT (WIP - DO NOT USE)
Specific instructions carried out are listed here. 
 - Step 0: Do a maven clean and package. This gets the jars ready. 
 - Step 1: Create an AWS Account; Download and install Terraform; add terraform to path. 
 - Step 2: Install or upgrade and then configure the AWS CLI (need at least the "default" profile)
 - Step 3: Run Terraform (dev setup only - 3 node cluster; see rad2_dev_vars.tf for values used)
 - Step 4: Sets up a 3 node cluster. Launch AWS EC2 dashboard to see public IPv4 addrs of Cluster

`$ cd <RAD^2 repo root dir>/terraform`

`$ terraform init`

`$ terraform plan`

`$ terraform apply`

When you are ready to get rid of the infrastructure created, use the following. 
There is no going back after this. 
`$ terraform destroy`
 

