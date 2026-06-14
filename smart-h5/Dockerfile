FROM node:22-alpine AS build

WORKDIR /app

ENV PNPM_HOME=/pnpm
ENV PATH=$PNPM_HOME:$PATH

RUN corepack enable

COPY package.json pnpm-lock.yaml pnpm-workspace.yaml ./
RUN pnpm install --frozen-lockfile

COPY . .

ARG API_PROXY_TARGET=http://smart-gateway:9990
ENV API_PROXY_TARGET=$API_PROXY_TARGET

RUN pnpm build

FROM node:22-alpine AS runner

WORKDIR /app

ENV NODE_ENV=production
ENV HOSTNAME=0.0.0.0
ENV PORT=3000

RUN addgroup -S nodejs && adduser -S nextjs -G nodejs

COPY --from=build --chown=nextjs:nodejs /app/.next/standalone ./

USER nextjs

EXPOSE 3000

CMD ["node", "server.js"]
