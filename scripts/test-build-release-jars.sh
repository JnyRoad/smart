#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
script_path="$repo_root/scripts/build-release-jars.sh"
default_manifest="$repo_root/scripts/release-jars.manifest"
test_dir=""

fail() {
  echo "FAIL: $*" >&2
  exit 1
}

assert_file() {
  [[ -f "$1" ]] || fail "expected file $1"
}

assert_not_file() {
  [[ ! -f "$1" ]] || fail "unexpected file $1"
}

assert_not_dir() {
  [[ ! -d "$1" ]] || fail "unexpected directory $1"
}

assert_contains() {
  local file="$1"
  local expected="$2"
  grep -Fq "$expected" "$file" || fail "expected $file to contain: $expected"
}

assert_line_before() {
  local file="$1"
  local earlier="$2"
  local later="$3"
  local earlier_line
  local later_line

  earlier_line="$( (grep -nF "$earlier" "$file" || true) | head -n 1 | cut -d: -f1)"
  later_line="$( (grep -nF "$later" "$file" || true) | head -n 1 | cut -d: -f1)"

  [[ -n "$earlier_line" ]] || fail "expected $file to contain: $earlier"
  [[ -n "$later_line" ]] || fail "expected $file to contain: $later"
  (( earlier_line < later_line )) || fail "expected Maven command before dependent build: $earlier"
}

make_jar_with_manifest() {
  local jar_path="$1"
  local manifest_body="$2"
  local jar_dir
  jar_dir="$(mktemp -d "${TMPDIR:-/tmp}/release-jar.XXXXXX")"

  mkdir -p "$jar_dir/META-INF"
  printf '%s\n' "$manifest_body" > "$jar_dir/META-INF/MANIFEST.MF"
  (cd "$jar_dir" && zip -q -r "$jar_path" META-INF)
  rm -rf "$jar_dir"
}

make_boot_jar() {
  local jar_path="$1"
  local jar_dir
  jar_dir="$(mktemp -d "${TMPDIR:-/tmp}/release-boot-jar.XXXXXX")"

  mkdir -p "$jar_dir/META-INF" "$jar_dir/BOOT-INF/classes" "$jar_dir/BOOT-INF/lib" "$jar_dir/org/springframework/boot/loader"
  printf '%s\n' $'Manifest-Version: 1.0\nMain-Class: org.springframework.boot.loader.JarLauncher\nStart-Class: com.tce.smart.GoodApplication\nSpring-Boot-Classes: BOOT-INF/classes/\nSpring-Boot-Lib: BOOT-INF/lib/' > "$jar_dir/META-INF/MANIFEST.MF"
  printf 'compiled classes placeholder\n' > "$jar_dir/BOOT-INF/classes/application.properties"
  printf 'dependency placeholder\n' > "$jar_dir/BOOT-INF/lib/example.jar"
  printf 'loader placeholder\n' > "$jar_dir/org/springframework/boot/loader/JarLauncher.class"
  (cd "$jar_dir" && zip -q -r "$jar_path" META-INF BOOT-INF org)
  rm -rf "$jar_dir"
}

test_gitignore_blocks_default_output() {
  assert_contains "$repo_root/.gitignore" "/release-artifacts/"
}

test_default_manifest_is_exact_release_allowlist() {
  local test_dir="$1"
  local actual_file="$test_dir/default-manifest.actual"
  local expected_file="$test_dir/default-manifest.expected"

  assert_file "$default_manifest"
  grep -vE '^[[:space:]]*(#|$)' "$default_manifest" > "$actual_file"
  cat > "$expected_file" <<'EOF'
smart-gateway|smart/smart-gateway/target/smart-gateway.jar
smart-auth|smart/smart-auth/target/smart-auth.jar
smart-upms-biz|smart/smart-upms/smart-upms-biz/target/smart-upms-biz.jar
smart-app-biz|smart-module/smart-app/smart-app-biz/target/smart-app-biz.jar
smart-platform-biz|smart-module/smart-platform/smart-platform-biz/target/smart-platform-biz.jar
smart-data-biz|smart-module/smart-data/smart-data-biz/target/smart-data-biz.jar
smart-bridge-biz|smart-module/smart-bridge/smart-bridge-biz/target/smart-bridge-biz.jar
smart-bridge-isc-biz|smart-module/smart-bridge-isc/smart-bridge-isc-biz/target/smart-bridge-isc-biz.jar
smart-bridge-concentrator-biz|smart-module/smart-bridge-concentrator/smart-bridge-concentrator-biz/target/smart-bridge-concentrator-biz.jar
smart-algorithm-biz|smart-module/smart-algorithm/smart-algorithm-biz/target/smart-algorithm-biz.jar
smart-push-biz|smart-module/smart-push/smart-push-biz/target/smart-push-biz.jar
smart-dispatcher-biz|smart-module/smart-dispatcher/smart-dispatcher-biz/target/smart-dispatcher-biz.jar
smart-schedule|smart-module/smart-schedule/target/smart-schedule.jar
file|smart-module/FileReceiver/build/file.jar
EOF

  diff -u "$expected_file" "$actual_file" || fail "default manifest changed unexpectedly"
}

test_builds_platform_api_before_smart_upms() {
  local test_dir="$1"
  local source_dir="$test_dir/platform-api-bootstrap-source"
  local output_dir="$test_dir/platform-api-bootstrap-output"
  local manifest_file="$test_dir/platform-api-bootstrap.manifest"
  local maven_dir="$test_dir/fake-maven"
  local maven_command="$maven_dir/mvn"
  local maven_log="$test_dir/maven.log"
  local bootstrap_command
  local platform_api_command
  local smart_command

  mkdir -p "$source_dir" "$maven_dir"
  make_boot_jar "$source_dir/smart-good.jar"
  cat > "$manifest_file" <<EOF
smart-good|$source_dir/smart-good.jar
EOF
  cat > "$maven_command" <<'EOF'
#!/usr/bin/env bash
printf '%s|%s\n' "$PWD" "$*" >> "${MAVEN_LOG:?}"
EOF
  chmod +x "$maven_command"

  MAVEN_CMD="$maven_command" MAVEN_LOG="$maven_log" \
    "$script_path" --manifest "$manifest_file" --output "$output_dir"

  bootstrap_command="$repo_root/smart|--projects smart-common/smart-common-bom,smart-common/smart-common-core,smart-common/smart-common-swagger --also-make clean install -DskipTests"
  platform_api_command="$repo_root/smart-module|--projects smart-platform/smart-platform-api --also-make clean install -DskipTests"
  smart_command="$repo_root/smart|clean install -DskipTests"

  assert_line_before "$maven_log" "$bootstrap_command" "$platform_api_command"
  assert_line_before "$maven_log" "$platform_api_command" "$smart_command"
}

test_collects_manifest_jars_and_writes_release_metadata() {
  local test_dir="$1"
  local source_dir="$test_dir/source"
  local output_dir="$test_dir/output"
  local manifest_file="$test_dir/release-jars.manifest"

  mkdir -p "$source_dir"
  make_boot_jar "$source_dir/smart-good.jar"
  make_jar_with_manifest "$source_dir/plain-library.jar" $'Manifest-Version: 1.0\nCreated-By: Maven JAR Plugin'

  cat > "$manifest_file" <<EOF
# service|jar path
smart-good|$source_dir/smart-good.jar
EOF

  "$script_path" --skip-build --manifest "$manifest_file" --output "$output_dir"

  assert_file "$output_dir/smart-jar/smart-good.jar"
  assert_not_file "$output_dir/smart-jar/plain-library.jar"
  assert_file "$output_dir/manifest.csv"
  assert_file "$output_dir/sha256sums.txt"
  assert_file "$output_dir/build-info.txt"
  assert_contains "$output_dir/manifest.csv" "smart-good"
  assert_contains "$output_dir/manifest.csv" "$source_dir/smart-good.jar"
  assert_contains "$output_dir/sha256sums.txt" "smart-jar/smart-good.jar"
  assert_contains "$output_dir/build-info.txt" "Build mode: skipped"
}

test_missing_manifest_jar_fails_fast() {
  local test_dir="$1"
  local output_dir="$test_dir/missing-output"
  local manifest_file="$test_dir/missing.manifest"
  local stderr_file="$test_dir/missing.stderr"

  cat > "$manifest_file" <<EOF
missing-service|$test_dir/does-not-exist.jar
EOF

  if "$script_path" --skip-build --manifest "$manifest_file" --output "$output_dir" 2>"$stderr_file"; then
    fail "expected missing jar collection to fail"
  fi

  assert_contains "$stderr_file" "Missing release jar for missing-service"
}

test_plain_jar_in_manifest_fails_fast() {
  local test_dir="$1"
  local source_dir="$test_dir/plain-source"
  local output_dir="$test_dir/plain-output"
  local manifest_file="$test_dir/plain.manifest"
  local stderr_file="$test_dir/plain.stderr"

  mkdir -p "$source_dir"
  make_jar_with_manifest "$source_dir/plain-library.jar" $'Manifest-Version: 1.0\nCreated-By: Maven JAR Plugin'

  cat > "$manifest_file" <<EOF
plain-library|$source_dir/plain-library.jar
EOF

  if "$script_path" --skip-build --manifest "$manifest_file" --output "$output_dir" 2>"$stderr_file"; then
    fail "expected plain jar collection to fail"
  fi

  assert_contains "$stderr_file" "not a Spring Boot executable jar"
}

test_manifest_only_jar_is_rejected() {
  local test_dir="$1"
  local source_dir="$test_dir/manifest-only-source"
  local output_dir="$test_dir/manifest-only-output"
  local manifest_file="$test_dir/manifest-only.manifest"
  local stderr_file="$test_dir/manifest-only.stderr"

  mkdir -p "$source_dir"
  make_jar_with_manifest "$source_dir/manifest-only.jar" $'Manifest-Version: 1.0\nMain-Class: org.springframework.boot.loader.JarLauncher'

  cat > "$manifest_file" <<EOF
manifest-only|$source_dir/manifest-only.jar
EOF

  if "$script_path" --skip-build --manifest "$manifest_file" --output "$output_dir" 2>"$stderr_file"; then
    fail "expected manifest-only jar collection to fail"
  fi

  assert_contains "$stderr_file" "missing Spring Boot jar entry"
}

test_duplicate_artifact_names_fail_fast() {
  local test_dir="$1"
  local source_dir="$test_dir/duplicate-source"
  local output_dir="$test_dir/duplicate-output"
  local manifest_file="$test_dir/duplicate.manifest"
  local stderr_file="$test_dir/duplicate.stderr"

  mkdir -p "$source_dir/one" "$source_dir/two"
  make_boot_jar "$source_dir/one/same.jar"
  make_boot_jar "$source_dir/two/same.jar"

  cat > "$manifest_file" <<EOF
one|$source_dir/one/same.jar
two|$source_dir/two/same.jar
EOF

  if "$script_path" --skip-build --manifest "$manifest_file" --output "$output_dir" 2>"$stderr_file"; then
    fail "expected duplicate artifact name collection to fail"
  fi

  assert_contains "$stderr_file" "Duplicate release artifact name"
}

test_failed_collection_leaves_no_release_directory() {
  local test_dir="$1"
  local source_dir="$test_dir/partial-source"
  local output_dir="$test_dir/partial-output"
  local manifest_file="$test_dir/partial.manifest"
  local stderr_file="$test_dir/partial.stderr"

  mkdir -p "$source_dir"
  make_boot_jar "$source_dir/good.jar"

  cat > "$manifest_file" <<EOF
good|$source_dir/good.jar
missing|$source_dir/missing.jar
EOF

  if "$script_path" --skip-build --manifest "$manifest_file" --output "$output_dir" 2>"$stderr_file"; then
    fail "expected partial manifest collection to fail"
  fi

  assert_contains "$stderr_file" "Missing release jar for missing"
  assert_not_dir "$output_dir"
}

test_repo_root_output_is_rejected() {
  local test_dir="$1"
  local source_dir="$test_dir/root-output-source"
  local manifest_file="$test_dir/root-output.manifest"
  local stderr_file="$test_dir/root-output.stderr"

  mkdir -p "$source_dir"
  make_boot_jar "$source_dir/smart-good.jar"

  cat > "$manifest_file" <<EOF
smart-good|$source_dir/smart-good.jar
EOF

  if "$script_path" --skip-build --manifest "$manifest_file" --output "$repo_root" 2>"$stderr_file"; then
    fail "expected project root output to fail"
  fi

  assert_contains "$stderr_file" "Refusing unsafe output directory"
}

test_tracked_repo_output_is_rejected() {
  local test_dir="$1"
  local source_dir="$test_dir/tracked-output-source"
  local manifest_file="$test_dir/tracked-output.manifest"
  local stderr_file="$test_dir/tracked-output.stderr"

  mkdir -p "$source_dir"
  make_boot_jar "$source_dir/smart-good.jar"

  cat > "$manifest_file" <<EOF
smart-good|$source_dir/smart-good.jar
EOF

  if "$script_path" --skip-build --manifest "$manifest_file" --output "$repo_root/scripts/release-output" 2>"$stderr_file"; then
    fail "expected tracked project output to fail"
  fi

  assert_contains "$stderr_file" "Output directory inside project must be under release-artifacts"
}

main() {
  command -v zip >/dev/null 2>&1 || fail "missing zip command"

  test_dir="$(mktemp -d "${TMPDIR:-/tmp}/release-jars-test.XXXXXX")"
  trap 'rm -rf "$test_dir"' EXIT

  test_gitignore_blocks_default_output
  test_default_manifest_is_exact_release_allowlist "$test_dir"
  test_builds_platform_api_before_smart_upms "$test_dir"
  test_collects_manifest_jars_and_writes_release_metadata "$test_dir"
  test_missing_manifest_jar_fails_fast "$test_dir"
  test_plain_jar_in_manifest_fails_fast "$test_dir"
  test_manifest_only_jar_is_rejected "$test_dir"
  test_duplicate_artifact_names_fail_fast "$test_dir"
  test_failed_collection_leaves_no_release_directory "$test_dir"
  test_repo_root_output_is_rejected "$test_dir"
  test_tracked_repo_output_is_rejected "$test_dir"

  echo "All build-release-jars tests passed."
}

main "$@"
