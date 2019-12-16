#!/bin/sh -e


AUTOMATION_HOME=/opt/automation

mkdir -p $AUTOMATION_HOME/log $AUTOMATION_HOME/hprof $AUTOMATION_HOME/sandbox || true

rm -fr $AUTOMATION_HOME/log/host.*.log.lck

# Be receptive to core dumps
ulimit -c unlimited

# Allow high connection count per process (raise file descriptor limit)
ulimit -n 65536

if [ "${DEFAULT_OPTS:-}" == "" ]; then
DEFAULT_OPTS="\
-server \
-showversion \
-XshowSettings \
-XX:+UseStringDeduplication \
-XX:-OmitStackTraceInFastThrow \
-XX:+UnlockExperimentalVMOptions \
-XX:+UnlockDiagnosticVMOptions \
-XX:+PrintCommandLineFlags \
-XX:+PrintFlagsFinal \
-XX:ErrorFile=$AUTOMATION_HOME/hprof/error_AUTOMATION_%p.log \
-XX:+HeapDumpOnOutOfMemoryError \
-XX:HeapDumpPath=$AUTOMATION_HOME/hprof \
-XX:+UseG1GC \
-XX:MaxGCPauseMillis=100 \
-XX:InitiatingHeapOccupancyPercent=60 \
-Dsun.io.useCanonCaches=false \
-Djava.awt.headless=true \
-Dfile.encoding=UTF-8 \
"
fi

IPV4_EXTERNAL=$(ip route get 8.8.8.8 | awk 'NR==1 {print $NF}')

if [ -z $BINDING_ADDRESS ]
then
  BINDING_ADDRESS="0.0.0.0"
fi
echo "Setting bind address to: $BINDING_ADDRESS"

if [ -z $BINDING_PORT ]
then
  BINDING_PORT=8000
fi

if [ -n "$BACKUP_PARAMS" ]
then
  echo "Setting backup params as System Properties"
  JAVA_OPTS="${JAVA_OPTS:-} $BACKUP_PARAMS"
fi

echo "Setting port to: $BINDING_PORT"

if [ -z $PUBLIC_URI ]
then
  PUBLIC_URI=http://$IPV4_EXTERNAL:$BINDING_PORT
fi
echo "Setting public URI to $PUBLIC_URI"

PUBLIC_HOST=`echo $PUBLIC_URI | sed 's/http:\/\///' | sed 's/:.*//'`

if [ -n "$JMX_PORT" ]
then
  echo "Enabling JMX connection on port: $JMX_PORT"
  JAVA_OPTS="${JAVA_OPTS:-} -Dcom.sun.management.jmxremote \
                            -Dcom.sun.management.jmxremote.port=$JMX_PORT \
                            -Dcom.sun.management.jmxremote.rmi.port=$JMX_PORT \
                            -Djava.rmi.server.hostname=127.0.0.1 \
                            -Dcom.sun.management.jmxremote.ssl=false \
                            -Dcom.sun.management.jmxremote.local.only=false \
                            -Dcom.sun.management.jmxremote.authenticate=false"
fi

if [ -n "$JVM_HEAP" ]
then
  echo "Setting heap to $JVM_HEAP"
  JAVA_OPTS="${JAVA_OPTS:-} -Xms$JVM_HEAP -Xmx$JVM_HEAP"
fi


if [ -n "$JVM_METASPACE" ]
then
  echo "Setting metaspace to $JVM_METASPACE"
  JAVA_OPTS="${JAVA_OPTS:-} -XX:MetaspaceSize=$JVM_METASPACE -XX:MaxMetaspaceSize=$JVM_METASPACE"
fi


if [ -n "$DEBUG_PORT" ]
then
  echo "Enabling debugging on port: $DEBUG_PORT"
  JAVA_OPTS="${JAVA_OPTS:-} -Xdebug -Xrunjdwp:transport=dt_socket,suspend=n,server=y,address=$DEBUG_PORT"
fi

# Only supply --peerNodes when we have one or more listed peers.
PEER_NODES_OPTION="${PEER_NODES:+--peerNodes=${PEER_NODES}}"
[ -n "$PEER_NODES_OPTION" ] && echo "Set PEER_NODES_OPTION to $PEER_NODES_OPTION"

CSP_OPTIONS=""
if [ -n "$CSP_CONFIG_FILE" ]
then
  echo "Configuring CSP with config file: $CSP_CONFIG_FILE"
  CSP_OPTIONS+="--cspConfigFile=$CSP_CONFIG_FILE "
fi

echo "$(date): starting jvm"

status=0
set +e
java ${JAVA_OPTS} -Djava.util.logging.config.file=$AUTOMATION_HOME/logging.config \
                  -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/var/log/ \
                  -jar $AUTOMATION_HOME/lib/automation-service.jar \
                  --isAuthorizationEnabled=true \
                  --port=$BINDING_PORT --bindAddress=$BINDING_ADDRESS \
                  --adminUser=automationserviceowner@vmware.com --adminUserPassword=VMware@123 \
                  --sandbox=$AUTOMATION_HOME/sandbox $CSP_OPTIONS $PEER_NODES_OPTION "$@"

status=$?
set -e

echo "$(date): jvm exited with status code ${status}"
exit ${status}

