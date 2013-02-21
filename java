#!/bin/bash
#
# A simple wrapper script to launch JavaGateway with JMX instrumentation.
# Written by Andrey Shcheglov.
#
# See <https://github.com/unix-junkie/ccontrol-scripts>.
#
# $Id$
#
# vim:ft=sh
#

export JAVA_HOME='/Library/Java/JavaVirtualMachines/1.6.0_39-b04-443.jdk/Contents/Home'
export JRE_HOME="${JAVA_HOME}/jre"

LOGNAME=`id -un` # Default logname returned by USER or LOGNAME may be different from the effective uid
export HOME=`eval echo ~${LOGNAME}` # Getting the HOME of the effective uid
VM_ARGS="-d64 -server -Dfile.encoding=`locale charmap` -Dcom.sun.management.jmxremote"
GATEWAY_CLASS_NAME='com.intersys.gateway.JavaGateway'
if [ "$4" == "${GATEWAY_CLASS_NAME}" ] || (echo "$@" | grep "${GATEWAY_CLASS_NAME}" >/dev/null 2>&1)
then
	TCP_PORT=$5
	JMX_PORT=$((58700 + $(($TCP_PORT % 100))))
	VM_ARGS="${VM_ARGS} -Djava.awt.headless=true -Dcom.sun.management.jmxremote.port=${JMX_PORT} -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"
fi

exec "${JAVA_HOME}/bin/java" ${VM_ARGS} "$@"
