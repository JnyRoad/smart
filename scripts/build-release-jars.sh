#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<'EOF'
Usage: scripts/build-release-jars.sh [options]

Builds backend modules, validates deployable Spring Boot jars, and copies the
release jars into one local output directory for server upgrades.

Options:
  -o, --output DIR      Output directory. Default:
                        release-artifacts/backend/<timestamp>
  -m, --manifest FILE   Release jar manifest. Default:
                        scripts/release-jars.manifest
  --skip-build          Collect existing jars without running Maven.
  --force               Allow replacing an existing output directory.
  -h, --help            Show this help.

Environment:
  MAVEN_CMD             Maven executable. Default: mvn
  MAVEN_ARGS            Extra Maven arguments appended to each build command.

Generated files:
  smart-jar/            Copied release jars.
  manifest.csv          service, source, artifact, sha256, and size metadata.
  sha256sums.txt        Checksums for copied jars.
  build-info.txt        Git revision and build mode.
EOF
}

fail() {
  echo "$*" >&2
  exit 1
}

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    fail "Missing required command: $1"
  fi
}

canonicalize_path() {
  local path="$1"
  local existing_path="$path"
  local missing_suffix=""
  local canonical_existing

  while [[ ! -e "$existing_path" && "$existing_path" != "/" ]]; do
    missing_suffix="/$(basename "$existing_path")$missing_suffix"
    existing_path="$(dirname "$existing_path")"
  done

  [[ -e "$existing_path" ]] || fail "Cannot resolve path: $path"

  canonical_existing="$(cd "$existing_path" && pwd -P)"
  printf '%s%s\n' "$canonical_existing" "$missing_suffix"
}

repo_root="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"
repo_root="$(cd "$repo_root" && pwd -P)"
timestamp="$(date +%Y%m%d%H%M%S)"
manifest_file="$repo_root/scripts/release-jars.manifest"
output_dir="$repo_root/release-artifacts/backend/$timestamp"
skip_build=0
force_output=0

while [[ "$#" -gt 0 ]]; do
  case "$1" in
    -o|--output)
      [[ "$#" -ge 2 ]] || fail "Missing value for $1"
      output_dir="$2"
      shift 2
      ;;
    -m|--manifest)
      [[ "$#" -ge 2 ]] || fail "Missing value for $1"
      manifest_file="$2"
      shift 2
      ;;
    --skip-build)
      skip_build=1
      shift
      ;;
    --force)
      force_output=1
      shift
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      fail "Unknown option: $1"
      ;;
  esac
done

case "$manifest_file" in
  /*) ;;
  *) manifest_file="$repo_root/$manifest_file" ;;
esac

case "$output_dir" in
  /*) ;;
  *) output_dir="$repo_root/$output_dir" ;;
esac

output_dir="$(canonicalize_path "$output_dir")"
release_artifacts_root="$repo_root/release-artifacts"
staging_dir=""
jar_output_dir=""
manifest_csv=""
checksum_file=""
build_info_file=""
release_services=()
release_sources=()
release_artifacts=()

cleanup_staging_dir() {
  if [[ -n "${staging_dir:-}" && -e "$staging_dir" ]]; then
    rm -rf "$staging_dir"
  fi
}

trap cleanup_staging_dir EXIT

if [[ ! -f "$manifest_file" ]]; then
  fail "Release jar manifest not found: $manifest_file"
fi

check_output_target() {
  [[ "$output_dir" != "/" ]] || fail "Refusing unsafe output directory: $output_dir"
  [[ "$output_dir" != "$repo_root" ]] || fail "Refusing unsafe output directory: $output_dir"
  [[ "$output_dir" != "$release_artifacts_root" ]] || fail "Refusing unsafe output directory: $output_dir"

  if [[ "$output_dir" == "$repo_root"/* && "$output_dir" != "$release_artifacts_root"/* ]]; then
    fail "Output directory inside project must be under release-artifacts: $output_dir"
  fi

  if [[ -e "$output_dir" ]]; then
    if [[ "$force_output" -ne 1 ]]; then
      fail "Output directory already exists. Use --force to replace it: $output_dir"
    fi
    if [[ "$output_dir" != "$release_artifacts_root"/* ]]; then
      fail "Refusing --force outside release-artifacts: $output_dir"
    fi
  fi
}

prepare_output_dir() {
  local output_parent

  output_parent="$(dirname "$output_dir")"
  mkdir -p "$output_parent"

  staging_dir="$(mktemp -d "$output_parent/.build-release-jars.XXXXXX")"
  jar_output_dir="$staging_dir/smart-jar"
  manifest_csv="$staging_dir/manifest.csv"
  checksum_file="$staging_dir/sha256sums.txt"
  build_info_file="$staging_dir/build-info.txt"
  mkdir -p "$jar_output_dir"
}

finalize_output_dir() {
  if [[ -e "$output_dir" ]]; then
    rm -rf "$output_dir"
  fi

  mv "$staging_dir" "$output_dir"
  staging_dir=""
}

check_build_inputs() {
  if [[ "$skip_build" -eq 1 ]]; then
    return
  fi

  require_cmd "${MAVEN_CMD:-mvn}"

  [[ -f "$repo_root/smart/pom.xml" ]] || fail "Missing smart/pom.xml. Ensure the smart backend directory is present."
  [[ -f "$repo_root/smart-module/pom.xml" ]] || fail "Missing smart-module/pom.xml. Ensure the business module directory is present."
  [[ -f "$repo_root/smart-module/FileReceiver/pom.xml" ]] || fail "Missing smart-module/FileReceiver/pom.xml. Ensure the FileReceiver module directory is present."
}

run_maven_builds() {
  if [[ "$skip_build" -eq 1 ]]; then
    echo "Skipping Maven build; collecting existing jars."
    return
  fi

  echo "Building smart backend modules..."
  (
    cd "$repo_root/smart"
    "${MAVEN_CMD:-mvn}" clean install -DskipTests ${MAVEN_ARGS:-}
  )

  echo "Building smart-module backend modules..."
  (
    cd "$repo_root/smart-module"
    "${MAVEN_CMD:-mvn}" clean package -DskipTests ${MAVEN_ARGS:-}
  )

  echo "Building FileReceiver executable jar..."
  (
    cd "$repo_root/smart-module/FileReceiver"
    "${MAVEN_CMD:-mvn}" clean package -DskipTests ${MAVEN_ARGS:-}
  )
}

resolve_path() {
  local path="$1"

  case "$path" in
    /*) printf '%s\n' "$path" ;;
    *) printf '%s\n' "$repo_root/$path" ;;
  esac
}

validate_service_name() {
  local service="$1"

  if [[ ! "$service" =~ ^[A-Za-z0-9._-]+$ ]]; then
    fail "Invalid service name in manifest: $service"
  fi
}

validate_boot_jar() {
  local service="$1"
  local jar_path="$2"
  local manifest_text
  local required_entry

  manifest_text="$(unzip -p "$jar_path" META-INF/MANIFEST.MF 2>/dev/null || true)"
  if [[ -z "$manifest_text" ]]; then
    fail "Release jar for $service has no META-INF/MANIFEST.MF: $jar_path"
  fi

  if ! grep -Fq "Main-Class: org.springframework.boot.loader.JarLauncher" <<<"$manifest_text"; then
    fail "Release jar for $service is not a Spring Boot executable jar: $jar_path"
  fi

  for required_entry in BOOT-INF/classes/ BOOT-INF/lib/ org/springframework/boot/loader/JarLauncher.class; do
    if ! unzip -l "$jar_path" "$required_entry" >/dev/null 2>&1; then
      fail "Release jar for $service is missing Spring Boot jar entry $required_entry: $jar_path"
    fi
  done
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

  fail "Missing required command: sha256sum or shasum"
}

load_release_manifest() {
  require_cmd unzip

  local line
  local service
  local jar_path
  local extra_field
  local source_path
  local artifact_name
  local artifact_names_seen="|"

  release_services=()
  release_sources=()
  release_artifacts=()

  while IFS= read -r line || [[ -n "$line" ]]; do
    line="${line#"${line%%[![:space:]]*}"}"
    line="${line%"${line##*[![:space:]]}"}"

    [[ -z "$line" || "${line:0:1}" == "#" ]] && continue

    IFS='|' read -r service jar_path extra_field <<< "$line"
    [[ -n "${service:-}" && -n "${jar_path:-}" && -z "${extra_field:-}" ]] || fail "Invalid manifest line: $line"

    validate_service_name "$service"
    source_path="$(resolve_path "$jar_path")"
    [[ -f "$source_path" ]] || fail "Missing release jar for $service: $source_path"

    validate_boot_jar "$service" "$source_path"

    artifact_name="$(basename "$source_path")"
    case "$artifact_names_seen" in
      *"|$artifact_name|"*) fail "Duplicate release artifact name: $artifact_name" ;;
    esac
    artifact_names_seen="${artifact_names_seen}${artifact_name}|"

    release_services+=("$service")
    release_sources+=("$source_path")
    release_artifacts+=("$artifact_name")
  done < "$manifest_file"

  [[ "${#release_services[@]}" -gt 0 ]] || fail "No release jars were listed in $manifest_file"
}

write_build_info() {
  {
    if [[ "$skip_build" -eq 1 ]]; then
      echo "Build mode: skipped"
    else
      echo "Build mode: maven"
    fi
    echo "Generated at: $(date -u +%Y-%m-%dT%H:%M:%SZ)"
    echo "Repository root: $repo_root"
    echo "Manifest: $manifest_file"
    echo "Output: $output_dir"
    echo "Git SHA: $(git -C "$repo_root" rev-parse HEAD 2>/dev/null || echo unknown)"
    for project_dir in smart smart-module smart-ui smart-h5; do
      if [[ -e "$repo_root/$project_dir" ]]; then
        echo "$project_dir: present"
      else
        echo "$project_dir: missing"
      fi
    done
  } > "$build_info_file"
}

collect_release_jars() {
  printf 'service,source,artifact,sha256,size_bytes\n' > "$manifest_csv"
  : > "$checksum_file"

  local service
  local source_path
  local artifact_name
  local artifact_path
  local checksum
  local size_bytes
  local index

  for ((index = 0; index < ${#release_services[@]}; index++)); do
    service="${release_services[$index]}"
    source_path="${release_sources[$index]}"
    artifact_name="${release_artifacts[$index]}"
    artifact_path="$jar_output_dir/$artifact_name"
    cp "$source_path" "$artifact_path"

    checksum="$(sha256_for "$artifact_path")"
    size_bytes="$(wc -c < "$artifact_path" | tr -d '[:space:]')"
    printf '%s,%s,%s,%s,%s\n' "$service" "$source_path" "smart-jar/$artifact_name" "$checksum" "$size_bytes" >> "$manifest_csv"
    printf '%s  %s\n' "$checksum" "smart-jar/$artifact_name" >> "$checksum_file"
  done

  echo "Collected ${#release_services[@]} release jars into $output_dir"
}

main() {
  check_build_inputs
  check_output_target
  run_maven_builds
  load_release_manifest
  prepare_output_dir
  collect_release_jars
  write_build_info
  finalize_output_dir

  echo "Release artifact directory: $output_dir"
}

main "$@"
