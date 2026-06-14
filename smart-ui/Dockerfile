FROM node:22-alpine AS build

WORKDIR /app

ENV PNPM_HOME=/pnpm
ENV PATH=$PNPM_HOME:$PATH

RUN corepack enable

COPY package.json pnpm-lock.yaml pnpm-workspace.yaml .npmrc ./
RUN pnpm install --frozen-lockfile

COPY . .

ARG VUE_APP_PLATFORM_URL=http://smart-gateway:9990
ARG VUE_APP_BASE_URL=http://smart-gateway:9990
ARG VUE_APP_SECURITY_ENCODE_KEY=

ENV VUE_APP_PLATFORM_URL=$VUE_APP_PLATFORM_URL
ENV VUE_APP_BASE_URL=$VUE_APP_BASE_URL
ENV VUE_APP_SECURITY_ENCODE_KEY=$VUE_APP_SECURITY_ENCODE_KEY

RUN pnpm build

FROM nginx:1.27-alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /data

EXPOSE 80
