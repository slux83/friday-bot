#!/bin/bash
PID_PATH=app.pid
appName="FRIDAY BOT"

if [ ! -f "$PID_PATH" ]; then
   echo "Process Id FilePath($PID_PATH) Not found"
else
    pid=`cat $PID_PATH`
    if [ ! -e "/proc/$pid" -a "/proc/$pid/exe" ]; then
        echo "$appName was not running.";
    else
       kill -2 $pid;
       echo "Gracefully stopping $appName with PID $pid..."

       # wait 30 secs for the shutdown then kill the app
       sleep 30
       kill -9 $pid
    fi
fi
