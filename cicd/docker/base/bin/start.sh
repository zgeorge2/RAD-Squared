#!/bin/sh -ex

PROG_NAME=`basename $0`
PROG_DIR=`dirname $0`
VAP_SAAS_SB_JAR=vap-saas-sb.jar
ASPECTJWEAVER_JAR=aspectjweaver.jar
VAP_CONFIG_DIR=$VAP_HOME/config

usage()
{
cat<<-EOF
Usage:
    $PROG_NAME
    $PROG_NAME <PORT>
    $PROG_NAME com.vmware.sb.app.SBApplication server --akka.conf=application_akka_vap_service.conf --server.port=9080
EOF
}

PORT=9080
if [ $# -eq 1 ]; then
    PORT=$1
fi
PARAMS='com.vmware.sb.app.SBApplication server --akka.conf=application_akka_vap_service.conf --server.port='
## Important:
##     Set default vap saas sb jar arguments
##     These arguments can be overwritten by passing new arguments on docker run command line
if [ $# -eq 0 -o $# -eq 1 ]; then
    set ${PARAMS}${PORT}
fi


# Be receptive to core dumps
ulimit -c unlimited

# Allow high connection count per process (raise file descriptor limit)
ulimit -n 65536


echo "$(date): starting jvm"

status=0
set +e
java ${JAVA_OPTS} -Djava.util.logging.config.file=$VAP_HOME/logging.config \
                  -javaagent:$VAP_HOME/lib/$ASPECTJWEAVER_JAR \
                  -Djava.net.preferIPv4Stack=true \
                  -Dspring.profiles.active=csp-prod \
                  -DconfigPath=$VAP_CONFIG_DIR \
                  -DSYMPHONY_DEPLOYMENT=local \
                  -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=$VAP_HOME/log/ \
                  -jar $VAP_HOME/lib/$VAP_SAAS_SB_JAR \
                  "${@}"

status=$?
set -e

echo "$(date): jvm exited with status code ${status}"
exit ${status}