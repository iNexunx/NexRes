#!/usr/bin/env sh
APP_HOME=`dirname "$0"`
APP_HOME=`cd "$APP_HOME" && pwd`
DEFAULT_JVM_OPTS="-Xmx64m -Xms64m"
CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar
exec java $DEFAULT_JVM_OPTS -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
