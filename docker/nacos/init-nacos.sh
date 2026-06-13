#!/bin/sh
set -eu

NACOS_SERVER="${NACOS_SERVER:-http://smart-nacos:8848}"
NACOS_NAMESPACE="${NACOS_NAMESPACE:-eda914a9-b100-427b-9d37-4d7da89b841f}"
NACOS_NAMESPACE_NAME="${NACOS_NAMESPACE_NAME:-smart-local-dev}"
NACOS_GROUP="${NACOS_GROUP:-dev}"
NACOS_CONFIG_DIR="${NACOS_CONFIG_DIR:-/nacos-config/dev}"

wait_for_nacos() {
  i=0
  while [ "$i" -lt 60 ]; do
    if curl -fsS "$NACOS_SERVER/nacos/v1/console/health/readiness" >/dev/null 2>&1; then
      return 0
    fi
    i=$((i + 1))
    sleep 2
  done
  echo "Nacos is not ready: $NACOS_SERVER" >&2
  return 1
}

create_namespace() {
  curl -fsS -X POST "$NACOS_SERVER/nacos/v1/console/namespaces" \
    --data-urlencode "customNamespaceId=$NACOS_NAMESPACE" \
    --data-urlencode "namespaceName=$NACOS_NAMESPACE_NAME" \
    --data-urlencode "namespaceDesc=YUTO Smart local Docker development" >/dev/null || true
}

publish_config() {
  file="$1"
  data_id="$(basename "$file")"
  echo "Publishing Nacos config: $data_id"
  curl -fsS -X POST "$NACOS_SERVER/nacos/v1/cs/configs" \
    --data-urlencode "tenant=$NACOS_NAMESPACE" \
    --data-urlencode "group=$NACOS_GROUP" \
    --data-urlencode "dataId=$data_id" \
    --data-urlencode "type=yaml" \
    --data-urlencode "content@$file" >/dev/null
}

wait_for_nacos
create_namespace

for file in "$NACOS_CONFIG_DIR"/*.yml; do
  [ -f "$file" ] || continue
  publish_config "$file"
done

echo "Nacos local config initialization complete."
