# Starting the HAProxy
$ haproxy -f rad2-sb/config/haproxy.conf

# Starting the SB:

# To run the LAX node (select profile as basic or dev)
# OLD: java -jar rad2-sb-1.0-SNAPSHOT.jar server --akka.conf=application_akka_LAX.conf --server.port=9080
# NEW:
$ java -Dspring.profiles.active=<basic|dev> -Xms512m -Xmx4096m -Djava.net.preferIPv4Stack=true \
    -javaagent:~/.rad2/lib/aspectjweaver.jar \
    -XX:+HeapDumpOnOutOfMemoryError -Dcom.sun.management.jmxremote.port=9081 \
    -Dspring.output.ansi.enabled=always -Dspring.jmx.enabled=true \
    -Dspring.liveBeansView.mbeanDomain -Dspring.application.admin.enabled=true \
    -Dfile.encoding=UTF-8 com.rad2.sb.app.SBApplication \
    --server.port=9080 --akka.conf=application_akka_LAX.conf

# To run the NYC node, just change the --akka.conf value to application_akka_NYC.conf and the port to
# 9090 (or other), etc.

# Postman (or other http client) Requests --
#  Creds - admin/admin for the dev profile
#  Creds - basic/basic for the basic profile
#  Content-Type - application/json

# DEPRECATED: To run Zipkin - zipkin jar in bundled in code-base.
$ java -jar zipkin.jar
# runs on Port:9411
