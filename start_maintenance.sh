#!/bin/bash

DIR=$(dirname "$(readlink -f "$BASH_SOURCE")")
MAIN="de.slux.line.friday.FridayBotApplication"
JAVA_OPTS=""
LINE_CHANNEL_TOKEN="ujk+j34cFZyA9qW0DYhKJ8P7+6FkmnEyWbcu7xhUmwhwVanjpjalsUfVgtHcrnXfhiAV2VIRIC0wAyYF7BBFvoKaenklkRi38/F+34qcyBXQVFMgTyrlmp8ILBLVgduDHvTehU9yf1RwOQ71cSBXhAdB04t89/1O/w1cDnyilFU="
LINE_CHANNEL_SECRET="a3cabd40aa88ddcfdeaecfabcd43dd9a"
FRIDAY_VERSION="0.2.0"
DB_USER="friday"
DB_PASSWORD="friday"
DB_HOST="localhost"
DB_PORT="3306"
DB_NAME="friday"


echo "*** Starting FRIDAY BOT ${FRIDAY_VERSION} ***"

java -Dline.bot.channelSecret="$LINE_CHANNEL_SECRET" \
     -Dline.bot.channelToken="$LINE_CHANNEL_TOKEN" \
     -Dfriday.db.user="$DB_USER" \
     -Dfriday.db.password="$DB_PASSWORD" \
     -Dfriday.db.host="$DB_HOST" \
     -Dfriday.db.port="$DB_PORT" \
     -Dfriday.db.name="$DB_NAME" \
     -Dfriday.maintenance=true \
     -Dfriday.scheduler.timefix=0 \
     $JAVA_OPTS \
     -cp "${DIR}/target/friday-bot-${FRIDAY_VERSION}.jar:${DIR}/target/libs/*" ${MAIN} > /dev/null 2>&1 &

# get the app pid
rm -f ${DIR}/FRIDAY.pid
echo $! > "${DIR}/FRIDAY.pid"

echo "*** FRIDAY BOT ${FRIDAY_VERSION} started with PID `cat ${DIR}/FRIDAY.pid` ***"



