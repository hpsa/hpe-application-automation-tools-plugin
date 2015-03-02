#!/bin/bash
for (( p = 8888; p <= 8999; p++ ))
do
        n=$(lsof -iTCP:$p)
        if [ "$n" == "" ]
        then
                export TESTING_SERVER_PORT=$p
                echo Selected port: $TESTING_SERVER_PORT
                break
        fi
done
