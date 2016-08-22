#!/bin/bash

for (( p = 9100; p <= 11999; p++ ))
do
        n=$(netstat -an | grep $p)
        if [ "$n" == "" ]
        then
                echo $p
                break
        fi
done
