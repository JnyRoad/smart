#!/bin/sh
set -eu

NACOS_SERVER="${NACOS_SERVER:-http://smart-app-demo-nacos:8848}"
NACOS_NAMESPACE="${NACOS_NAMESPACE:-smart-app-demo}"
NACOS_GROUP="${NACOS_GROUP:-app-demo}"
NACOS_CONFIG_DIR="${NACOS_CONFIG_DIR:-/nacos-config}"

wait_for_nacos() {
  attempts=0
  while [ "$attempts" -lt 90 ]; do
    if curl -fsS "$NACOS_SERVER/nacos/v1/console/health/readiness" >/dev/null 2>&1; then
      return 0
    fi
    attempts=$((attempts + 1))
    sleep 2
  done
  echo "Nacos readiness check failed" >&2
  return 1
}

publish_config() {
  config_file="$1"
  curl -fsS -X POST "$NACOS_SERVER/nacos/v1/cs/configs" \
    --data-urlencode "tenant=$NACOS_NAMESPACE" \
    --data-urlencode "group=$NACOS_GROUP" \
    --data-urlencode "dataId=$(basename "$config_file")" \
    --data-urlencode "type=yaml" \
    --data-urlencode "content@$config_file" >/dev/null
}

wait_for_nacos
curl -fsS -X POST "$NACOS_SERVER/nacos/v1/console/namespaces" \
  --data-urlencode "customNamespaceId=$NACOS_NAMESPACE" \
  --data-urlencode "namespaceName=smart-app-demo" \
  --data-urlencode "namespaceDesc=isolated local Smart App demonstration" >/dev/null || true

for config_file in "$NACOS_CONFIG_DIR"/*.yml; do
  [ -f "$config_file" ] || continue
  publish_config "$config_file"
done

echo "Nacos App demonstration configuration is ready."
