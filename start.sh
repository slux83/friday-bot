#!/bin/bash

PORT=50331

java $JAVA_OPTS -cp "target/jarvis-bot-0.1.1-SNAPSHOT.jar:target/libs/*" de.slux.line.jarvis.JarvisBotApplication --server.port=$PORT

#target/jarvis-bot-0.0.1-SNAPSHOT.jar
#src/main/java/de/slux/line/jarvis/JarvisBotApplication.java
