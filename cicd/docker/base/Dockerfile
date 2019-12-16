FROM symphony-docker-local.jfrog.io/symphony/symphony-base:jre-8u162_gpg2030_photon1
ENV VAP_HOME=/opt/vmware/vap-saas
RUN mkdir -p $VAP_HOME
ADD base $VAP_HOME
COPY logging.config $VAP_HOME
WORKDIR $VAP_HOME
RUN mkdir hprof log config
RUN ["chmod", "+x", "bin/start.sh"]
ENTRYPOINT ["bin/start.sh"]
