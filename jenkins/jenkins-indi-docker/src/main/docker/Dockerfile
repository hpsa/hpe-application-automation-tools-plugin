FROM jenkins:1.609.1
MAINTAINER Noam Kachko <noam.kachko@hpe.com>

ENV workdir=/opt

WORKDIR $workdir

COPY jenkins/ $workdir/jenkins

RUN ls -la ${workdir}/jenkins


EXPOSE 8080
