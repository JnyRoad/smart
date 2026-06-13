FROM anapsix/alpine-java:8_server-jre_unlimited

MAINTAINER smart@bjtce.com

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime

RUN mkdir -p /smart-gateway

WORKDIR /smart-gateway

EXPOSE 9990

ADD ./smart-gateway/target/smart-gateway.jar ./

ARG APP_ENV

ENV APP_ENV $APP_ENV

CMD java -Dspring.profiles.active=$APP_ENV -Djava.security.egd=file:/dev/./urandom -jar smart-gateway.jar
