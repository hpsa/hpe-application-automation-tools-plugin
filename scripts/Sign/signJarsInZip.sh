#!/bin/bash

zip_file=$1
jar_folder=$2

mkdir temp_folder
mkdir signed_folder
unzip $zip_file -d temp_folder
cp $zip_file UNSIGNED_$zip_file
cp /opt/HPCSS/SignHP/SignHP_v3_1/login.conf .

java -jar /opt/HPCSS/SignHP/SignHP_v3_1/SignHPUtility-1.4.02.jar -s "temp_folder/${jar_folder}/" -p AGN_JAR -i -v -u jarsigner -c 1 -o signed_folder -t 24 -f /opt/HPCSS/SignHP/SignHP_v3_1/sign.properties

if [[ $? -ne 0 ]]
then
  echo ERROR signing zip file content - Check the logs;
  exit 1;
fi;

cp signed_folder/*.jar temp_folder/${jar_folder}/
cd temp_folder
zip ../temp.zip -r *
cd ..
cp temp.zip $zip_file