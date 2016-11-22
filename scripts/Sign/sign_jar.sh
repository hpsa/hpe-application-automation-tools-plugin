#!/bin/bash

path_to_jar=$1
jar_file=$2
jar_full_name=${path_to_jar}/${jar_file}

echo red.mud-83 | kinit qcbuilder@EMEA.CPQCORP.NET
echo "Signing file: $jar_full_name"
java -jar /opt/HPCSS/SignHP/SignHP_v3_1/SignHPClient.jar -i ${jar_full_name} -o ${path_to_jar} -p AGN_JAR

echo "Verifying file signature"
if /usr/bin/jarsigner -verify ${jar_full_name} | grep 'jar verified.'
then
    echo "THE JAR ${jar_full_name} WAS SIGNED SUCCESSFULLY"
else
    echo THE BUILD FAILED BECAUSE JAR ${jar_full_name} IS UNSIGNED;
    exit 1
fi
exit 0
