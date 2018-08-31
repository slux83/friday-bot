#!/bin/bash
DIR=$(dirname "$(readlink -f "$BASH_SOURCE")")
PID_PATH=${DIR}/FRIDAY.pid
APP_NAME="FRIDAY BOT"

if [ ! -f "$PID_PATH" ]; then
   echo "PID $PID_PATH Not found"
else
    PID="`cat $PID_PATH`"
    if [ "$PID" = "" ]; then
        echo "$APP_NAME is not running"
	exit 0
    fi
    if [ ! -e "/proc/$PID" -a "/proc/$PID/exe" ]; then
        echo "$APP_NAME was not running.";
    else
       # send SIGTERM
       kill -15 $PID;
       echo "Gracefully stopping $APP_NAME with PID ${PID}..."

       # wait 30 secs for the shutdown then kill the app
       sleep 10
       kill -9 $pid > /dev/null 2>&1
    fi
fi
