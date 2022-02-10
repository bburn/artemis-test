FROM ubuntu:latest
LABEL maintainer="Bernard"

RUN mkdir /artemis
WORKDIR /artemis

RUN apt-get update
RUN apt-get -y install wget
RUN apt-get -y install openjdk-11-jdk
RUN java --version

RUN wget -O "artemis.tar" "https://www.apache.org/dyn/closer.cgi?filename=activemq/activemq-artemis/2.18.0/apache-artemis-2.18.0-bin.tar.gz&action=download"
RUN tar -xvf ./artemis.tar; \
ln -s /artemis/apache-artemis-2.18.0/ ./current

RUN /artemis/current/bin/artemis create --user admin --password admin --http-host 0.0.0.0 --require-login --relax-jolokia bernard

EXPOSE 8161 \
9404 \
61616 \
61617 \
5445 \
5672 \
1883 \
61613

ENTRYPOINT ["/artemis/bernard/bin/artemis", "run"]