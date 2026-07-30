#!/usr/bin/env bash
set -euo pipefail

# 发布前只读校验：确认发布包中受控脚本与 Jar 的校验和，并确认目标服务目录已具备
# app.sh 与同版本 smart-jar。它不复制文件、不停止服务，也不修改任何生产状态。
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd -P)"
DEFAULT_ARTIFACT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd -P)"
ARTIFACT_ROOT="$DEFAULT_ARTIFACT_ROOT"
APP_ROOT=""
SELECTED_SERVICES=()
SELECTED_SERVICE_COUNT=0
CHECKSUM_JAR_PATHS=()
CHECKSUM_JAR_VALUES=()
CHECKSUM_JAR_COUNT=0

die() {
  echo "ERROR: $*" >&2
  exit 1
}

usage() {
  cat <<'EOF'
Usage: verify-release-runtime.sh --app-root DIR [--artifact-root DIR] [--service NAME ...]

Read-only release preflight. It verifies the artifact checksum manifest, the
co-located runtime controls, and that the selected packaged Jars are present
unchanged under DIR/smart-jar together with an executable DIR/app.sh. Repeat
--service using the service name from manifest.csv for a split deployment; if
omitted, all packaged Jars are required on the target node.
EOF
}

canonicalize_directory() {
  local path="$1"

  [ -d "$path" ] || die "Missing directory: $path"
  cd "$path" && pwd -P
}

sha256_for() {
  local file_path="$1"

  if command -v sha256sum >/dev/null 2>&1; then
    sha256sum "$file_path" | awk '{print $1}'
    return
  fi
  if command -v shasum >/dev/null 2>&1; then
    shasum -a 256 "$file_path" | awk '{print $1}'
    return
  fi
  die "Missing required command: sha256sum or shasum"
}

verify_file_checksum() {
  local expected_checksum="$1"
  local relative_path="$2"
  local target_path="$3"
  local actual_checksum

  [ -f "$target_path" ] || die "Missing file for $relative_path: $target_path"
  actual_checksum="$(sha256_for "$target_path")"
  [ "$actual_checksum" = "$expected_checksum" ] || die "Checksum mismatch for $relative_path: $target_path"
}

validate_manifest_path() {
  local relative_path="$1"

  case "$relative_path" in
    smart-jar/*|runtime/restartService.sh|runtime/verify-watchdog-stopped.sh|runtime/verify-release-runtime.sh) ;;
    *) die "Unexpected checksum manifest path: $relative_path" ;;
  esac
  case "$relative_path" in
    *".."*|/*) die "Unsafe checksum manifest path: $relative_path" ;;
  esac
}

verify_runtime_controls() {
  local runtime_script

  for runtime_script in restartService.sh verify-watchdog-stopped.sh verify-release-runtime.sh; do
    [ -x "$ARTIFACT_ROOT/runtime/$runtime_script" ] || die "Missing executable runtime control: $ARTIFACT_ROOT/runtime/$runtime_script"
  done
}

array_contains() {
  local needle="$1"
  shift
  local value

  for value in "$@"; do
    [ "$value" = "$needle" ] && return 0
  done
  return 1
}

checksum_for_packaged_jar() {
  local artifact_path="$1"
  local index

  for ((index = 0; index < CHECKSUM_JAR_COUNT; index++)); do
    if [ "${CHECKSUM_JAR_PATHS[$index]}" = "$artifact_path" ]; then
      printf '%s\n' "${CHECKSUM_JAR_VALUES[$index]}"
      return 0
    fi
  done
  return 1
}

verify_checksum_manifest() {
  local checksum_file="$ARTIFACT_ROOT/sha256sums.txt"
  local line
  local expected_checksum
  local relative_path
  local entry_count=0
  local restart_checksum_seen=0
  local watchdog_checksum_seen=0
  local preflight_checksum_seen=0
  local seen_paths="|"

  [ -f "$checksum_file" ] || die "Missing checksum manifest: $checksum_file"
  verify_runtime_controls
  CHECKSUM_JAR_PATHS=()
  CHECKSUM_JAR_VALUES=()
  CHECKSUM_JAR_COUNT=0

  while IFS= read -r line || [ -n "$line" ]; do
    [[ "$line" =~ ^([[:xdigit:]]{64})\ \ (.+)$ ]] || die "Invalid checksum manifest line: $line"
    expected_checksum="${BASH_REMATCH[1]}"
    relative_path="${BASH_REMATCH[2]}"
    validate_manifest_path "$relative_path"
    case "$seen_paths" in
      *"|$relative_path|"*) die "Duplicate checksum manifest path: $relative_path" ;;
    esac
    seen_paths="${seen_paths}${relative_path}|"
    verify_file_checksum "$expected_checksum" "$relative_path" "$ARTIFACT_ROOT/$relative_path"
    case "$relative_path" in
      smart-jar/*)
        CHECKSUM_JAR_PATHS+=("$relative_path")
        CHECKSUM_JAR_VALUES+=("$expected_checksum")
        CHECKSUM_JAR_COUNT=$((CHECKSUM_JAR_COUNT + 1))
        ;;
      runtime/restartService.sh) restart_checksum_seen=1 ;;
      runtime/verify-watchdog-stopped.sh) watchdog_checksum_seen=1 ;;
      runtime/verify-release-runtime.sh) preflight_checksum_seen=1 ;;
    esac
    entry_count=$((entry_count + 1))
  done <"$checksum_file"

  [ "$entry_count" -gt 0 ] || die "Checksum manifest is empty: $checksum_file"
  [ "$CHECKSUM_JAR_COUNT" -gt 0 ] || die "Missing packaged Jar checksum entries: $checksum_file"
  [ "$restart_checksum_seen" -eq 1 ] || die "Missing required checksum for runtime/restartService.sh"
  [ "$watchdog_checksum_seen" -eq 1 ] || die "Missing required checksum for runtime/verify-watchdog-stopped.sh"
  [ "$preflight_checksum_seen" -eq 1 ] || die "Missing required checksum for runtime/verify-release-runtime.sh"
}

verify_selected_installed_jars() {
  local manifest_csv="$ARTIFACT_ROOT/manifest.csv"
  local header
  local line
  local service
  local source_path
  local artifact_path
  local manifest_checksum
  local size_bytes
  local extra_field
  local expected_checksum
  local selected=0
  local requested_service
  local found_services="|"
  local -a manifest_artifacts=()
  local -a selected_artifacts=()
  local -a selected_checksums=()
  local index
  local manifest_artifact_count=0
  local selected_artifact_count=0

  [ -f "$manifest_csv" ] || die "Missing release manifest metadata: $manifest_csv"
  IFS= read -r header <"$manifest_csv" || die "Release manifest metadata is empty: $manifest_csv"
  [ "$header" = "service,source,artifact,sha256,size_bytes" ] || die "Invalid release manifest header: $header"

  while IFS= read -r line || [ -n "$line" ]; do
    IFS=, read -r service source_path artifact_path manifest_checksum size_bytes extra_field <<<"$line"
    [[ "$service" =~ ^[A-Za-z0-9._-]+$ ]] || die "Invalid release manifest service: $service"
    [ -n "$source_path" ] && [ -n "$artifact_path" ] && [ -n "$manifest_checksum" ] && [ -n "$size_bytes" ] && [ -z "${extra_field:-}" ] || die "Invalid release manifest line: $line"
    validate_manifest_path "$artifact_path"
    case "$artifact_path" in
      smart-jar/*) ;;
      *) die "Release manifest artifact is not a Jar: $artifact_path" ;;
    esac
    if [ "$manifest_artifact_count" -gt 0 ] && array_contains "$artifact_path" "${manifest_artifacts[@]}"; then
      die "Duplicate release manifest artifact: $artifact_path"
    fi
    manifest_artifacts+=("$artifact_path")
    manifest_artifact_count=$((manifest_artifact_count + 1))
    if expected_checksum="$(checksum_for_packaged_jar "$artifact_path")"; then
      :
    else
      die "Release manifest artifact is missing from checksums: $artifact_path"
    fi
    [ "$manifest_checksum" = "$expected_checksum" ] || die "Release manifest checksum disagrees for $artifact_path"

    if [ "$SELECTED_SERVICE_COUNT" -eq 0 ]; then
      selected=1
    else
      selected=0
      for requested_service in "${SELECTED_SERVICES[@]}"; do
        if [ "$service" = "$requested_service" ]; then
          selected=1
          found_services="${found_services}${requested_service}|"
        fi
      done
    fi
    if [ "$selected" -eq 1 ]; then
      selected_artifacts+=("$artifact_path")
      selected_checksums+=("$expected_checksum")
      selected_artifact_count=$((selected_artifact_count + 1))
    fi
  done < <(tail -n +2 "$manifest_csv")

  [ "$manifest_artifact_count" -gt 0 ] || die "Release manifest contains no Jar entries: $manifest_csv"
  for ((index = 0; index < CHECKSUM_JAR_COUNT; index++)); do
    artifact_path="${CHECKSUM_JAR_PATHS[$index]}"
    array_contains "$artifact_path" "${manifest_artifacts[@]}" || die "Packaged Jar is missing from release manifest: $artifact_path"
  done
  if [ "$SELECTED_SERVICE_COUNT" -gt 0 ]; then
    for requested_service in "${SELECTED_SERVICES[@]}"; do
      case "$found_services" in
        *"|$requested_service|"*) ;;
        *) die "Requested service is missing from release manifest: $requested_service" ;;
      esac
    done
  fi
  [ "$selected_artifact_count" -gt 0 ] || die "No Jars selected for target verification"
  for ((index = 0; index < selected_artifact_count; index++)); do
    verify_file_checksum "${selected_checksums[$index]}" "${selected_artifacts[$index]}" "$APP_ROOT/${selected_artifacts[$index]}"
  done
}

verify_release() {
  [ -x "$APP_ROOT/app.sh" ] || die "Missing executable app.sh: $APP_ROOT/app.sh"
  [ -d "$APP_ROOT/smart-jar" ] || die "Missing installed smart-jar directory: $APP_ROOT/smart-jar"
  verify_checksum_manifest
  verify_selected_installed_jars
  echo "release runtime preflight passed: artifact=$ARTIFACT_ROOT app-root=$APP_ROOT"
}

while [[ "$#" -gt 0 ]]; do
  case "$1" in
    --app-root)
      [[ "$#" -ge 2 ]] || die "Missing value for --app-root"
      APP_ROOT="$2"
      shift 2
      ;;
    --artifact-root)
      [[ "$#" -ge 2 ]] || die "Missing value for --artifact-root"
      ARTIFACT_ROOT="$2"
      shift 2
      ;;
    --service)
      [[ "$#" -ge 2 ]] || die "Missing value for --service"
      [[ "$2" =~ ^[A-Za-z0-9._-]+$ ]] || die "Invalid --service value: $2"
      SELECTED_SERVICES+=("$2")
      SELECTED_SERVICE_COUNT=$((SELECTED_SERVICE_COUNT + 1))
      shift 2
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      usage >&2
      exit 1
      ;;
  esac
done

[ -n "$APP_ROOT" ] || die "--app-root is required"
ARTIFACT_ROOT="$(canonicalize_directory "$ARTIFACT_ROOT")"
APP_ROOT="$(canonicalize_directory "$APP_ROOT")"
verify_release
