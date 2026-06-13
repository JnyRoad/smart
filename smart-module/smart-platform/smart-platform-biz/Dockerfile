FROM anapsix/alpine-java:8_server-jre_unlimited

MAINTAINER smart@bjtce.com

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime

RUN mkdir -p /smart-platform

WORKDIR /smart-platform

EXPOSE 6030

ADD ./smart-platform/smart-platform-biz/target/smart-platform-biz.jar ./

ARG APP_ENV

ENV APP_ENV $APP_ENV

CMD java -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -Dspring.profiles.active=$APP_ENV -Djava.security.egd=file:/dev/./urandom -jar smart-platform-biz.jar
