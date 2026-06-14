FROM anapsix/alpine-java:8_server-jre_unlimited

MAINTAINER smart@bjtce.com

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime

RUN mkdir -p /smart-bridge-concentrator

WORKDIR /smart-bridge-concentrator

EXPOSE 6062 6001

ADD ./smart-bridge-concentrator/smart-bridge-concentrator-biz/target/smart-bridge-concentrator-biz.jar ./

ARG APP_ENV

ENV APP_ENV $APP_ENV

CMD java -Dspring.profiles.active=$APP_ENV -Djava.security.egd=file:/dev/./urandom -jar smart-bridge-concentrator-biz.jar
