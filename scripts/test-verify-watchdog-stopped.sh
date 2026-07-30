#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd -P)"
script="$repo_root/scripts/verify-watchdog-stopped.sh"

fail() {
  echo "FAIL: $*" >&2
  exit 1
}

assert_contains() {
  local file="$1"
  local expected="$2"

  grep -Fq -- "$expected" "$file" || fail "Expected '$expected' in $file"
}

run_expect_failure() {
  local output_file="$1"
  shift

  if "$@" >"$output_file" 2>&1; then
    fail "Command unexpectedly succeeded: $*"
  fi
}

test_dir="$(mktemp -d "${TMPDIR:-/tmp}/verify-watchdog-test.XXXXXX")"
trap 'rm -rf "$test_dir"' EXIT

app_root="$test_dir/app"
proc_root="$test_dir/proc"
cron_root="$test_dir/cron"
systemd_root="$test_dir/systemd"
service_home="$test_dir/service-home"
state_dir="$test_dir/state"
fake_bin="$test_dir/bin"
fake_crontab="$fake_bin/crontab"
mkdir -p "$app_root" "$proc_root" "$cron_root" "$systemd_root" "$service_home" "$state_dir" "$fake_bin"
touch "$app_root/119checksmart.sh"

cat >"$fake_crontab" <<'SH'
#!/usr/bin/env bash
set -euo pipefail

user=""
while [[ "$#" -gt 0 ]]; do
  case "$1" in
    -u)
      user="$2"
      shift 2
      ;;
    -l)
      shift
      ;;
    *)
      echo "unexpected crontab argument: $1" >&2
      exit 2
      ;;
  esac
done

if [ -f "$TEST_WATCHDOG_STATE/crontab-error-$user" ]; then
  echo "cannot inspect $user crontab" >&2
  exit 2
fi
if [ -f "$TEST_WATCHDOG_STATE/crontab-$user" ]; then
  cat "$TEST_WATCHDOG_STATE/crontab-$user"
  exit 0
fi

echo "no crontab for $user" >&2
exit 1
SH
chmod +x "$fake_crontab"

run_verifier() {
  local output_file="$1"
  local direct_config_path="${2:-}"

  TEST_WATCHDOG_STATE="$state_dir" TEST_WATCHDOG_DIRECT_CONFIG_PATH="$direct_config_path" bash -c '
    source "$1"
    require_root() { :; }
    PROC_ROOT="$2"
    CRONTAB_BIN="$3"
    WATCHDOG_SERVICE_USER=yuto
    WATCHDOG_SERVICE_HOME="$6"
    WATCHDOG_CONFIG_PATHS=("$4" "$5")
    if [ -n "${TEST_WATCHDOG_DIRECT_CONFIG_PATH:-}" ]; then
      WATCHDOG_CONFIG_PATHS+=("$TEST_WATCHDOG_DIRECT_CONFIG_PATH")
    fi
    verify_watchdog_stopped "$7"
  ' bash "$script" "$proc_root" "$fake_crontab" "$cron_root" "$systemd_root" "$service_home" "$app_root" >"$output_file" 2>&1
}

[ -f "$script" ] || fail "Expected watchdog verifier at $script"

# 预检自身和 sudo 父进程的命令行都包含 verify-watchdog-stopped.sh；它们不是自动
# 拉起服务的 watchdog，不能让真实 stop/restart 永远被自身误判阻断。
mkdir -p "$proc_root/4001" "$proc_root/4002"
printf 'bash\0%s\0' "$script" >"$proc_root/4001/cmdline"
printf 'sudo\0--\0%s\0' "$script" >"$proc_root/4002/cmdline"
run_verifier "$state_dir/clear.log" || fail "A clear watchdog preflight should succeed"

printf '%s\n' '0 * * * * /usr/sbin/ntpdate 10.0.20.5' >"$state_dir/crontab-root"
printf '%s\n' '0 * * * * /usr/bin/true' >"$state_dir/crontab-yuto"
run_verifier "$state_dir/unrelated-crontab.log" || fail "An unrelated nonempty crontab must not block the preflight"
rm -f "$state_dir/crontab-root" "$state_dir/crontab-yuto"

direct_config="$test_dir/unrelated.conf"
printf '%s\n' '[Service]' 'ExecStart=/usr/bin/true' >"$direct_config"
run_verifier "$state_dir/unrelated-direct-config.log" "$direct_config" || fail "An unrelated direct config file must not block the preflight"
rm -f "$direct_config"

printf '%s\n' "@reboot $app_root/119checksmart.sh" >"$state_dir/crontab-root"
run_expect_failure "$state_dir/root-cron.log" run_verifier "$state_dir/root-cron.log"
assert_contains "$state_dir/root-cron.log" "root crontab"
rm -f "$state_dir/crontab-root"

printf '%s\n' "*/10 * * * * $app_root/119checksmart.sh;/usr/bin/true" >"$state_dir/crontab-root"
run_expect_failure "$state_dir/inline-separator-cron.log" run_verifier "$state_dir/inline-separator-cron.log"
assert_contains "$state_dir/inline-separator-cron.log" "root crontab"
rm -f "$state_dir/crontab-root"

printf '%s\n' "*/10 * * * * $app_root/119checksmart.sh&" >"$state_dir/crontab-root"
run_expect_failure "$state_dir/background-separator-cron.log" run_verifier "$state_dir/background-separator-cron.log"
assert_contains "$state_dir/background-separator-cron.log" "root crontab"
rm -f "$state_dir/crontab-root"

printf '%s\n' "*/10 * * * * $app_root/119checksmart.sh" >"$state_dir/crontab-yuto"
run_expect_failure "$state_dir/service-user-cron.log" run_verifier "$state_dir/service-user-cron.log"
assert_contains "$state_dir/service-user-cron.log" "yuto crontab"
rm -f "$state_dir/crontab-yuto"

printf '%s\n' '[Service]' "ExecStart=$app_root/119checksmart.sh" >"$systemd_root/119-watchdog.service"
run_expect_failure "$state_dir/systemd.log" run_verifier "$state_dir/systemd.log"
assert_contains "$state_dir/systemd.log" "watchdog configuration"
rm -f "$systemd_root/119-watchdog.service"

printf '%s\n' '[Service]' "ExecStart=\"$app_root/119checksmart.sh\"" >"$systemd_root/quoted-watchdog.service"
run_expect_failure "$state_dir/quoted-systemd.log" run_verifier "$state_dir/quoted-systemd.log"
assert_contains "$state_dir/quoted-systemd.log" "watchdog configuration"
rm -f "$systemd_root/quoted-watchdog.service"

# 主机硬件 watchdog 不是 smart 自动拉起器；不能因为通用 OS 服务名就永久阻断发布。
printf '%s\n' '[Service]' 'ExecStart=/usr/sbin/watchdog' >"$systemd_root/host-watchdog.service"
run_verifier "$state_dir/host-watchdog.log" || fail "A host watchdog outside the app root must not block the preflight"
rm -f "$systemd_root/host-watchdog.service"

printf '%s\n' '[Service]' "ExecStart=$app_root/watchdog.sh" >"$systemd_root/app-watchdog.service"
run_expect_failure "$state_dir/app-watchdog-config.log" run_verifier "$state_dir/app-watchdog-config.log"
assert_contains "$state_dir/app-watchdog-config.log" "watchdog configuration"
rm -f "$systemd_root/app-watchdog.service"

mkdir -p "$service_home/.config/systemd/user"
printf '%s\n' '[Service]' "ExecStart=$app_root/119checksmart.sh" >"$service_home/.config/systemd/user/119-watchdog.service"
run_expect_failure "$state_dir/user-systemd.log" run_verifier "$state_dir/user-systemd.log"
assert_contains "$state_dir/user-systemd.log" "watchdog configuration"
rm -f "$service_home/.config/systemd/user/119-watchdog.service"

printf '%s\n' '[Service]' "ExecStart=$app_root-backup/watchdog.sh" >"$systemd_root/prefix-watchdog.service"
run_verifier "$state_dir/prefix-watchdog.log" || fail "A watchdog below an app-root prefix sibling must not block the preflight"
rm -f "$systemd_root/prefix-watchdog.service"

mkdir -p "$proc_root/5151"
printf 'bash\0%s/119checksmart.sh\0' "$app_root" >"$proc_root/5151/cmdline"
run_expect_failure "$state_dir/running.log" run_verifier "$state_dir/running.log"
assert_contains "$state_dir/running.log" "watchdog process"
rm -rf "$proc_root/5151"

mkdir -p "$proc_root/5152"
printf 'watchdog\0' >"$proc_root/5152/cmdline"
run_verifier "$state_dir/generic-running.log" || fail "A host watchdog process outside the app root must not block the preflight"
rm -rf "$proc_root/5152"

mkdir -p "$proc_root/5153"
printf 'bash\0%s/watchdog.sh\0' "$app_root" >"$proc_root/5153/cmdline"
run_expect_failure "$state_dir/app-watchdog-running.log" run_verifier "$state_dir/app-watchdog-running.log"
assert_contains "$state_dir/app-watchdog-running.log" "watchdog process"
rm -rf "$proc_root/5153"

mkdir -p "$proc_root/5154"
printf 'bash\0%s-backup/watchdog.sh\0' "$app_root" >"$proc_root/5154/cmdline"
run_verifier "$state_dir/prefix-watchdog-running.log" || fail "A watchdog below an app-root prefix sibling process must not block the preflight"
rm -rf "$proc_root/5154"

touch "$state_dir/crontab-error-root"
run_expect_failure "$state_dir/root-cron-error.log" run_verifier "$state_dir/root-cron-error.log"
assert_contains "$state_dir/root-cron-error.log" "Unable to inspect root crontab"

echo "watchdog verifier tests passed"
