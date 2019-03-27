FROM ubuntu:18.04

# Install OpenJDK and SBT
RUN apt-get update
RUN apt-get install -y openjdk-8-jdk git gnupg

RUN echo "deb https://dl.bintray.com/sbt/debian /" | tee -a /etc/apt/sources.list.d/sbt.list && \
	apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823 && \
	apt-get update && apt-get install -y sbt

# Copy and compile account-service
COPY . /src/account-service/.
RUN cd /src/account-service && sbt assembly && \
		mkdir -p /apps/account-service && cp target/scala-2.12/account-service-assembly-*.jar /apps/account-service/. && \
		rm -rf /src/account-service

# Start service at init of container
EXPOSE 8080
ENTRYPOINT ["/bin/bash", "-c", "cd /apps/account-service && java -cp .:* in.varadharajan.accountservice.Main"]