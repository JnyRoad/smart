#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd -P)"
script="$repo_root/scripts/restartService.sh"

fail() {
  echo "FAIL: $*" >&2
  exit 1
}

assert_contains() {
  local file="$1"
  local expected="$2"
  grep -Fqx -- "$expected" "$file" >/dev/null || fail "Expected '$expected' in $file"
}

assert_not_contains() {
  local file="$1"
  local unexpected="$2"
  if grep -Fq -- "$unexpected" "$file"; then
    fail "Did not expect '$unexpected' in $file"
  fi
}

run_expect_failure() {
  local output_file="$1"
  shift
  if "$@" >"$output_file" 2>&1; then
    fail "Command unexpectedly succeeded: $*"
  fi
}

write_fake_stat() {
  local pid="$1"
  local command_name="$2"
  local start_time="$3"
  local _

  # /proc/<pid>/stat 的第 22 列为 starttime；去掉前两列后是第 20 列。
  {
    printf '%s (%s) S' "$pid" "$command_name"
    for _ in {1..18}; do
      printf ' 0'
    done
    printf ' %s\n' "$start_time"
  } >"$proc_root/$pid/stat"
}

test_dir="$(mktemp -d "${TMPDIR:-/tmp}/restart-service-test.XXXXXX")"
trap 'rm -rf "$test_dir"' EXIT

app_root="$test_dir/app"
proc_root="$test_dir/proc"
fake_bin="$test_dir/bin"
state_dir="$test_dir/state"
mkdir -p "$app_root/smart-jar" "$proc_root" "$fake_bin" "$state_dir"
app_root="$(cd "$app_root" && pwd -P)"
proc_root="$(cd "$proc_root" && pwd -P)"
fake_bin="$(cd "$fake_bin" && pwd -P)"
state_dir="$(cd "$state_dir" && pwd -P)"
touch "$app_root/smart-jar/smart-push-biz.jar"

cat >"$fake_bin/kill" <<'SH'
#!/usr/bin/env bash
set -euo pipefail
printf '%s\n' "$*" >>"$TEST_STATE/kill.log"
if [ "$1" = "-TERM" ]; then
  if [ "${TEST_RACE_ON_TERM:-}" = "$2" ]; then
    rm -rf "$TEST_PROC_ROOT/$2"
    rm -f "$TEST_STATE/listening-port"
    exit 1
  fi
  [ -d "$TEST_PROC_ROOT/$2" ] || exit 1
  rm -rf "$TEST_PROC_ROOT/$2"
  rm -f "$TEST_STATE/listening-port"
fi
SH

cat >"$fake_bin/ss" <<'SH'
#!/usr/bin/env bash
set -euo pipefail
if [ -f "$TEST_STATE/listening-port" ]; then
  printf 'LISTEN 0 128 127.0.0.1:%s 0.0.0.0:* users:(("java",pid=%s,fd=1))\n' "$(cat "$TEST_STATE/listening-port")" "$(cat "$TEST_STATE/listening-pid")"
fi
SH

cat >"$fake_bin/crontab" <<'SH'
#!/usr/bin/env bash
set -euo pipefail
if [ -f "$TEST_STATE/watchdog-enabled" ]; then
  printf '%s\n' '@reboot sh /home/yuto/smart/136checksmart.sh'
fi
SH

cat >"$app_root/app.sh" <<'SH'
#!/usr/bin/env bash
set -euo pipefail
printf '%s\n' "$*" >>"$TEST_STATE/app.log"
pid=5151
mkdir -p "$TEST_PROC_ROOT/$pid"
printf 'java\0-jar\0%s\0' "$TEST_ROOT/$2" >"$TEST_PROC_ROOT/$pid/cmdline"
ln -s "$TEST_ROOT" "$TEST_PROC_ROOT/$pid/cwd"
{
  printf '%s (java) S' "$pid"
  for _ in {1..18}; do
    printf ' 0'
  done
  printf ' %s\n' "$pid"
} >"$TEST_PROC_ROOT/$pid/stat"
printf '%s\n' 6090 >"$TEST_STATE/listening-port"
printf '%s\n' "$pid" >"$TEST_STATE/listening-pid"
SH

chmod +x "$fake_bin/kill" "$fake_bin/ss" "$fake_bin/crontab" "$app_root/app.sh"

export SMART_APP_ROOT="$app_root"
export SMART_PROC_ROOT="$proc_root"
export SMART_APP_SCRIPT="$app_root/app.sh"
export SMART_KILL_BIN="$fake_bin/kill"
export SMART_SS_BIN="$fake_bin/ss"
export SMART_CRONTAB_BIN="$fake_bin/crontab"
export SMART_STOP_TIMEOUT_SECONDS=1
export SMART_START_TIMEOUT_SECONDS=1
export SMART_POLL_SECONDS=1
export TEST_PROC_ROOT="$proc_root"
export TEST_ROOT="$app_root"
export TEST_STATE="$state_dir"
unset SMART_WATCHDOG_STOP_CONFIRMED

[ -x "$script" ] || fail "Expected production restart script at $script"

if ! "$script" --help >"$state_dir/help.log"; then
  fail "Help must succeed without selecting a service"
fi
grep -Fq '用法' "$state_dir/help.log" || fail "Help must describe the command usage"

run_expect_failure "$state_dir/unknown.log" "$script" status unknown
grep -Fq 'Unsupported service' "$state_dir/unknown.log" || fail "Unknown service should be rejected"

mkdir -p "$proc_root/4242"
printf 'java\0-jar\0%s\0' "$app_root/smart-jar/smart-push-biz.jar" >"$proc_root/4242/cmdline"
ln -s "$app_root" "$proc_root/4242/cwd"
write_fake_stat 4242 java 100
printf '%s\n' 6090 >"$state_dir/listening-port"
printf '%s\n' 4242 >"$state_dir/listening-pid"

"$script" status push >"$state_dir/status.log"
grep -Fq 'smart-push-biz.jar' "$state_dir/status.log" || fail "Push status must target smart-push-biz.jar"

run_expect_failure "$state_dir/watchdog-confirmation.log" "$script" restart push
grep -Fq 'SMART_WATCHDOG_STOP_CONFIRMED=1' "$state_dir/watchdog-confirmation.log" || fail "Restart must require explicit watchdog confirmation"
[ ! -e "$state_dir/kill.log" ] || fail "Restart must not signal a service without watchdog confirmation"
[ ! -e "$state_dir/app.log" ] || fail "Restart must not start a service without watchdog confirmation"
export SMART_WATCHDOG_STOP_CONFIRMED=1

"$script" restart push
assert_contains "$state_dir/kill.log" "-TERM 4242"
assert_not_contains "$state_dir/kill.log" "-9 4242"
assert_contains "$state_dir/app.log" "start smart-jar/smart-push-biz.jar"

"$script" stop push >"$state_dir/stop.log"
grep -Fq 'push is stopped' "$state_dir/stop.log" || fail "Stop must report the stopped state"

# 非 Java 进程即使带有相同 JAR 参数和工作目录，也绝不能成为停止目标。
rm -f "$state_dir/app.log" "$state_dir/kill.log" "$state_dir/listening-port" "$state_dir/listening-pid"
mkdir -p "$proc_root/3333"
printf 'sleep\0smart-jar/smart-push-biz.jar\0' >"$proc_root/3333/cmdline"
ln -s "$app_root" "$proc_root/3333/cwd"
write_fake_stat 3333 sleep 200
"$script" stop push >"$state_dir/non-java-stop.log"
grep -Fq 'push is already stopped' "$state_dir/non-java-stop.log" || fail "Stop must ignore a non-Java process with a JAR-looking argument"
[ ! -e "$state_dir/kill.log" ] || fail "Stop must not signal a non-Java process with a JAR-looking argument"
[ -d "$proc_root/3333" ] || fail "Stop must leave the unrelated non-Java process untouched"
rm -rf "$proc_root/3333"

# 无法稳定读取 starttime 时，脚本必须拒绝操作，而不是把运行实例误报为已停止。
mkdir -p "$proc_root/3344"
printf 'java\0-jar\0%s\0' "$app_root/smart-jar/smart-push-biz.jar" >"$proc_root/3344/cmdline"
ln -s "$app_root" "$proc_root/3344/cwd"
run_expect_failure "$state_dir/missing-stat.log" "$script" stop push
grep -Fq 'identity cannot be established' "$state_dir/missing-stat.log" || fail "Stop must reject a target without a stable process identity"
[ ! -e "$state_dir/kill.log" ] || fail "Stop must not signal a target without a stable process identity"
[ -d "$proc_root/3344" ] || fail "Stop must leave a target without a stable process identity untouched"
rm -rf "$proc_root/3344"

# 工件在替换过程中丢失时，仍必须能停止经过精确进程/工作目录匹配的旧实例。
rm -f "$state_dir/app.log" "$state_dir/kill.log" "$state_dir/listening-port" "$state_dir/listening-pid"
mkdir -p "$proc_root/4242"
printf 'java\0-jar\0%s\0' "$app_root/smart-jar/smart-push-biz.jar" >"$proc_root/4242/cmdline"
ln -s "$app_root" "$proc_root/4242/cwd"
write_fake_stat 4242 java 300
printf '%s\n' 6090 >"$state_dir/listening-port"
printf '%s\n' 4242 >"$state_dir/listening-pid"
rm -f "$app_root/smart-jar/smart-push-biz.jar"
if ! "$script" stop push >"$state_dir/stop-without-artifact.log"; then
  fail "Stop must allow a precisely matched running service when its artifact is missing"
fi
grep -Fq 'push is stopped' "$state_dir/stop-without-artifact.log" || fail "Stop without artifact must report the stopped state"
assert_contains "$state_dir/kill.log" "-TERM 4242"
[ ! -d "$proc_root/4242" ] || fail "Stop without artifact must terminate the matched process"
touch "$app_root/smart-jar/smart-push-biz.jar"

rm -f "$state_dir/app.log" "$state_dir/kill.log" "$state_dir/listening-port" "$state_dir/listening-pid"
run_expect_failure "$state_dir/missing.log" "$script" restart push
grep -Fq 'not running' "$state_dir/missing.log" || fail "Restart must reject an already stopped service"
[ ! -e "$state_dir/app.log" ] || fail "Restart must not start an absent service"

# 模拟 restart 已识别初始 PID、但二次扫描前该 PID 自行退出的竞态；此时不能继续 start。
# shellcheck disable=SC2016 # $1 必须由子 bash 在运行时展开，不能在测试脚本当前 shell 展开。
run_expect_failure "$state_dir/disappeared-before-stop.log" bash -c '
  source "$1"
  configure_service push
  require_valid_runtime
  stop_service 4242
' bash "$script"
grep -Fq 'no longer running before SIGTERM' "$state_dir/disappeared-before-stop.log" || fail "Restart must reject a PID that disappears before SIGTERM"

# PID 被新进程复用时，starttime 改变；不能仅因 PID 数字相同就发送 SIGTERM。
mkdir -p "$proc_root/4242"
printf 'java\0-jar\0%s\0' "$app_root/smart-jar/smart-push-biz.jar" >"$proc_root/4242/cmdline"
ln -s "$app_root" "$proc_root/4242/cwd"
write_fake_stat 4242 java 601
# shellcheck disable=SC2016 # $1 必须由子 bash 在运行时展开，不能在测试脚本当前 shell 展开。
run_expect_failure "$state_dir/pid-reused.log" bash -c '
  source "$1"
  configure_service push
  require_valid_runtime
  stop_service 4242 600
' bash "$script"
grep -Fq 'identity changed' "$state_dir/pid-reused.log" || fail "Restart must reject a PID whose starttime changed"
[ ! -e "$state_dir/kill.log" ] || fail "Restart must not signal a PID reused by another process"
[ -d "$proc_root/4242" ] || fail "Restart must leave a PID-reused process untouched"
rm -rf "$proc_root/4242"

mkdir -p "$proc_root/4242"
printf 'java\0-jar\0%s\0' "$app_root/smart-jar/smart-push-biz.jar" >"$proc_root/4242/cmdline"
ln -s "$app_root" "$proc_root/4242/cwd"
write_fake_stat 4242 java 400
printf '%s\n' 6090 >"$state_dir/listening-port"
printf '%s\n' 9999 >"$state_dir/listening-pid"
run_expect_failure "$state_dir/wrong-port.log" "$script" status push
grep -Fq 'port 6090 is not listening' "$state_dir/wrong-port.log" || fail "Status must reject a port owned by another PID"
rm -rf "$proc_root/4242"
rm -f "$state_dir/listening-port" "$state_dir/listening-pid"

mkdir -p "$proc_root/4242"
printf 'java\0-jar\0%s\0' "$app_root/smart-jar/smart-push-biz.jar" >"$proc_root/4242/cmdline"
ln -s "$app_root" "$proc_root/4242/cwd"
write_fake_stat 4242 java 500
printf '%s\n' 6090 >"$state_dir/listening-port"
printf '%s\n' 4242 >"$state_dir/listening-pid"
export TEST_RACE_ON_TERM=4242
run_expect_failure "$state_dir/race.log" "$script" restart push
unset TEST_RACE_ON_TERM
grep -Fq 'Failed to send SIGTERM' "$state_dir/race.log" || fail "Restart must fail if the original PID exits before SIGTERM"
[ ! -e "$state_dir/app.log" ] || fail "Restart must not start after the original PID exits before SIGTERM"
rm -f "$state_dir/kill.log" "$state_dir/listening-port" "$state_dir/listening-pid"

for pid in 4242 5252; do
  mkdir -p "$proc_root/$pid"
  printf 'java\0-jar\0%s\0' "$app_root/smart-jar/smart-push-biz.jar" >"$proc_root/$pid/cmdline"
  ln -s "$app_root" "$proc_root/$pid/cwd"
  write_fake_stat "$pid" java "$pid"
done
printf '%s\n' 6090 >"$state_dir/listening-port"
printf '%s\n' 4242 >"$state_dir/listening-pid"
run_expect_failure "$state_dir/multiple.log" "$script" restart push
grep -Fq 'Multiple processes match' "$state_dir/multiple.log" || fail "Multiple matching processes must be rejected"
[ ! -e "$state_dir/kill.log" ] || fail "Restart must not signal an ambiguous process set"
[ ! -e "$state_dir/app.log" ] || fail "Restart must not start while process matching is ambiguous"

rm -rf "$proc_root/5252"
rm -f "$state_dir/app.log" "$state_dir/kill.log"
touch "$state_dir/watchdog-enabled"
run_expect_failure "$state_dir/watchdog.log" "$script" restart push
grep -Fq 'watchdog' "$state_dir/watchdog.log" || fail "Restart should refuse while watchdog is configured"
[ ! -e "$state_dir/app.log" ] || fail "Restart must not start a service while watchdog is configured"

echo "restart service tests passed"
