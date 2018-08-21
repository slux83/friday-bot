#!/bin/bash

PORT=50331
MAIN="de.slux.line.jarvis.JarvisBotApplication"
JAVA_OPTS=""

java $JAVA_OPTS -cp "target/jarvis-bot-0.0.1-SNAPSHOT.jar:target/libs/*" ${MAIN} --server.port=$PORT

#jarvis-bot-0.0.1-SNAPSHOT.jar
#src/main/java/de/slux/line/jarvis/JarvisBotApplication.java
