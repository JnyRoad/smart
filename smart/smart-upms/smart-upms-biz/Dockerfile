FROM anapsix/alpine-java:8_server-jre_unlimited

MAINTAINER smart@bjtce.com

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime

RUN mkdir -p /smart-upms

WORKDIR /smart-upms

EXPOSE 4000

ADD ./smart-upms/smart-upms-biz/target/smart-upms-biz.jar ./

ARG APP_ENV

ENV APP_ENV $APP_ENV

CMD java -Dspring.profiles.active=$APP_ENV -Djava.security.egd=file:/dev/./urandom -jar smart-upms-biz.jar
