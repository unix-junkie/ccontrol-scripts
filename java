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

# We shouldn't rely on the externally set JAVA_HOME, as this script may erroneously call itself.
# Instead, provide OS-specific Java detection code.
case "$(uname -s)" in
	'Darwin')
		java_version=$(basename $(ls -d1 /Library/Java/JavaVirtualMachines/1.6.0_*-b*-*.jdk | sort | tail -n1))
		export JAVA_HOME="/Library/Java/JavaVirtualMachines/${java_version}/Contents/Home"
		;;
	*)
		echo "$(uname -s) not yet supported."
		exit 1
		;;
esac
export JRE_HOME="${JAVA_HOME}/jre"

RUN_AS_USER='ashcheglov'

if [ "${RUN_AS_USER}" != `id -un` ]
then
	SUDO_REQUIRED=1	
else
	SUDO_REQUIRED=0
fi

if [ ${SUDO_REQUIRED} -eq 0 ]
then
	# Running as a restricted user
	LOGNAME=`id -un` # Default logname returned by USER or LOGNAME may be different from the effective uid
	export HOME=`eval echo ~${LOGNAME}` # Getting the HOME of the effective uid
fi

VM_ARGS="-d64 -server -Dfile.encoding=`locale charmap` -Dcom.sun.management.jmxremote"
GATEWAY_CLASS_NAME='com.intersys.gateway.JavaGateway'
if [ "$4" = "${GATEWAY_CLASS_NAME}" ] || (echo "$@" | grep "${GATEWAY_CLASS_NAME}" >/dev/null 2>&1)
then
	TCP_PORT=$5
	JMX_PORT=$((58700 + $(($TCP_PORT % 100))))
	VM_ARGS="${VM_ARGS} -Djava.awt.headless=true -Dcom.sun.management.jmxremote.port=${JMX_PORT} -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"
fi

if [ ${SUDO_REQUIRED} -eq 0 ]
then
	exec "${JAVA_HOME}/bin/java" ${VM_ARGS} "$@"
else
	# This requires the following line to be present in /etc/sudoers:
	# cacheusr        ALL=(ashcheglov) NOPASSWD: /Library/Java/JavaVirtualMachines/1.6.0_39-b04-443.jdk/Contents/Home/bin/java
	exec sudo -u ${RUN_AS_USER} "${JAVA_HOME}/bin/java" ${VM_ARGS} "$@"
fi
