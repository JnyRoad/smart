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
  local session_id="${4:-0}"
  local _

  # /proc/<pid>/stat 的第 6 列为 session、第 22 列为 starttime；去掉前两列后
  # 分别是第 4、20 列。
  {
    printf '%s (%s) S 0 0 %s' "$pid" "$command_name" "$session_id"
    for _ in {1..15}; do
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

cat >"$fake_bin/sudo" <<'SH'
#!/usr/bin/env bash
set -euo pipefail

printf '%s\n' "$*" >>"$TEST_STATE/sudo.log"
if [ "${1:-}" = "--" ]; then
  shift
fi
"$@"
SH

cat >"$fake_bin/setsid" <<'SH'
#!/usr/bin/env bash
set -euo pipefail

# 测试为即将 exec 的 wrapper 构造与真实 setsid 一致的 /proc 收据：同步路径的
# wrapper 直接由重启脚本派生；fork 路径故意伪造不同父进程，以验证它绝不会获准启动。
printf '%s\n' "$*" >>"$TEST_STATE/setsid.log"
write_wrapper_stat() {
  local pid="$1"
  local parent_pid="$2"
  local session_id="$3"
  local _

  mkdir -p "$TEST_PROC_ROOT/$pid"
  {
    printf '%s (bash) S %s 0 %s' "$pid" "$parent_pid" "$session_id"
    for _ in {1..15}; do
      printf ' 0'
    done
    printf ' %s\n' "$pid"
  } >"$TEST_PROC_ROOT/$pid/stat"
}

if [ "${TEST_SETSID_FORK:-0}" = "1" ]; then
  (
    sleep "${TEST_SETSID_DELAY_SECONDS:-0}"
    # macOS 自带 Bash 3 没有 BASHPID；让新的 bash 在 exec wrapper 前用自身 $$
    # 写入伪 /proc，并固定父进程为不匹配值来模拟 setsid 的 fork 分支。
    exec bash -c '
      set -eu
      pid=$$
      mkdir -p "$TEST_PROC_ROOT/$pid"
      {
        printf "%s (bash) S 99999 0 %s" "$pid" "$pid"
        for _ in {1..15}; do
          printf " 0"
        done
        printf " %s\\n" "$pid"
      } >"$TEST_PROC_ROOT/$pid/stat"
      exec "$@"
    ' bash "$@"
  ) &
  exit 0
fi
write_wrapper_stat "$$" "$PPID" "$$"
exec "$@"
SH

cat >"$fake_bin/watchdog-checker" <<'SH'
#!/usr/bin/env bash
set -euo pipefail

printf '%s\n' "$*" >>"$TEST_STATE/watchdog-checker.log"
if [ -f "$TEST_STATE/root-watchdog-enabled" ]; then
  echo "watchdog configuration found in root preflight" >&2
  exit 1
fi
SH

cat >"$app_root/app.sh" <<'SH'
#!/usr/bin/env bash
set -euo pipefail
printf 'cwd=%s\n' "$(pwd)" >>"$TEST_STATE/app.log"
printf '%s\n' "$*" >>"$TEST_STATE/app.log"
pid=5151
session_id="${SMART_RESTART_SERVICE_SESSION_ID:-0}"
if [ "${TEST_APP_CREATES_PROCESS:-1}" = "1" ]; then
  mkdir -p "$TEST_PROC_ROOT/$pid"
  printf 'java\0-jar\0%s\0' "$TEST_ROOT/$2" >"$TEST_PROC_ROOT/$pid/cmdline"
  ln -s "$TEST_ROOT" "$TEST_PROC_ROOT/$pid/cwd"
  {
    printf '%s (java) S 0 0 %s' "$pid" "$session_id"
    for _ in {1..15}; do
      printf ' 0'
    done
    printf ' %s\n' "$pid"
  } >"$TEST_PROC_ROOT/$pid/stat"
  if [ "${TEST_START_LISTENS:-1}" = "1" ]; then
    printf '%s\n' 6090 >"$TEST_STATE/listening-port"
    printf '%s\n' "$pid" >"$TEST_STATE/listening-pid"
  fi
fi
if [ "${TEST_APP_CREATES_FOREIGN_PROCESS:-0}" = "1" ]; then
  foreign_pid=5252
  mkdir -p "$TEST_PROC_ROOT/$foreign_pid"
  printf 'java\0-jar\0%s\0' "$TEST_ROOT/$2" >"$TEST_PROC_ROOT/$foreign_pid/cmdline"
  ln -s "$TEST_ROOT" "$TEST_PROC_ROOT/$foreign_pid/cwd"
  {
    printf '%s (java) S 0 0 99999' "$foreign_pid"
    for _ in {1..15}; do
      printf ' 0'
    done
    printf ' %s\n' "$foreign_pid"
  } >"$TEST_PROC_ROOT/$foreign_pid/stat"
fi
if [ "${TEST_APP_EXIT_CODE:-0}" -ne 0 ]; then
  exit "$TEST_APP_EXIT_CODE"
fi
SH

chmod +x "$fake_bin/kill" "$fake_bin/ss" "$fake_bin/crontab" "$fake_bin/sudo" "$fake_bin/setsid" "$fake_bin/watchdog-checker" "$app_root/app.sh"

export SMART_APP_ROOT="$app_root"
export SMART_PROC_ROOT="$proc_root"
export SMART_APP_SCRIPT="$app_root/app.sh"
export SMART_KILL_BIN="$fake_bin/kill"
export SMART_SS_BIN="$fake_bin/ss"
export SMART_SETSID_BIN="$fake_bin/setsid"
export SMART_CRONTAB_BIN="$fake_bin/crontab"
export SMART_STOP_TIMEOUT_SECONDS=1
export SMART_START_TIMEOUT_SECONDS=1
export SMART_START_LAUNCH_TIMEOUT_SECONDS=3
export SMART_POLL_SECONDS=1
export TEST_PROC_ROOT="$proc_root"
export TEST_ROOT="$app_root"
export TEST_STATE="$state_dir"
# 旧实现会把它当成放行条件；新实现必须忽略它并执行特权预检。
export SMART_WATCHDOG_STOP_CONFIRMED=1

[ -x "$script" ] || fail "Expected production restart script at $script"

# 管理员以 sudo 直接运行脚本时，root 预检仍须检查原服务账号 yuto，不能只检查
# root 的 crontab。这里通过假 id 和同一假检查器覆盖原始 require_watchdog_stopped。
rm -f "$state_dir/watchdog-checker.log"
TEST_STATE="$state_dir" bash -c '
  source "$1"
  id() {
    case "$1" in
      -u) printf "0\\n" ;;
      -un) printf "root\\n" ;;
      *) return 1 ;;
    esac
  }
  SUDO_USER=yuto
  WATCHDOG_CHECKER="$2"
  require_watchdog_stopped
' bash "$script" "$fake_bin/watchdog-checker"
grep -Fq -- "--service-user yuto" "$state_dir/watchdog-checker.log" || fail "A root sudo invocation must inspect yuto"

# shellcheck disable=SC2016 # $1、$2 必须由子 bash 在运行时展开。
run_expect_failure "$state_dir/root-without-service-user.log" bash -c '
  source "$1"
  id() {
    case "$1" in
      -u) printf "0\\n" ;;
      -un) printf "root\\n" ;;
      *) return 1 ;;
    esac
  }
  unset SUDO_USER SMART_ROOT_SERVICE_USER
  WATCHDOG_CHECKER="$2"
  require_watchdog_stopped
' bash "$script" "$fake_bin/watchdog-checker"
grep -Fq 'SMART_ROOT_SERVICE_USER' "$state_dir/root-without-service-user.log" || fail "Direct root must require the service user"

run_controlled_script() {
  bash -c '
    source "$1"
    SUDO_BIN="$2"
    WATCHDOG_CHECKER="$3"
    main "${@:4}"
  ' bash "$script" "$fake_bin/sudo" "$fake_bin/watchdog-checker" "$@"
}

if ! "$script" --help >"$state_dir/help.log"; then
  fail "Help must succeed without selecting a service"
fi
grep -Fq '用法' "$state_dir/help.log" || fail "Help must describe the command usage"
grep -Fq 'SMART_APP_ROOT' "$state_dir/help.log" || fail "Help must describe the required app root contract"

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

touch "$state_dir/root-watchdog-enabled"
run_expect_failure "$state_dir/root-watchdog.log" run_controlled_script restart push
grep -Fq 'watchdog configuration found' "$state_dir/root-watchdog.log" || fail "Restart must reject a root watchdog preflight failure"
[ ! -e "$state_dir/kill.log" ] || fail "Restart must not signal a service while root watchdog verification fails"
[ ! -e "$state_dir/app.log" ] || fail "Restart must not start a service while root watchdog verification fails"
[ -e "$state_dir/sudo.log" ] || fail "Restart must invoke the privileged watchdog checker"
rm -f "$state_dir/root-watchdog-enabled" "$state_dir/sudo.log" "$state_dir/watchdog-checker.log"

run_controlled_script restart push
assert_contains "$state_dir/kill.log" "-TERM 4242"
assert_not_contains "$state_dir/kill.log" "-9 4242"
assert_contains "$state_dir/app.log" "cwd=$app_root"
assert_contains "$state_dir/app.log" "start smart-jar/smart-push-biz.jar"
if grep -Fq -- "--wait" "$state_dir/setsid.log"; then
  fail "Restart must not require a newer setsid --wait option"
fi

run_controlled_script stop push >"$state_dir/stop.log"
grep -Fq 'push is stopped' "$state_dir/stop.log" || fail "Stop must report the stopped state"

# 非 Java 进程即使带有相同 JAR 参数和工作目录，也绝不能成为停止目标。
rm -f "$state_dir/app.log" "$state_dir/kill.log" "$state_dir/listening-port" "$state_dir/listening-pid"
mkdir -p "$proc_root/3333"
printf 'sleep\0smart-jar/smart-push-biz.jar\0' >"$proc_root/3333/cmdline"
ln -s "$app_root" "$proc_root/3333/cwd"
write_fake_stat 3333 sleep 200
run_controlled_script stop push >"$state_dir/non-java-stop.log"
grep -Fq 'push is already stopped' "$state_dir/non-java-stop.log" || fail "Stop must ignore a non-Java process with a JAR-looking argument"
[ ! -e "$state_dir/kill.log" ] || fail "Stop must not signal a non-Java process with a JAR-looking argument"
[ -d "$proc_root/3333" ] || fail "Stop must leave the unrelated non-Java process untouched"
rm -rf "$proc_root/3333"

# 无法稳定读取 starttime 时，脚本必须拒绝操作，而不是把运行实例误报为已停止。
mkdir -p "$proc_root/3344"
printf 'java\0-jar\0%s\0' "$app_root/smart-jar/smart-push-biz.jar" >"$proc_root/3344/cmdline"
ln -s "$app_root" "$proc_root/3344/cwd"
run_expect_failure "$state_dir/missing-stat.log" run_controlled_script stop push
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
if ! run_controlled_script stop push >"$state_dir/stop-without-artifact.log"; then
  fail "Stop must allow a precisely matched running service when its artifact is missing"
fi
grep -Fq 'push is stopped' "$state_dir/stop-without-artifact.log" || fail "Stop without artifact must report the stopped state"
assert_contains "$state_dir/kill.log" "-TERM 4242"
[ ! -d "$proc_root/4242" ] || fail "Stop without artifact must terminate the matched process"
touch "$app_root/smart-jar/smart-push-biz.jar"

rm -f "$state_dir/app.log" "$state_dir/kill.log" "$state_dir/listening-port" "$state_dir/listening-pid"
run_expect_failure "$state_dir/missing.log" run_controlled_script restart push
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
run_expect_failure "$state_dir/race.log" run_controlled_script restart push
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
run_expect_failure "$state_dir/multiple.log" run_controlled_script restart push
grep -Fq 'Multiple processes match' "$state_dir/multiple.log" || fail "Multiple matching processes must be rejected"
[ ! -e "$state_dir/kill.log" ] || fail "Restart must not signal an ambiguous process set"
[ ! -e "$state_dir/app.log" ] || fail "Restart must not start while process matching is ambiguous"

rm -rf "$proc_root/5252"
rm -f "$state_dir/app.log" "$state_dir/kill.log"
touch "$state_dir/root-watchdog-enabled"
run_expect_failure "$state_dir/watchdog.log" run_controlled_script restart push
grep -Fq 'watchdog' "$state_dir/watchdog.log" || fail "Restart should refuse while watchdog is configured"
[ ! -e "$state_dir/app.log" ] || fail "Restart must not start a service while watchdog is configured"

# 新实例在启动超时后必须被精确终止，不能留下一个没有监听端口的后台 Java 进程。
rm -f "$state_dir/root-watchdog-enabled" "$state_dir/app.log" "$state_dir/kill.log" "$state_dir/listening-port" "$state_dir/listening-pid"
rm -rf "$proc_root/4242" "$proc_root/5151"
mkdir -p "$proc_root/4242"
printf 'java\0-jar\0%s\0' "$app_root/smart-jar/smart-push-biz.jar" >"$proc_root/4242/cmdline"
ln -s "$app_root" "$proc_root/4242/cwd"
write_fake_stat 4242 java 700
printf '%s\n' 6090 >"$state_dir/listening-port"
printf '%s\n' 4242 >"$state_dir/listening-pid"
export TEST_START_LISTENS=0
run_expect_failure "$state_dir/start-timeout.log" run_controlled_script restart push
unset TEST_START_LISTENS
grep -Fq 'did not become ready' "$state_dir/start-timeout.log" || fail "Restart must report a startup readiness timeout"
assert_contains "$state_dir/kill.log" "-TERM 4242"
assert_contains "$state_dir/kill.log" "-TERM 5151"
assert_not_contains "$state_dir/kill.log" "-9 5151"
[ ! -d "$proc_root/5151" ] || fail "Restart must clean up the unready instance it started"

# app.sh 即使返回 0，只要没有产生可验证的目标 Java，也必须被识别为启动失败；不能因
# wait_for_start 的空结果误报成功并跳过失败收敛路径。
rm -f "$state_dir/app.log" "$state_dir/kill.log" "$state_dir/listening-port" "$state_dir/listening-pid"
rm -rf "$proc_root/4242" "$proc_root/5151"
mkdir -p "$proc_root/4242"
printf 'java\0-jar\0%s\0' "$app_root/smart-jar/smart-push-biz.jar" >"$proc_root/4242/cmdline"
ln -s "$app_root" "$proc_root/4242/cwd"
write_fake_stat 4242 java 7001
printf '%s\n' 6090 >"$state_dir/listening-port"
printf '%s\n' 4242 >"$state_dir/listening-pid"
export TEST_APP_CREATES_PROCESS=0
run_expect_failure "$state_dir/no-process-start.log" run_controlled_script restart push
unset TEST_APP_CREATES_PROCESS
grep -Fq 'did not become ready' "$state_dir/no-process-start.log" || fail "Restart must reject an app.sh success without a target process"
assert_contains "$state_dir/kill.log" "-TERM 4242"
[ ! -d "$proc_root/5151" ] || fail "An app.sh success without a target process must not leave a Java instance"

# 即使 app.sh 在派生后台实例后返回非零，也必须收敛该实例，不能依赖 set -e 直接退出。
rm -f "$state_dir/app.log" "$state_dir/kill.log" "$state_dir/listening-port" "$state_dir/listening-pid"
rm -rf "$proc_root/4242" "$proc_root/5151"
mkdir -p "$proc_root/4242"
printf 'java\0-jar\0%s\0' "$app_root/smart-jar/smart-push-biz.jar" >"$proc_root/4242/cmdline"
ln -s "$app_root" "$proc_root/4242/cwd"
write_fake_stat 4242 java 701
printf '%s\n' 6090 >"$state_dir/listening-port"
printf '%s\n' 4242 >"$state_dir/listening-pid"
export TEST_APP_EXIT_CODE=7
run_expect_failure "$state_dir/app-start-error.log" run_controlled_script restart push
unset TEST_APP_EXIT_CODE
grep -Fq 'app.sh start failed with status 7' "$state_dir/app-start-error.log" || fail "Restart must report an app.sh start failure"
assert_contains "$state_dir/kill.log" "-TERM 4242"
assert_contains "$state_dir/kill.log" "-TERM 5151"
assert_not_contains "$state_dir/kill.log" "-9 5151"
[ ! -d "$proc_root/5151" ] || fail "Restart must clean up an instance spawned by a failing app.sh"

# 本次 app.sh 未产生目标进程、但另一个操作者恰好拉起同一 Jar 时，失败清理绝不能
# 根据“当前唯一进程”猜测并误杀对方实例。
rm -f "$state_dir/app.log" "$state_dir/kill.log" "$state_dir/listening-port" "$state_dir/listening-pid"
rm -rf "$proc_root/4242" "$proc_root/5151" "$proc_root/5252"
mkdir -p "$proc_root/4242"
printf 'java\0-jar\0%s\0' "$app_root/smart-jar/smart-push-biz.jar" >"$proc_root/4242/cmdline"
ln -s "$app_root" "$proc_root/4242/cwd"
write_fake_stat 4242 java 702
printf '%s\n' 6090 >"$state_dir/listening-port"
printf '%s\n' 4242 >"$state_dir/listening-pid"
export TEST_APP_CREATES_PROCESS=0
export TEST_APP_CREATES_FOREIGN_PROCESS=1
export TEST_APP_EXIT_CODE=7
run_expect_failure "$state_dir/foreign-start-error.log" run_controlled_script restart push
unset TEST_APP_CREATES_PROCESS TEST_APP_CREATES_FOREIGN_PROCESS TEST_APP_EXIT_CODE
assert_contains "$state_dir/kill.log" "-TERM 4242"
assert_not_contains "$state_dir/kill.log" "-TERM 5252"
[ -d "$proc_root/5252" ] || fail "Restart must leave a foreign same-Jar instance untouched"

# app.sh 在本次独立会话中创建目标实例、同时外部又创建同 Jar 实例时，身份不稳定
# 分支也必须清理本次会话的实例，且不能停止外部实例。
rm -f "$state_dir/app.log" "$state_dir/kill.log" "$state_dir/listening-port" "$state_dir/listening-pid"
rm -rf "$proc_root/4242" "$proc_root/5151" "$proc_root/5252"
mkdir -p "$proc_root/4242"
printf 'java\0-jar\0%s\0' "$app_root/smart-jar/smart-push-biz.jar" >"$proc_root/4242/cmdline"
ln -s "$app_root" "$proc_root/4242/cwd"
write_fake_stat 4242 java 703
printf '%s\n' 6090 >"$state_dir/listening-port"
printf '%s\n' 4242 >"$state_dir/listening-pid"
export TEST_START_LISTENS=0
export TEST_APP_CREATES_FOREIGN_PROCESS=1
run_expect_failure "$state_dir/multiple-after-start.log" run_controlled_script restart push
unset TEST_START_LISTENS TEST_APP_CREATES_FOREIGN_PROCESS
assert_contains "$state_dir/kill.log" "-TERM 4242"
assert_contains "$state_dir/kill.log" "-TERM 5151"
assert_not_contains "$state_dir/kill.log" "-TERM 5252"
[ ! -d "$proc_root/5151" ] || fail "Restart must clean up the unready instance from its own start session"
[ -d "$proc_root/5252" ] || fail "Restart must leave a foreign concurrent instance untouched"

# 若 setsid 让父进程先返回、子会话稍后才执行 wrapper，wrapper 的实际父进程就不再
# 是重启脚本；它必须在停止旧实例前拒绝，且 pending gate 不得执行 app.sh。
rm -f "$state_dir/app.log" "$state_dir/kill.log" "$state_dir/listening-port" "$state_dir/listening-pid" "$state_dir/setsid.log"
rm -rf "$proc_root/4242" "$proc_root/5151" "$proc_root/5252"
mkdir -p "$proc_root/4242"
printf 'java\0-jar\0%s\0' "$app_root/smart-jar/smart-push-biz.jar" >"$proc_root/4242/cmdline"
ln -s "$app_root" "$proc_root/4242/cwd"
write_fake_stat 4242 java 704
printf '%s\n' 6090 >"$state_dir/listening-port"
printf '%s\n' 4242 >"$state_dir/listening-pid"
export TEST_SETSID_FORK=1
export TEST_SETSID_DELAY_SECONDS=1
run_expect_failure "$state_dir/setsid-fork.log" run_controlled_script restart push
unset TEST_SETSID_FORK TEST_SETSID_DELAY_SECONDS
grep -Fq 'cannot prove the required direct parent/session contract' "$state_dir/setsid-fork.log" || fail "Restart must reject an asynchronous setsid implementation"
[ ! -e "$state_dir/kill.log" ] || fail "Restart must not stop the old instance when setsid wait behavior cannot be proven"
[ ! -e "$state_dir/app.log" ] || fail "Restart must not start an unprovable isolated session"
[ -d "$proc_root/4242" ] || fail "Restart must leave the old instance running when setsid wait behavior is unsafe"

# 即使 setsid 子会话在预检超时之后才出现，pending/缺失决策也必须让它自行退出；
# 不能因为主脚本已返回就延迟执行 app.sh，或在旧实例保持运行时拉起重复 Java。
rm -f "$state_dir/app.log" "$state_dir/kill.log" "$state_dir/listening-port" "$state_dir/listening-pid" "$state_dir/setsid.log"
rm -rf "$proc_root/4242" "$proc_root/5151" "$proc_root/5252"
mkdir -p "$proc_root/4242"
printf 'java\0-jar\0%s\0' "$app_root/smart-jar/smart-push-biz.jar" >"$proc_root/4242/cmdline"
ln -s "$app_root" "$proc_root/4242/cwd"
write_fake_stat 4242 java 705
printf '%s\n' 6090 >"$state_dir/listening-port"
printf '%s\n' 4242 >"$state_dir/listening-pid"
export TEST_SETSID_FORK=1
export TEST_SETSID_DELAY_SECONDS=4
run_expect_failure "$state_dir/setsid-late.log" run_controlled_script restart push
unset TEST_SETSID_FORK TEST_SETSID_DELAY_SECONDS
grep -Fq 'did not become verifiable' "$state_dir/setsid-late.log" || fail "Restart must time out before an unverified late session"
[ ! -e "$state_dir/kill.log" ] || fail "A late unverified session must not stop the old instance"
sleep 2
[ ! -e "$state_dir/app.log" ] || fail "A late unverified session must not execute app.sh after the parent exits"
[ -d "$proc_root/4242" ] || fail "A late unverified session must leave the old instance running"

echo "restart service tests passed"
