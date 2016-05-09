FROM jenkins:1.609.1
MAINTAINER Noam Kachko <noam.kachko@hpe.com>

#Folders
ENV jenkins_home=./target/jenkins/ plugin_dir=${jenkins_home}plugins tools_dir=${jenkins_home}tools jobs_dir=${jenkins_home}jobs  user_contnent_dir=${jenkins_home}userContent nexus_plugin=http://mydtbld0051.hpeswlab.net:8081/nexus/service/local/repositories/devops-releases/content/com/hp/docker/jenkins/plugins

RUN mkdir -p {$jenkins_home, $plugin_dir, $tools_dir, $jobs_dir,$user_contnent_dir}

RUN  $jenkins_home

COPY mqm-onprem.rpm *.sh DBSetup.xml $work_dir

RUN rpm -U $work_dir/mqm-onprem.rpm

RUN chmod  u+rwx $work_dir/*.sh

RUN $work_dir/configure.sh

VOLUME $config_dir $log_dir $repo_dir

CMD $work_dir/run.sh

EXPOSE 8080
