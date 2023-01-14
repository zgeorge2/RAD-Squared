FROM openjdk:11
MAINTAINER https://github.com/zgeorge2/RAD-Squared
COPY ./rad2-sb/target/rad2-sb-1.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar",\
"-Dspring.profiles.active=basic",\
"-Xms512m",\
"-Xmx4096m ",\
"-Djava.net.preferIPv4Stack=true",\
"-javaagent:~/.rad2/lib/aspectjweaver.jar",\
"-XX:+HeapDumpOnOutOfMemoryError",\
"-Dcom.sun.management.jmxremote.port=9081",\
"-Dspring.output.ansi.enabled=always",\
"-Dspring.jmx.enabled=true ",\
"-Dspring.liveBeansView.mbeanDomain",\
"-Dspring.application.admin.enabled=true",\
"-Dfile.encoding=UTF-8 ",\
"com.rad2.sb.app.SBApplication",\
"--server.port=$SERVER_PORT",\
"--akka.conf=$AKKA_CONF"]