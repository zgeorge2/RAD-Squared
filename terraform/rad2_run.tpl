# Java run command for RAD2_node
java -jar ${rad2_jar_destination} ${rad2_java_opts_mem} ${rad2_java_opts_other} -javaagent:${aspectJ_jar_destination} -Dcom.sun.management.jmxremote.port=${jmxremote_port} -Dspring.profiles.active=${rad2_spring_opts_profile} ${rad2_spring_opts_other} --server.port=${server_port} --akka.conf=${akka_conf}
