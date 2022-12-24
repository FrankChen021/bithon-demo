#!/bin/sh

/wait

APP_NAME=$1
APP_NS=$2
APP_JAR=$3

JAVA_OPTS="-Dbithon.application.name=$APP_NAME -Dbithon.application.env=$APP_NS $JAVA_OPTS"

if [ -f /opt/agent-distribution/agent-main.jar ] ; then
  JAVA_OPTS="-javaagent:/opt/agent-distribution/agent-main.jar $JAVA_OPTS"
  echo "Starting application with agent: $JAVA_OPTS"
else
  echo "Starting application WITHOUT agent: $JAVA_OPTS"
fi

exec java ${JAVA_OPTS} -jar /opt/${APP_JAR} ${APP_OPTS}
