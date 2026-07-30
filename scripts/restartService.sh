#!/usr/bin/env bash
set -euo pipefail

# 生产服务生命周期脚本：仅处理受控的服务名，不接受任意 JAR 或任意命令参数。
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd -P)"
if [ -f "$SCRIPT_DIR/app.sh" ]; then
  DEFAULT_APP_ROOT="$SCRIPT_DIR"
else
  DEFAULT_APP_ROOT="$(cd "$SCRIPT_DIR/.." && pwd -P)"
fi

APP_ROOT="${SMART_APP_ROOT:-$DEFAULT_APP_ROOT}"
# 将工作目录规范为物理路径，避免 /var 与 /private/var 等等价路径导致进程归属误判。
if [ -d "$APP_ROOT" ]; then
  APP_ROOT="$(cd "$APP_ROOT" && pwd -P)"
fi
APP_SCRIPT="${SMART_APP_SCRIPT:-$APP_ROOT/app.sh}"
PROC_ROOT="${SMART_PROC_ROOT:-/proc}"
KILL_BIN="${SMART_KILL_BIN:-/bin/kill}"
SS_BIN="${SMART_SS_BIN:-ss}"
CRONTAB_BIN="${SMART_CRONTAB_BIN:-crontab}"
LOCK_DIR="${SMART_LOCK_DIR:-$APP_ROOT/.restart-service.lock}"
STOP_TIMEOUT_SECONDS="${SMART_STOP_TIMEOUT_SECONDS:-60}"
START_TIMEOUT_SECONDS="${SMART_START_TIMEOUT_SECONDS:-60}"
POLL_SECONDS="${SMART_POLL_SECONDS:-1}"

SERVICE_NAME=""
JAR_NAME=""
SERVICE_PORT=""
JAR_PATH=""
MATCHED_PIDS=()
MATCHED_START_TIMES=()
UNSTABLE_MATCHED_PIDS=()

die() {
  echo "ERROR: $*" >&2
  exit 1
}

usage() {
  cat <<'EOF'
用法：
  restartService.sh <service>
  restartService.sh {status|stop|restart} <service>

服务：gateway auth push upms dispatcher platform app data schedule

stop/restart 前必须先核查当前用户、root/system cron 与 systemd watchdog，
并仅在确认所有 watchdog 已停用后设置 SMART_WATCHDOG_STOP_CONFIRMED=1。
EOF
}

configure_service() {
  SERVICE_NAME="$1"
  case "$SERVICE_NAME" in
    gateway) JAR_NAME="smart-gateway.jar"; SERVICE_PORT=9990 ;;
    auth) JAR_NAME="smart-auth.jar"; SERVICE_PORT=3000 ;;
    push) JAR_NAME="smart-push-biz.jar"; SERVICE_PORT=6090 ;;
    upms) JAR_NAME="smart-upms-biz.jar"; SERVICE_PORT=4000 ;;
    dispatcher) JAR_NAME="smart-dispatcher-biz.jar"; SERVICE_PORT=8082 ;;
    platform) JAR_NAME="smart-platform-biz.jar"; SERVICE_PORT=6030 ;;
    app) JAR_NAME="smart-app-biz.jar"; SERVICE_PORT=6020 ;;
    data) JAR_NAME="smart-data-biz.jar"; SERVICE_PORT=6060 ;;
    schedule) JAR_NAME="smart-schedule.jar"; SERVICE_PORT=7070 ;;
    *) die "Unsupported service: $SERVICE_NAME" ;;
  esac
  JAR_PATH="$APP_ROOT/smart-jar/$JAR_NAME"
}

require_valid_runtime() {
  [ -x "$APP_SCRIPT" ] || die "Missing executable app.sh: $APP_SCRIPT"
  [ -d "$PROC_ROOT" ] || die "Missing process root: $PROC_ROOT"
  case "$STOP_TIMEOUT_SECONDS" in
    ''|*[!0-9]*) die "SMART_STOP_TIMEOUT_SECONDS must be a positive integer" ;;
  esac
  case "$START_TIMEOUT_SECONDS" in
    ''|*[!0-9]*) die "SMART_START_TIMEOUT_SECONDS must be a positive integer" ;;
  esac
  case "$POLL_SECONDS" in
    ''|*[!0-9]*) die "SMART_POLL_SECONDS must be a positive integer" ;;
  esac
  [ "$STOP_TIMEOUT_SECONDS" -ge 1 ] || die "SMART_STOP_TIMEOUT_SECONDS must be at least 1"
  [ "$START_TIMEOUT_SECONDS" -ge 1 ] || die "SMART_START_TIMEOUT_SECONDS must be at least 1"
  [ "$POLL_SECONDS" -ge 1 ] || die "SMART_POLL_SECONDS must be at least 1"
}

require_artifact() {
  [ -f "$JAR_PATH" ] || die "Missing service artifact: $JAR_PATH"
}

process_matches_service() {
  local pid="$1"
  local proc_dir="$PROC_ROOT/$pid"
  local cwd
  local argument
  local previous_argument=""
  local argument_index=0

  [ -r "$proc_dir/cmdline" ] || return 1
  # 只接受真实 Java -jar 进程，避免同目录的包装程序带同名参数时被误停止。
  # 先检查命令行，避免对机器上的每个进程解析 /proc/<pid>/cwd。
  # 某些网络工作目录的符号链接解析会较慢；只有命中精确 JAR 时才校验其工作目录。
  while IFS= read -r -d '' argument; do
    if [ "$argument_index" -eq 0 ]; then
      case "${argument##*/}" in
        java) ;;
        *) return 1 ;;
      esac
    elif [ "$previous_argument" = "-jar" ] && { [ "$argument" = "$JAR_PATH" ] || [ "$argument" = "smart-jar/$JAR_NAME" ]; }; then
      # /proc/<pid>/cwd 是目录链接；使用 cd/pwd 同时兼容生产 Linux 与本地测试环境。
      cwd="$(cd "$proc_dir/cwd" 2>/dev/null && pwd -P || true)"
      [ "$cwd" = "$APP_ROOT" ] && return 0
    fi
    previous_argument="$argument"
    argument_index=$((argument_index + 1))
  done <"$proc_dir/cmdline"

  return 1
}

process_start_time() {
  local pid="$1"
  local stat_line
  local stat_after_command
  local -a stat_fields=()

  [ -r "$PROC_ROOT/$pid/stat" ] || return 1
  IFS= read -r stat_line <"$PROC_ROOT/$pid/stat" || return 1
  # stat 的第二列 comm 被括号包围且可能带空格；剥离到最后一个 ") " 后再取第 22 列。
  stat_after_command="${stat_line##*) }"
  read -r -a stat_fields <<<"$stat_after_command"
  [ "${#stat_fields[@]}" -ge 20 ] || return 1
  case "${stat_fields[19]}" in
    ''|*[!0-9]*) return 1 ;;
  esac
  printf '%s\n' "${stat_fields[19]}"
}

collect_service_pids() {
  local proc_dir
  local pid
  local start_time
  local confirmed_start_time

  MATCHED_PIDS=()
  MATCHED_START_TIMES=()
  UNSTABLE_MATCHED_PIDS=()
  for proc_dir in "$PROC_ROOT"/[0-9]*; do
    [ -d "$proc_dir" ] || continue
    pid="${proc_dir##*/}"
    if process_matches_service "$pid"; then
      # 采集时前后各校验一次命令行/工作目录，并确保 starttime 未变化；PID 复用不作为同一实例。
      if start_time="$(process_start_time "$pid")" \
        && process_matches_service "$pid" \
        && confirmed_start_time="$(process_start_time "$pid")" \
        && [ "$start_time" = "$confirmed_start_time" ]; then
        MATCHED_PIDS+=("$pid")
        MATCHED_START_TIMES+=("$start_time")
      else
        # 无法证明身份稳定时必须拒绝，不把仍在运行的目标误报为“已停止”。
        UNSTABLE_MATCHED_PIDS+=("$pid")
      fi
    fi
  done
}

single_service_pid() {
  collect_service_pids
  if [ "${#UNSTABLE_MATCHED_PIDS[@]}" -gt 0 ]; then
    echo "ERROR: process identity cannot be established for $JAR_NAME: ${UNSTABLE_MATCHED_PIDS[*]}" >&2
    return 2
  fi
  case "${#MATCHED_PIDS[@]}" in
    0) return 1 ;;
    1) printf '%s\n' "${MATCHED_PIDS[0]}"; return 0 ;;
    # 不在此处调用 die：调用方若通过命令替换取 PID，die 只会退出子 shell，
    # 从而把“多实例”误判为“未运行”。
    *) echo "ERROR: Multiple processes match $JAR_NAME: ${MATCHED_PIDS[*]}" >&2; return 2 ;;
  esac
}

single_service_identity() {
  collect_service_pids
  if [ "${#UNSTABLE_MATCHED_PIDS[@]}" -gt 0 ]; then
    echo "ERROR: process identity cannot be established for $JAR_NAME: ${UNSTABLE_MATCHED_PIDS[*]}" >&2
    return 2
  fi
  case "${#MATCHED_PIDS[@]}" in
    0) return 1 ;;
    1) printf '%s %s\n' "${MATCHED_PIDS[0]}" "${MATCHED_START_TIMES[0]}"; return 0 ;;
    *) echo "ERROR: Multiple processes match $JAR_NAME: ${MATCHED_PIDS[*]}" >&2; return 2 ;;
  esac
}

port_is_occupied() {
  "$SS_BIN" -ltnH 2>/dev/null | awk -v port="$SERVICE_PORT" '
    $4 ~ (":" port "$") { found = 1 }
    END { exit(found ? 0 : 1) }
  '
}

port_is_listening_by_pid() {
  local pid="$1"

  "$SS_BIN" -ltnpH 2>/dev/null | awk -v port="$SERVICE_PORT" -v pid="$pid" '
    $4 ~ (":" port "$") && $0 ~ ("pid=" pid "([,)]|$)") { found = 1 }
    END { exit(found ? 0 : 1) }
  '
}

watchdog_is_configured() {
  # 此命令只能读取当前用户 crontab；系统级来源必须由人工预检并显式确认。
  "$CRONTAB_BIN" -l 2>/dev/null | grep -Eq '(^|[[:space:]])[^[:space:]]*checksmart\.sh([[:space:]]|$)'
}

watchdog_is_running() {
  local proc_dir
  local cmdline

  for proc_dir in "$PROC_ROOT"/[0-9]*; do
    [ -r "$proc_dir/cmdline" ] || continue
    cmdline="$(tr '\000' ' ' <"$proc_dir/cmdline")"
    if [[ "$cmdline" == *checksmart.sh* ]]; then
      return 0
    fi
  done
  return 1
}

require_watchdog_stopped() {
  if [ "${SMART_WATCHDOG_STOP_CONFIRMED:-}" != "1" ]; then
    die "watchdog stop is not explicitly confirmed; inspect user/root/system cron and systemd, then set SMART_WATCHDOG_STOP_CONFIRMED=1"
  fi
  if watchdog_is_configured || watchdog_is_running; then
    die "watchdog is configured or running; disable it before restarting $SERVICE_NAME"
  fi
}

acquire_lock() {
  if ! mkdir "$LOCK_DIR" 2>/dev/null; then
    die "Another restartService.sh process is already running: $LOCK_DIR"
  fi
  trap 'rmdir "$LOCK_DIR" 2>/dev/null || true' EXIT
}

wait_for_stop() {
  local deadline=$((SECONDS + STOP_TIMEOUT_SECONDS))
  local result

  while [ "$SECONDS" -lt "$deadline" ]; do
    if single_service_pid >/dev/null; then
      :
    else
      result=$?
      [ "$result" -eq 1 ] && return 0
      return "$result"
    fi
    sleep "$POLL_SECONDS"
  done

  if single_service_pid >/dev/null; then
    return 1
  fi
  result=$?
  [ "$result" -eq 1 ] && return 0
  return "$result"
}

wait_for_pid_stop() {
  local pid="$1"
  local deadline=$((SECONDS + STOP_TIMEOUT_SECONDS))

  while [ "$SECONDS" -lt "$deadline" ]; do
    if ! process_matches_service "$pid"; then
      return 0
    fi
    sleep "$POLL_SECONDS"
  done

  ! process_matches_service "$pid"
}

wait_for_start() {
  local deadline=$((SECONDS + START_TIMEOUT_SECONDS))
  local pid
  local result

  while [ "$SECONDS" -lt "$deadline" ]; do
    if pid="$(single_service_pid)"; then
      if port_is_listening_by_pid "$pid"; then
        printf '%s\n' "$pid"
        return 0
      fi
    else
      result=$?
      [ "$result" -eq 2 ] && return 2
    fi
    sleep "$POLL_SECONDS"
  done

  if pid="$(single_service_pid)"; then
    if port_is_listening_by_pid "$pid"; then
      printf '%s\n' "$pid"
      return 0
    fi
    return 1
  fi
  return "$?"
}

show_status() {
  local pid
  local result

  if pid="$(single_service_pid)"; then
    :
  else
    result=$?
    [ "$result" -eq 1 ] && echo "$SERVICE_NAME is not running ($JAR_NAME)"
    return "$result"
  fi

  if ! port_is_listening_by_pid "$pid"; then
    echo "$SERVICE_NAME process $pid is running, but port $SERVICE_PORT is not listening" >&2
    return 1
  fi

  echo "$SERVICE_NAME is running: pid=$pid jar=$JAR_NAME port=$SERVICE_PORT"
}

stop_service() {
  local expected_pid="${1:-}"
  local expected_start_time="${2:-}"
  local identity
  local pid
  local start_time
  local confirmed_start_time
  local result

  if identity="$(single_service_identity)"; then
    read -r pid start_time <<<"$identity"
    :
  else
    result=$?
    if [ "$result" -eq 1 ]; then
      if [ -n "$expected_pid" ]; then
        die "$SERVICE_NAME pid=$expected_pid is no longer running before SIGTERM; refusing restart"
      fi
      echo "$SERVICE_NAME is already stopped ($JAR_NAME)"
      return 0
    fi
    return "$result"
  fi

  # restart 传入首次识别到的 PID，要求我们仍将终止这个精确进程。
  # 若它已自然退出或被其他动作替换，拒绝启动新实例，避免意外改变节点拓扑。
  if [ -n "$expected_pid" ] && [ "$pid" != "$expected_pid" ]; then
    die "$SERVICE_NAME process changed from pid=$expected_pid to pid=$pid; refusing restart"
  fi
  if [ -n "$expected_start_time" ] && [ "$start_time" != "$expected_start_time" ]; then
    die "$SERVICE_NAME process identity changed for pid=$pid before SIGTERM; refusing restart"
  fi

  # 在真正发送信号前再次确认同一 PID 仍是同一个 Java -jar 实例，避免 PID 复用误杀。
  if ! process_matches_service "$pid"; then
    die "$SERVICE_NAME process changed before SIGTERM; refusing restart"
  fi
  if confirmed_start_time="$(process_start_time "$pid")"; then
    :
  else
    die "$SERVICE_NAME process identity cannot be read before SIGTERM; refusing restart"
  fi
  if [ "$confirmed_start_time" != "$start_time" ]; then
    die "$SERVICE_NAME process identity changed for pid=$pid before SIGTERM; refusing restart"
  fi

  echo "Stopping $SERVICE_NAME (pid=$pid) with SIGTERM"
  if "$KILL_BIN" -TERM "$pid"; then
    :
  else
    die "Failed to send SIGTERM to $SERVICE_NAME pid=$pid; refusing restart"
  fi

  if [ -n "$expected_pid" ]; then
    if wait_for_pid_stop "$pid"; then
      :
    else
      die "$SERVICE_NAME pid=$pid did not stop within ${STOP_TIMEOUT_SECONDS}s; refusing to send SIGKILL automatically"
    fi
  elif wait_for_stop; then
    :
  else
    result=$?
    [ "$result" -eq 2 ] && return 2
    die "$SERVICE_NAME did not stop within ${STOP_TIMEOUT_SECONDS}s; refusing to send SIGKILL automatically"
  fi
}

start_service() {
  local result

  if single_service_pid >/dev/null; then
    die "$SERVICE_NAME is already running; refusing to start a duplicate instance"
  else
    result=$?
    [ "$result" -eq 2 ] && return 2
  fi
  if port_is_occupied; then
    die "Port $SERVICE_PORT is already listening while $SERVICE_NAME is absent; investigate before starting"
  fi

  echo "Starting $SERVICE_NAME from $JAR_PATH"
  "$APP_SCRIPT" start "smart-jar/$JAR_NAME"
  if wait_for_start >/dev/null; then
    :
  else
    result=$?
    [ "$result" -eq 2 ] && return 2
    die "$SERVICE_NAME did not become ready within ${START_TIMEOUT_SECONDS}s"
  fi
}

restart_service() {
  local identity
  local pid
  local start_time
  local result

  # restart 只接受已存在且唯一的目标进程；缺失服务必须显式排障，不能被误拉起。
  if identity="$(single_service_identity)"; then
    read -r pid start_time <<<"$identity"
    :
  else
    result=$?
    if [ "$result" -eq 1 ]; then
      die "$SERVICE_NAME is not running; refusing restart because start must be an explicit, investigated operation"
    fi
    return "$result"
  fi

  stop_service "$pid" "$start_time"
  start_service
}

main() {
  local action
  local service

  case "${1:-}" in
    -h|--help)
      usage
      return 0
      ;;
  esac

  case "$#" in
    0)
      read -r -t 20 -p "请输入需要重启的服务名称：" service || die "No service selected within 20 seconds"
      action="restart"
      ;;
    1)
      action="restart"
      service="$1"
      ;;
    2)
      action="$1"
      service="$2"
      ;;
    *)
      usage >&2
      return 1
      ;;
  esac

  configure_service "$service"
  require_valid_runtime

  case "$action" in
    status)
      show_status
      ;;
    stop)
      require_watchdog_stopped
      acquire_lock
      stop_service
      echo "$SERVICE_NAME is stopped ($JAR_NAME)"
      ;;
    restart)
      require_artifact
      require_watchdog_stopped
      acquire_lock
      restart_service
      show_status
      ;;
    *)
      usage >&2
      return 1
      ;;
  esac
}

if [ "${BASH_SOURCE[0]}" = "$0" ]; then
  main "$@"
fi
