from tomcat:9-jre11 as base_app
RUN apt-get update
RUN apt-get upgrade -y
RUN apt-get install curl
RUN mkdir /opt/faction
#Fix issue with sending emails
RUN sed -i 's/^jdk.tls.disabledAlgorithms/# jdk.tls.disabledAlgorithms/' /opt/java/openjdk/conf/security/java.security
#Raise Tomcat's 2MB POST limit so large base64 image uploads work (25MB)
RUN sed -i 's/<Connector port="8080"/<Connector maxPostSize="26214400" port="8080"/' /usr/local/tomcat/conf/server.xml

              

#Remove this kruft
RUN rm -rf /usr/local/tomcat/webapps/manager
RUN rm -rf /usr/local/tomcat/webapps/host-manager
RUN rm -rf /usr/local/tomcat/webapps/docs
RUN rm -rf /usr/local/tomcat/webapps/examples
RUN rm -rf /usr/local/tomcat/webapps/ROOT

RUN wget https://github.com/factionsecurity/faction/releases/latest/download/faction.war -O /usr/local/tomcat/webapps/ROOT.war

#COPY ./target/faction.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080/tcp
CMD ["/usr/local/tomcat/bin/catalina.sh", "run"]
