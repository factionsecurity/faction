from tomcat:9-jre11 as base_app
RUN apt-get update
RUN apt-get upgrade -y
RUN mkdir /opt/faction
#Fix issue with sending emails
RUN sed -i 's/^jdk.tls.disabledAlgorithms/# jdk.tls.disabledAlgorithms/' /opt/java/openjdk/conf/security/java.security

#Remove this kruft
RUN rm -rf /usr/local/tomcat/webapps/manager
RUN rm -rf /usr/local/tomcat/webapps/host-manager
RUN rm -rf /usr/local/tomcat/webapps/docs
RUN rm -rf /usr/local/tomcat/webapps/examples
RUN rm -rf /usr/local/tomcat/webapps/ROOT

COPY ./target/faction.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080/tcp
CMD ["/usr/local/tomcat/bin/catalina.sh", "run"]
