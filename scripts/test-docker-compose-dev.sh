#!/usr/bin/env bash
# shellcheck disable=SC2016
# 本脚本多处断言需要匹配配置文件中的字面 ${...} 占位符。
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

assert_service_not_contains() {
  local service="$1"
  local unexpected="$2"

  if awk -v service="$service" -v unexpected="$unexpected" '
    $0 == "  " service ":" {
      in_service = 1
      next
    }
    in_service && $0 ~ /^  [A-Za-z0-9_-]+:/ {
      exit found ? 0 : 1
    }
    in_service && index($0, unexpected) {
      found = 1
      exit 0
    }
    END {
      if (!found) {
        exit 1
      }
    }
  ' "$compose_file"; then
    fail "expected service $service not to contain: $unexpected"
  fi
}

assert_env_port() {
  local env_name="$1"
  grep -Eq "^${env_name}=" "$env_example" || fail "expected env port variable: $env_name"
}

assert_env_active_not_contains() {
  local unexpected="$1"

  # 只检查真正生效的 env 行，注释里的历史说明不参与本地安全门判断。
  if grep -Eq "^[A-Z0-9_]+=.*${unexpected}" "$env_example"; then
    fail "expected active local env values not to contain: $unexpected"
  fi
}

assert_env_not_active() {
  local env_name="$1"

  if grep -Eq "^${env_name}=" "$env_example"; then
    fail "expected third-party env variable to stay commented in local example: $env_name"
  fi
}

assert_local_endpoint_host() {
  local env_name="$1"
  local endpoint_host="$2"

  endpoint_host="${endpoint_host%%/*}"
  endpoint_host="${endpoint_host%%:*}"
  endpoint_host="${endpoint_host//$'\r'/}"
  endpoint_host="${endpoint_host//$'\n'/}"
  endpoint_host="${endpoint_host//$'\t'/}"
  endpoint_host="${endpoint_host// /}"

  case "$endpoint_host" in
    ""|localhost|127.0.0.1|smart-*)
      return 0
      ;;
    *)
      fail "expected $env_name to point to local compose service or mock, got host: $endpoint_host"
      ;;
  esac
}

assert_local_env_endpoints() {
  local env_name
  local env_value
  local endpoint_host
  local endpoint_item
  local -a endpoint_items
  local without_scheme

  while IFS='=' read -r env_name env_value; do
    [[ -n "$env_name" ]] || continue

    endpoint_host=""
    case "$env_value" in
      jdbc:oracle:thin:@//*)
        endpoint_host="${env_value#jdbc:oracle:thin:@//}"
        ;;
      jdbc:oracle:thin:@*)
        endpoint_host="${env_value#jdbc:oracle:thin:@}"
        ;;
      jdbc:sqlserver://*)
        endpoint_host="${env_value#jdbc:sqlserver://}"
        ;;
      *://*)
        without_scheme="${env_value#*://}"
        endpoint_host="$without_scheme"
        ;;
    esac

    if [[ -n "$endpoint_host" ]]; then
      assert_local_endpoint_host "$env_name" "$endpoint_host"
    fi

    case "$env_name" in
      *_HOST|*_QUORUM|*_BOOTSTRAP_SERVERS|*_URL|*_URI|*_BASEURL|*_FACEURL|*_ENDPOINT|*_DOMAIN)
        [[ -z "$env_value" ]] && continue
        [[ "$env_value" == *://* || "$env_value" == jdbc:* ]] && continue
        endpoint_items=()
        IFS=',' read -ra endpoint_items <<<"$env_value"
        for endpoint_item in "${endpoint_items[@]}"; do
          assert_local_endpoint_host "$env_name" "$endpoint_item"
        done
        ;;
    esac
  done < <(grep -E '^[A-Z0-9_]+=' "$env_example")
}

assert_image_manifest() {
  local image_name="$1"
  local timeout_seconds="${SMART_IMAGE_MANIFEST_TIMEOUT_SECONDS:-30}"
  local manifest_pid
  local manifest_log
  local manifest_error
  local waited_seconds=0

  # 本地开发环境依赖可公开拉取的基础镜像；提前检查 manifest，避免 up 阶段才发现 tag 已失效。
  manifest_log="${TMPDIR:-/tmp}/smart-image-manifest-$$.log"
  docker manifest inspect "$image_name" >"$manifest_log" 2>&1 &
  manifest_pid=$!

  while kill -0 "$manifest_pid" 2>/dev/null; do
    if (( waited_seconds >= timeout_seconds )); then
      kill "$manifest_pid" 2>/dev/null || true
      wait "$manifest_pid" 2>/dev/null || true
      rm -f "$manifest_log"
      fail "timed out checking docker image manifest after ${timeout_seconds}s: $image_name"
    fi
    sleep 1
    waited_seconds=$((waited_seconds + 1))
  done

  if ! wait "$manifest_pid"; then
    manifest_error="$(tr '\n' ' ' <"$manifest_log" | sed 's/[[:space:]]\{1,\}/ /g')"
    rm -f "$manifest_log"
    fail "expected pullable docker image manifest: $image_name; docker said: $manifest_error"
  fi

  rm -f "$manifest_log"
}

assert_manifest_images_from_stdin() {
  local image_name

  while IFS= read -r image_name; do
    [[ -n "$image_name" ]] || continue
    [[ "$image_name" == smart/* ]] && continue
    assert_image_manifest "$image_name"
  done
}

assert_dockerfile_base_image_manifests() {
  local dockerfile_path

  for dockerfile_path in "$@"; do
    awk '$1 == "FROM" { print $2 }' "$dockerfile_path"
  done | sed 's/@.*//' | sort -u | assert_manifest_images_from_stdin
}

assert_profile_has_service() {
  local profile_name="$1"
  local expected_service="$2"
  local profile_services

  profile_services="$(
    SMART_DOCKER_ENV_FILE=docker/.env.local.example \
      docker compose --env-file docker/.env.local.example \
      -f docker-compose.dev.yml \
      --profile "$profile_name" \
      config --services
  )"

  grep -Fxq "$expected_service" <<<"$profile_services" || fail "expected profile $profile_name to include service: $expected_service"
}

assert_profile_lacks_service() {
  local profile_name="$1"
  local unexpected_service="$2"
  local profile_services

  profile_services="$(
    SMART_DOCKER_ENV_FILE=docker/.env.local.example \
      docker compose --env-file docker/.env.local.example \
      -f docker-compose.dev.yml \
      --profile "$profile_name" \
      config --services
  )"

  if grep -Fxq "$unexpected_service" <<<"$profile_services"; then
    fail "expected profile $profile_name not to include service: $unexpected_service"
  fi
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
module_dockerfiles=()
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
    module_dockerfiles+=("$repo_root/$dockerfile")
  fi
  if [[ "$module_name" != "nacos" && "$image_name" != smart/* ]]; then
    fail "expected business module image to use smart/*: $module_name -> $image_name"
  fi
  assert_contains "$compose_file" "$image_name"
done

assert_env_port "SMART_BRIDGE_CONCENTRATOR_NETTY_HOST_PORT"
assert_env_port "SMART_ORACLE_HOST_PORT"
assert_env_port "SMART_MOCK_HTTP_HOST_PORT"
assert_contains "$env_example" "SMART_JAVA_TOOL_OPTIONS="
assert_contains "$env_example" "SMART_ZOOKEEPER_HEAP_OPTS="
assert_contains "$env_example" "SMART_KAFKA_HEAP_OPTS="
assert_env_active_not_contains "REAL_"
assert_env_active_not_contains "host\\.docker\\.internal"
assert_env_active_not_contains "smart-mssql"
assert_local_env_endpoints
assert_service "smart-oracle"
assert_service "smart-mock-http"
assert_contains "$compose_file" "gvenzl/oracle-xe:21-slim"
assert_not_contains "$compose_file" "mcr.microsoft.com/mssql/server"
assert_not_contains "$compose_file" "smart-mssql"
assert_not_contains "$env_example" "SMART_MSSQL"
assert_contains "$compose_file" "mockserver/mockserver:5.15.0"
assert_service_contains "smart-oracle" 'profiles: ["backend", "local-db", "bridge", "bridge-isc"]'
assert_service_contains "smart-oracle" "healthcheck:"
assert_service_contains "smart-oracle" "healthcheck.sh"
assert_service_contains "smart-zookeeper" 'KAFKA_HEAP_OPTS: ${SMART_ZOOKEEPER_HEAP_OPTS:-'
assert_service_contains "smart-zookeeper" 'profiles: ["backend", "bridge", "bridge-isc", "bridge-concentrator", "local-kafka"]'
assert_service_contains "smart-kafka" 'KAFKA_HEAP_OPTS: ${SMART_KAFKA_HEAP_OPTS:-'
assert_service_contains "smart-kafka" 'profiles: ["backend", "bridge", "bridge-isc", "bridge-concentrator", "local-kafka"]'
assert_service_contains "smart-kafka" '</dev/tcp/127.0.0.1/9092'
assert_service_not_contains "smart-kafka" "kafka-topics --bootstrap-server localhost:9092 --list"
assert_service_contains "smart-gateway" 'JAVA_TOOL_OPTIONS: ${SMART_JAVA_TOOL_OPTIONS:-'
assert_contains "$compose_file" 'profiles: ["bridge"]'
assert_contains "$compose_file" 'profiles: ["bridge-isc"]'
assert_contains "$compose_file" 'profiles: ["bridge-concentrator"]'
assert_contains "$compose_file" '${SMART_H5_HOST_PORT:-8081}:3000'

assert_service_contains "smart-gateway" 'profiles: ["backend", "frontend"]'
assert_service_contains "smart-platform" 'profiles: ["backend", "bridge", "bridge-isc"]'
assert_service_contains "smart-dispatcher" 'profiles: ["backend", "bridge", "bridge-isc"]'
assert_service_contains "smart-mock-http" 'profiles: ["bridge", "bridge-isc", "mock"]'
assert_service_contains "smart-platform" "smart-oracle:"
assert_service_not_contains "smart-platform" "smart-mssql:"
assert_service_contains "smart-dispatcher" "smart-oracle:"
assert_service_not_contains "smart-dispatcher" "smart-mssql:"
assert_service_contains "smart-schedule" "smart-oracle:"
assert_service_not_contains "smart-schedule" "smart-mssql:"
assert_service_contains "smart-bridge" 'profiles: ["bridge"]'
assert_service_contains "smart-bridge-isc" 'profiles: ["bridge-isc"]'
assert_service_contains "smart-bridge" "smart-mock-http:"
assert_service_contains "smart-bridge-isc" "smart-mock-http:"
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
assert_contains "$repo_root/docker/nacos/config/dev/smart-data.yml" 'driver-class-name: "${SMART_DATA_DB_DRIVER:oracle.jdbc.driver.OracleDriver}"'
assert_contains "$repo_root/docker/nacos/config/dev/smart-schedule.yml" 'driver-class-name: "${SMART_SCHEDULE_DB_DRIVER:oracle.jdbc.driver.OracleDriver}"'
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

third_party_db_envs=(
  SMART_ATTENDANCE_DB_DRIVER
  SMART_ATTENDANCE_DB_URL
  SMART_ATTENDANCE_DB_USERNAME
  SMART_ATTENDANCE_DB_PASSWORD
  SMART_BUSINESSTRIP_DB_DRIVER
  SMART_BUSINESSTRIP_DB_URL
  SMART_BUSINESSTRIP_DB_USERNAME
  SMART_BUSINESSTRIP_DB_PASSWORD
  SMART_EHRVIEW_DB_DRIVER
  SMART_EHRVIEW_DB_URL
  SMART_EHRVIEW_DB_USERNAME
  SMART_EHRVIEW_DB_PASSWORD
  SMART_DHRVIEW_DB_DRIVER
  SMART_DHRVIEW_DB_URL
  SMART_DHRVIEW_DB_USERNAME
  SMART_DHRVIEW_DB_PASSWORD
  SMART_TEMPORARY_DB_DRIVER
  SMART_TEMPORARY_DB_URL
  SMART_TEMPORARY_DB_USERNAME
  SMART_TEMPORARY_DB_PASSWORD
  SMART_GUARD_DB_DRIVER
  SMART_GUARD_DB_URL
  SMART_GUARD_DB_USERNAME
  SMART_GUARD_DB_PASSWORD
  SMART_XC_C6_DB_DRIVER
  SMART_XC_C6_DB_URL
  SMART_XC_C6_DB_USERNAME
  SMART_XC_C6_DB_PASSWORD
)

for third_party_db_env in "${third_party_db_envs[@]}"; do
  assert_env_not_active "$third_party_db_env"
done

if command -v docker >/dev/null 2>&1 && docker compose version >/dev/null 2>&1; then
  if [[ "${SMART_SKIP_IMAGE_MANIFEST_CHECK:-0}" != "1" ]]; then
    SMART_DOCKER_ENV_FILE=docker/.env.local.example \
      docker compose --env-file docker/.env.local.example \
      -f docker-compose.dev.yml \
      --profile backend \
      --profile bridge \
      --profile bridge-isc \
      --profile bridge-concentrator \
      --profile frontend \
      config --images | sort -u | assert_manifest_images_from_stdin

    assert_dockerfile_base_image_manifests "${module_dockerfiles[@]}"
  fi

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

  for profile in backend local-db bridge bridge-isc frontend; do
    assert_profile_lacks_service "$profile" "smart-mssql"
  done

  assert_profile_lacks_service "backend" "smart-mock-http"
  assert_profile_lacks_service "frontend" "smart-zookeeper"
  assert_profile_lacks_service "frontend" "smart-kafka"

  for profile in bridge bridge-isc; do
    assert_profile_has_service "$profile" "smart-oracle"
    assert_profile_has_service "$profile" "smart-mock-http"
  done
fi

echo "Docker compose dev module coverage checks passed."
