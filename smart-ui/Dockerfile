FROM node:24-alpine AS build

WORKDIR /app

ENV PNPM_HOME=/pnpm
ENV PATH=$PNPM_HOME:$PATH

RUN corepack enable

COPY smart-ui/package.json smart-ui/pnpm-lock.yaml smart-ui/pnpm-workspace.yaml smart-ui/.npmrc ./
RUN pnpm install --frozen-lockfile

COPY smart-ui/ .
COPY smart-print-renderer/assets/fonts/ /smart-print-renderer/assets/fonts/

ARG VUE_APP_PLATFORM_URL=http://smart-gateway:9990
ARG VUE_APP_BASE_URL=http://smart-gateway:9990
ARG VUE_APP_SECURITY_ENCODE_KEY=

ENV VUE_APP_PLATFORM_URL=$VUE_APP_PLATFORM_URL
ENV VUE_APP_BASE_URL=$VUE_APP_BASE_URL
ENV VUE_APP_SECURITY_ENCODE_KEY=$VUE_APP_SECURITY_ENCODE_KEY

RUN pnpm build

FROM nginx:1.27-alpine

COPY smart-ui/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /data

EXPOSE 80
