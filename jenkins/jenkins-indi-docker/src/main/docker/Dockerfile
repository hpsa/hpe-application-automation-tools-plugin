FROM jenkins:1.609.1
MAINTAINER Noam Kachko <noam.kachko@hpe.com>


COPY jenkins/ /usr/share/jenkins/ref

RUN ls -la /usr/share/jenkins/ref


EXPOSE 8080

#docker run -d  -p 8080:8080 --name=jenkins -v /home/sources/jenkins:/var/jenkins_home -e "http_proxy=http://web-proxy.bbn.hpecorp.net:8080" -e "https_proxy=http://web-proxy.bbn.hpecorp.net:8080" -e "no_proxy=127.0.0.1,localhost,mydyumserver,mydyumserver.hpswlabs.adapps.hp.com,.hp.com,16.59.0.0,hpswlabs.adapps.hp.com:80,hpeswlab.net" 16.60.141.93:5000/jenkins-nga:12.53.5-SNAPSHOT
#https://hub.docker.com/_/jenkins/ see guidlines of extending jenkins image