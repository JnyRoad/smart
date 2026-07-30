#!/usr/bin/env bash
set -euo pipefail

# 该脚本只能由 root 执行。它用于在停止或重启前检查 cron、systemd 配置与运行进程，
# 不能用调用方设置的环境变量替代真实检查结果。
PROC_ROOT="/proc"
CRONTAB_BIN="crontab"
# 只阻断实际应用目录中的 checksmart / watchdog 启动器。主机硬件 watchdog、watchdogd
# 以及目录前缀相似的无关服务不能导致发布永远被拒绝。
APP_WATCHDOG_LAUNCHER_PATTERN=""
WATCHDOG_APP_ROOT=""
WATCHDOG_SERVICE_USER=""
WATCHDOG_SERVICE_HOME=""
WATCHDOG_CONFIG_PATHS=(
  /etc/crontab
  /etc/anacrontab
  /etc/cron.d
  /etc/cron.hourly
  /etc/cron.daily
  /etc/cron.weekly
  /etc/cron.monthly
  /var/spool/cron
  /var/spool/cron/crontabs
  /etc/systemd/system
  /etc/systemd/system.control
  /usr/lib/systemd/system
  /lib/systemd/system
  /run/systemd/system
  /run/systemd/system.control
  /run/systemd/generator
  /run/systemd/generator.early
  /run/systemd/generator.late
  /etc/rc.local
  /etc/rc.d/rc.local
  /etc/init.d
  /etc/rc.d/init.d
  /etc/rc0.d
  /etc/rc1.d
  /etc/rc2.d
  /etc/rc3.d
  /etc/rc4.d
  /etc/rc5.d
  /etc/rc6.d
  /etc/supervisord.conf
  /etc/supervisor
  /etc/monitrc
  /etc/monit.d
)

die() {
  echo "ERROR: $*" >&2
  exit 1
}

usage() {
  cat <<'EOF'
Usage: verify-watchdog-stopped.sh --app-root DIR --service-user USER

Runs a privileged, fail-closed preflight before a controlled service stop or
restart. It rejects active or configured checksmart scripts and app-root
watchdog launchers found in the service user's or root's crontab, standard
cron/systemd locations, or the process table.
EOF
}

require_root() {
  [ "$(id -u)" -eq 0 ] || die "watchdog verification must run with root privileges"
}

require_command() {
  command -v "$1" >/dev/null 2>&1 || die "Missing required command: $1"
}

build_app_watchdog_launcher_pattern() {
  local escaped_app_root

  # app root 来自受控参数但仍须转义 ERE 元字符；随后强制要求紧跟目录分隔符，避免
  # /home/yuto/smart-backup 之类的前缀相似路径被误认为本应用。
  if escaped_app_root="$(printf '%s' "$WATCHDOG_APP_ROOT" | sed 's/[][\\.^$*+?{}|()]/\\&/g')"; then
    :
  else
    die "Unable to prepare watchdog app-root matcher"
  fi
  # cron 中的命令可紧接 ;、&、管道或重定向等 shell 分隔符；这些符号同样说明
  # 启动器路径已经结束，不能成为漏检的绕过方式。
  APP_WATCHDOG_LAUNCHER_PATTERN="${escaped_app_root}/[^[:space:]\\\"']*(checksmart|watchdog)(\\.sh)?([[:space:]\\\"';&|()<>]|$)"
}

scan_text() {
  local source_name="$1"
  local text="$2"
  local result

  if printf '%s\n' "$text" | grep -Eqi -- "$APP_WATCHDOG_LAUNCHER_PATTERN"; then
    die "watchdog configuration found in $source_name"
  else
    result=$?
  fi
  [ "$result" -eq 1 ] || die "Unable to inspect watchdog configuration text: $source_name"
}

scan_crontab() {
  local user="$1"
  local output

  if output="$(LC_ALL=C "$CRONTAB_BIN" -u "$user" -l 2>&1)"; then
    scan_text "$user crontab" "$output"
    return 0
  fi

  # crontab 在不存在用户 crontab 时以非零退出；这是唯一允许的非零结果。
  if [[ "$output" == *"no crontab for $user"* ]]; then
    return 0
  fi
  die "Unable to inspect $user crontab"
}

scan_config_file() {
  local path="$1"
  local result

  [ -e "$path" ] || return 0
  [ -r "$path" ] || die "Unable to read watchdog configuration path: $path"
  if grep -Eqi -- "$APP_WATCHDOG_LAUNCHER_PATTERN" "$path"; then
    die "watchdog configuration found in $path"
  else
    result=$?
  fi
  [ "$result" -eq 1 ] || die "Unable to inspect watchdog configuration path: $path"
}

scan_config_path() {
  local path="$1"
  local match
  local result

  [ -e "$path" ] || return 0
  [ -r "$path" ] || die "Unable to read watchdog configuration path: $path"
  if [ -f "$path" ]; then
    scan_config_file "$path"
    return 0
  fi
  if [ ! -d "$path" ]; then
    die "Unsupported watchdog configuration path: $path"
  fi

  # 不跟随配置目录中的符号链接，避免特权预检被链接引向无关或无限递归的目录。
  # systemd 的真实 unit 所在目录会逐一列在 WATCHDOG_CONFIG_PATHS 中。
  if match="$(grep -rIlE -- "$APP_WATCHDOG_LAUNCHER_PATTERN" "$path" 2>/dev/null)"; then
    if [ -n "$match" ]; then
      die "watchdog configuration found in $match"
    fi
  else
    result=$?
    [ "$result" -eq 1 ] || die "Unable to inspect watchdog configuration path: $path"
  fi

}

scan_running_processes() {
  local proc_dir
  local cmdline
  local result

  for proc_dir in "$PROC_ROOT"/[0-9]*; do
    [ -d "$proc_dir" ] || continue
    # 进程可能在枚举与读取之间退出；不存在则跳过，存在但不可读则拒绝继续。
    [ -e "$proc_dir/cmdline" ] || continue
    [ -r "$proc_dir/cmdline" ] || die "Unable to inspect watchdog process: ${proc_dir##*/}"
    if ! cmdline="$(tr '\000' ' ' <"$proc_dir/cmdline")"; then
      die "Unable to inspect watchdog process: ${proc_dir##*/}"
    fi
    if printf '%s\n' "$cmdline" | grep -Eqi -- "$APP_WATCHDOG_LAUNCHER_PATTERN"; then
      die "watchdog process is running: ${proc_dir##*/}"
    else
      result=$?
    fi
    [ "$result" -eq 1 ] || die "Unable to inspect watchdog process: ${proc_dir##*/}"
  done
}

append_service_user_systemd_paths() {
  local passwd_entry
  local service_home

  if [ -n "$WATCHDOG_SERVICE_HOME" ]; then
    service_home="$WATCHDOG_SERVICE_HOME"
  else
    require_command getent
    if passwd_entry="$(getent passwd "$WATCHDOG_SERVICE_USER")"; then
      :
    else
      die "Unable to resolve home directory for watchdog service user: $WATCHDOG_SERVICE_USER"
    fi
    service_home="$(printf '%s\n' "$passwd_entry" | awk -F: 'NR == 1 { print $6; exit }')"
  fi

  [ -n "$service_home" ] || die "Missing home directory for watchdog service user: $WATCHDOG_SERVICE_USER"
  WATCHDOG_CONFIG_PATHS+=(
    "$service_home/.config/systemd/user"
    "$service_home/.local/share/systemd/user"
  )
}

verify_watchdog_stopped() {
  local app_root="$1"
  local path

  require_root
  [ -d "$app_root" ] || die "Missing app root for watchdog verification: $app_root"
  WATCHDOG_APP_ROOT="$app_root"
  [[ "$WATCHDOG_SERVICE_USER" =~ ^[A-Za-z_][A-Za-z0-9_-]*$ ]] || die "Invalid watchdog service user: $WATCHDOG_SERVICE_USER"
  require_command "$CRONTAB_BIN"
  require_command grep
  require_command tr
  require_command awk
  require_command sed
  build_app_watchdog_launcher_pattern

  scan_crontab root
  if [ "$WATCHDOG_SERVICE_USER" != "root" ]; then
    scan_crontab "$WATCHDOG_SERVICE_USER"
  fi
  append_service_user_systemd_paths
  for path in "${WATCHDOG_CONFIG_PATHS[@]}"; do
    scan_config_path "$path"
  done
  scan_running_processes
}

main() {
  local app_root=""

  while [[ "$#" -gt 0 ]]; do
    case "$1" in
      --app-root)
        [[ "$#" -ge 2 ]] || die "Missing value for --app-root"
        app_root="$2"
        shift 2
        ;;
      --service-user)
        [[ "$#" -ge 2 ]] || die "Missing value for --service-user"
        WATCHDOG_SERVICE_USER="$2"
        shift 2
        ;;
      -h|--help)
        usage
        return 0
        ;;
      *)
        usage >&2
        return 1
        ;;
    esac
  done

  [ -n "$app_root" ] || die "--app-root is required"
  [ -n "$WATCHDOG_SERVICE_USER" ] || die "--service-user is required"
  app_root="$(cd "$app_root" && pwd -P)"
  verify_watchdog_stopped "$app_root"
  echo "watchdog preflight passed for $app_root"
}

if [ "${BASH_SOURCE[0]}" = "$0" ]; then
  main "$@"
fi
