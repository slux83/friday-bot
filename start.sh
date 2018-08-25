#!/bin/bash

MAIN="de.slux.line.jarvis.JarvisBotApplication"
JAVA_OPTS=""
LINE_CHANNEL_TOKEN="ujk+j34cFZyA9qW0DYhKJ8P7+6FkmnEyWbcu7xhUmwhwVanjpjalsUfVgtHcrnXfhiAV2VIRIC0wAyYF7BBFvoKaenklkRi38/F+34qcyBXQVFMgTyrlmp8ILBLVgduDHvTehU9yf1RwOQ71cSBXhAdB04t89/1O/w1cDnyilFU="
LINE_CHANNEL_SECRET="a3cabd40aa88ddcfdeaecfabcd43dd9a"
JARVIS_VERSION="0.0.1-beta1"

echo "*** Starting Jarvis BOT ${JARVIS_VERSION} ***"

java -Dline.bot.channelSecret="$LINE_CHANNEL_SECRET" \
	-Dline.bot.channelToken="$LINE_CHANNEL_TOKEN" $JAVA_OPTS \
	-cp "target/jarvis-bot-${JARVIS_VERSION}.jar:target/libs/*" ${MAIN} 



