FROM anapsix/alpine-java:8_server-jre_unlimited

MAINTAINER smart@bjtce.com

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime

RUN mkdir -p /smart-bridge-isc/database

WORKDIR /smart-bridge-isc

EXPOSE 6060

ADD ./smart-bridge-isc/smart-bridge-isc-biz/target/smart-bridge-isc-biz.jar ./

ARG APP_ENV

ENV APP_ENV $APP_ENV

CMD java -Dspring.profiles.active=$APP_ENV -Djava.security.egd=file:/dev/./urandom -jar smart-bridge-isc-biz.jar
