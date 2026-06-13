FROM anapsix/alpine-java:8_server-jre_unlimited

MAINTAINER smart@bjtce.com

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime

RUN mkdir -p /smart-app

WORKDIR /smart-app

EXPOSE 6020

ADD ./smart-app/smart-app-biz/target/smart-app-biz.jar ./

ARG APP_ENV

ENV APP_ENV $APP_ENV

CMD java -Dspring.profiles.active=$APP_ENV -Djava.security.egd=file:/dev/./urandom -jar smart-app-biz.jar
