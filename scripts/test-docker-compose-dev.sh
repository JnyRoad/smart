#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
compose_file="$repo_root/docker-compose.dev.yml"
env_example="$repo_root/docker/.env.local.example"
h5_next_config="$repo_root/smart-h5/next.config.ts"
h5_dockerfile="$repo_root/smart-h5/Dockerfile"
ui_dockerfile="$repo_root/smart-ui/Dockerfile"
ui_nginx_conf="$repo_root/smart-ui/nginx.conf"
bridge_isc_bootstrap="$repo_root/smart-module/smart-bridge-isc/smart-bridge-isc-biz/src/main/resources/bootstrap.yml"

fail() {
  echo "FAIL: $*" >&2
  exit 1
}

assert_file() {
  [[ -f "$1" ]] || fail "expected file $1"
}

assert_contains() {
  local file="$1"
  local expected="$2"
  grep -Fq "$expected" "$file" || fail "expected $file to contain: $expected"
}

assert_not_contains() {
  local file="$1"
  local unexpected="$2"
  if grep -Fq "$unexpected" "$file"; then
    fail "expected $file not to contain: $unexpected"
  fi
}

assert_service() {
  local service="$1"
  grep -Eq "^  ${service}:" "$compose_file" || fail "expected compose service: $service"
}

assert_service_uses_dockerfile() {
  local dockerfile="$1"
  local compose_dockerfile="$2"
  assert_file "$repo_root/$dockerfile"
  assert_contains "$compose_file" "dockerfile: $compose_dockerfile"
}

assert_service_contains() {
  local service="$1"
  local expected="$2"

  awk -v service="$service" -v expected="$expected" '
    $0 == "  " service ":" {
      in_service = 1
      next
    }
    in_service && $0 ~ /^  [A-Za-z0-9_-]+:/ {
      exit found ? 0 : 1
    }
    in_service && index($0, expected) {
      found = 1
      exit 0
    }
    END {
      if (!found) {
        exit 1
      }
    }
  ' "$compose_file" || fail "expected service $service to contain: $expected"
}

assert_env_port() {
  local env_name="$1"
  grep -Eq "^${env_name}=" "$env_example" || fail "expected env port variable: $env_name"
}

assert_file "$compose_file"
assert_file "$env_example"
assert_file "$h5_next_config"
assert_file "$h5_dockerfile"
assert_file "$ui_dockerfile"
assert_file "$ui_nginx_conf"
assert_file "$bridge_isc_bootstrap"

# This Compose coverage follows the requested container module list. FileReceiver
# remains a release artifact, but it is a Windows-side photo receiver and is not
# part of this Docker Compose module set.
#
# Module name | compose service | Dockerfile | compose Dockerfile | host port env var | image.
modules=(
  "nacos|smart-nacos|||NACOS_HOST_PORT|nacos/nacos-server:v1.4.6"
  "smart-ui|smart-ui|smart-ui/Dockerfile|Dockerfile|SMART_UI_HOST_PORT|smart/smart-ui:dev"
  "smart-h5|smart-h5|smart-h5/Dockerfile|Dockerfile|SMART_H5_HOST_PORT|smart/smart-h5:dev"
  "smart-algorithm-biz|smart-algorithm|smart-module/smart-algorithm/smart-algorithm-biz/Dockerfile|smart-algorithm/smart-algorithm-biz/Dockerfile|SMART_ALGORITHM_HOST_PORT|smart/smart-algorithm-biz:dev"
  "smart-app-biz|smart-app|smart-module/smart-app/smart-app-biz/Dockerfile|smart-app/smart-app-biz/Dockerfile|SMART_APP_HOST_PORT|smart/smart-app-biz:dev"
  "smart-auth|smart-auth|smart/smart-auth/Dockerfile|smart-auth/Dockerfile|SMART_AUTH_HOST_PORT|smart/smart-auth:dev"
  "smart-bridge-biz|smart-bridge|smart-module/smart-bridge/smart-bridge-biz/Dockerfile|smart-bridge/smart-bridge-biz/Dockerfile|SMART_BRIDGE_HOST_PORT|smart/smart-bridge-biz:dev"
  "smart-bridge-concentrator-biz|smart-bridge-concentrator|smart-module/smart-bridge-concentrator/smart-bridge-concentrator-biz/Dockerfile|smart-bridge-concentrator/smart-bridge-concentrator-biz/Dockerfile|SMART_BRIDGE_CONCENTRATOR_HOST_PORT|smart/smart-bridge-concentrator-biz:dev"
  "smart-bridge-isc-biz|smart-bridge-isc|smart-module/smart-bridge-isc/smart-bridge-isc-biz/Dockerfile|smart-bridge-isc/smart-bridge-isc-biz/Dockerfile|SMART_BRIDGE_ISC_HOST_PORT|smart/smart-bridge-isc-biz:dev"
  "smart-data-biz|smart-data|smart-module/smart-data/smart-data-biz/Dockerfile|smart-data/smart-data-biz/Dockerfile|SMART_DATA_HOST_PORT|smart/smart-data-biz:dev"
  "smart-dispatcher-biz|smart-dispatcher|smart-module/smart-dispatcher/smart-dispatcher-biz/Dockerfile|smart-dispatcher/smart-dispatcher-biz/Dockerfile|SMART_DISPATCHER_HOST_PORT|smart/smart-dispatcher-biz:dev"
  "smart-gateway|smart-gateway|smart/smart-gateway/Dockerfile|smart-gateway/Dockerfile|SMART_GATEWAY_HOST_PORT|smart/smart-gateway:dev"
  "smart-platform-biz|smart-platform|smart-module/smart-platform/smart-platform-biz/Dockerfile|smart-platform/smart-platform-biz/Dockerfile|SMART_PLATFORM_HOST_PORT|smart/smart-platform-biz:dev"
  "smart-push-biz|smart-push|smart-module/smart-push/smart-push-biz/Dockerfile|smart-push/smart-push-biz/Dockerfile|SMART_PUSH_HOST_PORT|smart/smart-push-biz:dev"
  "smart-schedule|smart-schedule|smart-module/smart-schedule/Dockerfile|smart-schedule/Dockerfile|SMART_SCHEDULE_HOST_PORT|smart/smart-schedule:dev"
  "smart-upms-biz|smart-upms|smart/smart-upms/smart-upms-biz/Dockerfile|smart-upms/smart-upms-biz/Dockerfile|SMART_UPMS_HOST_PORT|smart/smart-upms-biz:dev"
)

for module_entry in "${modules[@]}"; do
  IFS='|' read -r module_name service_name dockerfile compose_dockerfile env_port image_name <<<"$module_entry"
  assert_service "$service_name"
  assert_env_port "$env_port"
  if [[ -n "$dockerfile" ]]; then
    assert_service_uses_dockerfile "$dockerfile" "$compose_dockerfile"
  fi
  if [[ "$module_name" != "nacos" && "$image_name" != smart/* ]]; then
    fail "expected business module image to use smart/*: $module_name -> $image_name"
  fi
  assert_contains "$compose_file" "$image_name"
done

assert_env_port "SMART_BRIDGE_CONCENTRATOR_NETTY_HOST_PORT"
assert_contains "$compose_file" 'profiles: ["bridge"]'
assert_contains "$compose_file" 'profiles: ["bridge-isc"]'
assert_contains "$compose_file" 'profiles: ["bridge-concentrator"]'
assert_contains "$compose_file" '${SMART_H5_HOST_PORT:-8081}:3000'

assert_service_contains "smart-gateway" 'profiles: ["backend", "frontend"]'
assert_service_contains "smart-platform" 'profiles: ["backend", "bridge", "bridge-isc"]'
assert_service_contains "smart-dispatcher" 'profiles: ["backend", "bridge", "bridge-isc"]'
assert_service_contains "smart-bridge" 'profiles: ["bridge"]'
assert_service_contains "smart-bridge-isc" 'profiles: ["bridge-isc"]'
assert_service_contains "smart-bridge-concentrator" 'profiles: ["bridge-concentrator"]'
assert_service_contains "smart-bridge-concentrator" 'SMART_KAFKA_BRIDGE_EVENT_TOPIC: ${SMART_KAFKA_BRIDGE_EVENT_TOPIC:-smart-local-bridge-event}'
assert_service_contains "smart-h5" '${SMART_H5_HOST_PORT:-8081}:3000'
assert_service_contains "smart-h5" 'API_PROXY_TARGET: ${SMART_H5_API_PROXY_TARGET:-http://smart-gateway:9990}'
assert_service_contains "smart-ui" 'VUE_APP_PLATFORM_URL: ${VUE_APP_PLATFORM_URL:-http://smart-gateway:9990}'
assert_service_contains "smart-ui" 'VUE_APP_BASE_URL: ${VUE_APP_BASE_URL:-http://smart-gateway:9990}'

assert_contains "$env_example" "VUE_APP_PLATFORM_URL=http://smart-gateway:9990"
assert_contains "$env_example" "VUE_APP_BASE_URL=http://smart-gateway:9990"
assert_contains "$env_example" "SMART_H5_API_PROXY_TARGET=http://smart-gateway:9990"
assert_contains "$env_example" "SMART_BRIDGE_C_PROCESS_BRIDGE_URL=http://smart-bridge-concentrator:6062"
assert_not_contains "$env_example" "SMART_BRIDGE_C_PROCESS_BRIDGE_URL=http://smart-bridge-isc:6060"
assert_contains "$h5_next_config" "if (process.env.NODE_ENV !== 'development' && !configuredTarget) return []"
assert_contains "$h5_dockerfile" "ARG API_PROXY_TARGET=http://smart-gateway:9990"
assert_contains "$repo_root/smart-module/smart-bridge-concentrator/smart-bridge-concentrator-biz/src/main/resources/application.yml" 'bridge-event-topic: "${SMART_KAFKA_BRIDGE_EVENT_TOPIC:BRIDGE_EVENT_TOPIC}"'
assert_contains "$ui_dockerfile" "ARG VUE_APP_PLATFORM_URL=http://smart-gateway:9990"
assert_contains "$ui_dockerfile" "ARG VUE_APP_BASE_URL=http://smart-gateway:9990"
assert_contains "$ui_nginx_conf" "location ~* ^/(code|auth|admin|algorithm|file|push|app|platform|schedule|data|gen)"

for bridge_config in "$repo_root"/docker/nacos/config/dev/smart-bridge-biz-*.yml; do
  assert_contains "$bridge_config" 'application-name: smart-bridge-biz'
  assert_contains "$bridge_config" 'bridge-event-topic: "${SMART_KAFKA_BRIDGE_EVENT_TOPIC:smart-local-bridge-event}"'
  assert_contains "$bridge_config" 'bridge-event-group-id: "${SMART_KAFKA_BRIDGE_EVENT_GROUP_ID:smart-local-bridge-event-group}"'
done

assert_contains "$repo_root/docker/nacos/config/dev/smart-bridge-isc-biz-5000021.yml" 'application-name: smart-bridge-isc-biz'
assert_contains "$bridge_isc_bootstrap" 'data-id: smart-bridge-isc-biz-5000021.yml'
assert_not_contains "$bridge_isc_bootstrap" 'data-id: smart-bridge-biz-5000021.yml'

bridge_url_configs=(
  "$repo_root/docker/nacos/config/dev/smart-bridge-biz-10001.yml"
  "$repo_root/docker/nacos/config/dev/smart-bridge-biz-5000021.yml"
  "$repo_root/docker/nacos/config/dev/smart-bridge-isc-biz-5000021.yml"
)

for bridge_url_config in "${bridge_url_configs[@]}"; do
  assert_contains "$bridge_url_config" 'bridge-url: "${SMART_BRIDGE_C_PROCESS_BRIDGE_URL:http://smart-bridge-concentrator:6062}"'
  assert_not_contains "$bridge_url_config" 'bridge-url: "${SMART_BRIDGE_C_PROCESS_BRIDGE_URL:http://smart-bridge-isc:6060}"'
done

if command -v docker >/dev/null 2>&1 && docker compose version >/dev/null 2>&1; then
  SMART_DOCKER_ENV_FILE=docker/.env.local.example \
    docker compose --env-file docker/.env.local.example \
    -f docker-compose.dev.yml \
    --profile backend \
    --profile bridge \
    --profile bridge-isc \
    --profile bridge-concentrator \
    --profile frontend \
    config --quiet

  for profile in frontend bridge bridge-isc bridge-concentrator; do
    SMART_DOCKER_ENV_FILE=docker/.env.local.example \
      docker compose --env-file docker/.env.local.example \
      -f docker-compose.dev.yml \
      --profile "$profile" \
      config --quiet
  done
fi

echo "Docker compose dev module coverage checks passed."
