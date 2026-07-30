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
SETSID_BIN="${SMART_SETSID_BIN:-setsid}"
BASH_BIN="${SMART_BASH_BIN:-${BASH:-bash}}"
# 只通过脚本所在目录的受控检查器做特权预检，不接受环境变量的“已确认”声明。
SUDO_BIN="$(command -v sudo 2>/dev/null || true)"
WATCHDOG_CHECKER="$SCRIPT_DIR/verify-watchdog-stopped.sh"
LOCK_DIR="${SMART_LOCK_DIR:-$APP_ROOT/.restart-service.lock}"
STOP_TIMEOUT_SECONDS="${SMART_STOP_TIMEOUT_SECONDS:-60}"
START_TIMEOUT_SECONDS="${SMART_START_TIMEOUT_SECONDS:-60}"
START_LAUNCH_TIMEOUT_SECONDS="${SMART_START_LAUNCH_TIMEOUT_SECONDS:-30}"
POLL_SECONDS="${SMART_POLL_SECONDS:-1}"

SERVICE_NAME=""
JAR_NAME=""
SERVICE_PORT=""
JAR_PATH=""
MATCHED_PIDS=()
MATCHED_START_TIMES=()
UNSTABLE_MATCHED_PIDS=()
START_SESSION_FILE=""
START_SESSION_ID=""
START_COMPLETION_FILE=""
START_DECISION_FILE=""
START_GATE_TOKEN=""
SESSION_PIDS=()
SESSION_START_TIMES=()
UNSTABLE_SESSION_PIDS=()

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

stop/restart 前会通过 sudo 执行 root watchdog 预检；预检无法完成、发现 cron、
systemd 或运行中的 watchdog 时，操作会拒绝执行。

若直接以 root 登录运行（没有 sudo 调用方），必须显式设置
SMART_ROOT_SERVICE_USER=<实际服务账号>，以便预检该账号的 cron 与用户级 systemd。

发布契约：
  从 release-artifacts/backend/<version>/runtime/ 直接运行本脚本，并保留同目录的
  verify-watchdog-stopped.sh 与 verify-release-runtime.sh。先以后者校验发布包和
  SMART_APP_ROOT；SMART_APP_ROOT 必须指向同时包含 app.sh 与 smart-jar/ 的实际服务
  目录，缺少任一文件时脚本会拒绝执行。
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
  case "$START_LAUNCH_TIMEOUT_SECONDS" in
    ''|*[!0-9]*) die "SMART_START_LAUNCH_TIMEOUT_SECONDS must be a positive integer" ;;
  esac
  case "$POLL_SECONDS" in
    ''|*[!0-9]*) die "SMART_POLL_SECONDS must be a positive integer" ;;
  esac
  [ "$STOP_TIMEOUT_SECONDS" -ge 1 ] || die "SMART_STOP_TIMEOUT_SECONDS must be at least 1"
  [ "$START_TIMEOUT_SECONDS" -ge 1 ] || die "SMART_START_TIMEOUT_SECONDS must be at least 1"
  [ "$START_LAUNCH_TIMEOUT_SECONDS" -ge 1 ] || die "SMART_START_LAUNCH_TIMEOUT_SECONDS must be at least 1"
  [ "$POLL_SECONDS" -ge 1 ] || die "SMART_POLL_SECONDS must be at least 1"
  command -v "$KILL_BIN" >/dev/null 2>&1 || die "Missing required kill command: $KILL_BIN"
  command -v "$SS_BIN" >/dev/null 2>&1 || die "Missing required socket inspection command: $SS_BIN"
  command -v "$SETSID_BIN" >/dev/null 2>&1 || die "Missing required session isolation command: $SETSID_BIN"
  command -v "$BASH_BIN" >/dev/null 2>&1 || die "Missing required bash command: $BASH_BIN"
  command -v mktemp >/dev/null 2>&1 || die "Missing required temporary-file command: mktemp"
  command -v mv >/dev/null 2>&1 || die "Missing required atomic-rename command: mv"
  "$SS_BIN" -ltnH >/dev/null 2>&1 || die "Cannot inspect listening sockets with: $SS_BIN"
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

process_session_id() {
  local pid="$1"
  local stat_line
  local stat_after_command
  local -a stat_fields=()

  [ -r "$PROC_ROOT/$pid/stat" ] || return 1
  IFS= read -r stat_line <"$PROC_ROOT/$pid/stat" || return 1
  # /proc/<pid>/stat 的第 6 列是 session；与 starttime 一样先处理可能含空格的 comm。
  stat_after_command="${stat_line##*) }"
  read -r -a stat_fields <<<"$stat_after_command"
  [ "${#stat_fields[@]}" -ge 4 ] || return 1
  case "${stat_fields[3]}" in
    ''|*[!0-9]*) return 1 ;;
  esac
  printf '%s\n' "${stat_fields[3]}"
}

process_parent_pid() {
  local pid="$1"
  local stat_line
  local stat_after_command
  local -a stat_fields=()

  [ -r "$PROC_ROOT/$pid/stat" ] || return 1
  IFS= read -r stat_line <"$PROC_ROOT/$pid/stat" || return 1
  # /proc/<pid>/stat 的第 4 列是父进程；与其他字段一样先去掉可能含空格的 comm。
  stat_after_command="${stat_line##*) }"
  read -r -a stat_fields <<<"$stat_after_command"
  [ "${#stat_fields[@]}" -ge 20 ] || return 1
  case "${stat_fields[1]}" in
    ''|*[!0-9]*) return 1 ;;
  esac
  printf '%s\n' "${stat_fields[1]}"
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
  local sockets

  if sockets="$("$SS_BIN" -ltnH 2>/dev/null)"; then
    :
  else
    return 2
  fi
  printf '%s\n' "$sockets" | awk -v port="$SERVICE_PORT" '
    $4 ~ (":" port "$") { found = 1 }
    END { exit(found ? 0 : 1) }
  '
}

port_is_listening_by_pid() {
  local pid="$1"
  local sockets

  if sockets="$("$SS_BIN" -ltnpH 2>/dev/null)"; then
    :
  else
    return 2
  fi
  printf '%s\n' "$sockets" | awk -v port="$SERVICE_PORT" -v pid="$pid" '
    $4 ~ (":" port "$") && $0 ~ ("pid=" pid "([,)]|$)") { found = 1 }
    END { exit(found ? 0 : 1) }
  '
}

require_watchdog_stopped() {
  local service_user

  [ -x "$WATCHDOG_CHECKER" ] || die "Missing executable watchdog verifier: $WATCHDOG_CHECKER"
  # 从 sudo 进入 root 的场景仍必须检查原服务账号的用户级 cron。非 root
  # 调用方不能借由伪造 SUDO_USER 改变预检对象；直接 root 登录则强制显式声明。
  if [ "$(id -u)" -eq 0 ]; then
    if [ -n "${SUDO_USER:-}" ]; then
      service_user="$SUDO_USER"
    elif [ -n "${SMART_ROOT_SERVICE_USER:-}" ]; then
      service_user="$SMART_ROOT_SERVICE_USER"
    else
      die "Direct root invocation requires SMART_ROOT_SERVICE_USER for watchdog verification"
    fi
  else
    service_user="$(id -un)"
  fi
  if [ "$(id -u)" -eq 0 ]; then
    if "$WATCHDOG_CHECKER" --app-root "$APP_ROOT" --service-user "$service_user"; then
      return 0
    fi
  else
    [ -n "$SUDO_BIN" ] || die "Missing sudo required for watchdog verification"
    if "$SUDO_BIN" -- "$WATCHDOG_CHECKER" --app-root "$APP_ROOT" --service-user "$service_user"; then
      return 0
    fi
  fi
  die "watchdog preflight failed; refusing to stop or restart $SERVICE_NAME"
}

acquire_lock() {
  if ! mkdir -m 700 "$LOCK_DIR" 2>/dev/null; then
    die "Another restartService.sh process is already running: $LOCK_DIR"
  fi
  START_SESSION_FILE="$LOCK_DIR/start-session-id"
  START_COMPLETION_FILE="$LOCK_DIR/start-session-result"
  START_DECISION_FILE="$LOCK_DIR/start-decision"
  trap cleanup_lock EXIT
}

cleanup_lock() {
  # 未获准启动的 wrapper 只接受精确的 go 决策；删除 pending/abort 文件等价于
  # 终止授权，因此脚本异常退出也不会在之后意外拉起 Java。
  rm -f -- "$START_SESSION_FILE" "$START_COMPLETION_FILE" "$START_DECISION_FILE" \
    "${START_DECISION_FILE}.tmp.$$" 2>/dev/null || true
  rmdir "$LOCK_DIR" 2>/dev/null || true
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
  else
    result=$?
  fi
  [ "$result" -eq 1 ] && return 0
  return "$result"
}

wait_for_pid_stop() {
  local pid="$1"
  local expected_start_time="$2"
  local current_start_time
  local deadline=$((SECONDS + STOP_TIMEOUT_SECONDS))

  while [ "$SECONDS" -lt "$deadline" ]; do
    if ! process_matches_service "$pid"; then
      return 0
    fi
    if ! current_start_time="$(process_start_time "$pid")" || [ "$current_start_time" != "$expected_start_time" ]; then
      # 旧实例已经消失，但同一 PID 可能被复用；不能把新实例当作已被本脚本安全停止。
      return 2
    fi
    sleep "$POLL_SECONDS"
  done

  if ! process_matches_service "$pid"; then
    return 0
  fi
  if ! current_start_time="$(process_start_time "$pid")" || [ "$current_start_time" != "$expected_start_time" ]; then
    return 2
  fi
  return 1
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
      else
        result=$?
        [ "$result" -eq 2 ] && return 2
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
    else
      result=$?
      [ "$result" -eq 2 ] && return 2
    fi
    return 1
  else
    return "$?"
  fi
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
    result=$?
    [ "$result" -eq 2 ] && die "Cannot inspect listening sockets for $SERVICE_NAME"
    echo "$SERVICE_NAME process $pid is running, but port $SERVICE_PORT is not listening" >&2
    return 1
  fi

  echo "$SERVICE_NAME is running: pid=$pid jar=$JAR_NAME port=$SERVICE_PORT"
}

terminate_service_identity() {
  local pid="$1"
  local expected_start_time="$2"
  local allow_disappearance="${3:-0}"
  local confirmed_start_time
  local result

  # 在真正发送信号前再次确认同一 PID 仍是同一个 Java -jar 实例，避免 PID 复用误杀。
  if ! process_matches_service "$pid"; then
    if [ "$allow_disappearance" = "1" ]; then
      echo "$SERVICE_NAME pid=$pid exited before SIGTERM during failed-start cleanup"
      return 0
    fi
    die "$SERVICE_NAME process changed before SIGTERM; refusing restart"
  fi
  if confirmed_start_time="$(process_start_time "$pid")"; then
    :
  else
    die "$SERVICE_NAME process identity cannot be read before SIGTERM; refusing restart"
  fi
  if [ "$confirmed_start_time" != "$expected_start_time" ]; then
    die "$SERVICE_NAME process identity changed for pid=$pid before SIGTERM; refusing restart"
  fi

  echo "Stopping $SERVICE_NAME (pid=$pid) with SIGTERM"
  if "$KILL_BIN" -TERM "$pid"; then
    :
  else
    die "Failed to send SIGTERM to $SERVICE_NAME pid=$pid; refusing restart"
  fi

  if wait_for_pid_stop "$pid" "$expected_start_time"; then
    return 0
  else
    result=$?
  fi
  if [ "$result" -eq 2 ]; then
    die "$SERVICE_NAME process identity changed while waiting for SIGTERM; refusing to send SIGKILL automatically"
  fi
  die "$SERVICE_NAME pid=$pid did not stop within ${STOP_TIMEOUT_SECONDS}s; refusing to send SIGKILL automatically"
}

stop_service() {
  local expected_pid="${1:-}"
  local expected_start_time="${2:-}"
  local allow_expected_pid_disappearance="${3:-0}"
  local identity
  local pid
  local start_time
  local result

  if identity="$(single_service_identity)"; then
    read -r pid start_time <<<"$identity"
    :
  else
    result=$?
    if [ "$result" -eq 1 ]; then
      if [ -n "$expected_pid" ]; then
        if [ "$allow_expected_pid_disappearance" = "1" ]; then
          echo "$SERVICE_NAME pid=$expected_pid exited before SIGTERM during failed-start cleanup"
          return 0
        fi
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

  terminate_service_identity "$pid" "$start_time" "$allow_expected_pid_disappearance"
}

read_start_session_receipt() {
  local token
  local pid
  local parent_pid
  local session_id
  local start_time
  local state
  local trailing=""

  [ -n "$START_SESSION_FILE" ] || return 1
  [ -r "$START_SESSION_FILE" ] || return 1
  IFS=' ' read -r token pid parent_pid session_id start_time state trailing <"$START_SESSION_FILE" || return 2
  [ -z "$trailing" ] || return 2
  for value in "$token" "$pid" "$parent_pid" "$session_id" "$start_time"; do
    case "$value" in
      ''|*[!0-9A-Za-z._-]*) return 2 ;;
    esac
  done
  case "$pid:$parent_pid:$session_id:$start_time" in
    *[!0-9:]*|':::') return 2 ;;
  esac
  case "$state" in
    ready|unsafe-parent|unsafe-session|invalid-stat) ;;
    *) return 2 ;;
  esac
  printf '%s %s %s %s %s %s\n' "$token" "$pid" "$parent_pid" "$session_id" "$start_time" "$state"
}

write_start_decision() {
  local decision="$1"
  local decision_tmp

  case "$decision" in
    pending|go|abort) ;;
    *) return 2 ;;
  esac
  [ -n "$START_DECISION_FILE" ] || return 2
  [ -n "$START_GATE_TOKEN" ] || return 2
  decision_tmp="${START_DECISION_FILE}.tmp.$$"
  if ! printf '%s %s\n' "$decision" "$START_GATE_TOKEN" >"$decision_tmp"; then
    rm -f -- "$decision_tmp" 2>/dev/null || true
    return 1
  fi
  if ! mv -f -- "$decision_tmp" "$START_DECISION_FILE"; then
    rm -f -- "$decision_tmp" 2>/dev/null || true
    return 1
  fi
}

abort_prepared_start_session() {
  if [ -n "$START_GATE_TOKEN" ] && [ -n "$START_DECISION_FILE" ]; then
    if ! write_start_decision abort; then
      echo "WARNING: failed to write the start-abort decision; the wrapper only accepts an exact go decision" >&2
    fi
  fi
}

validate_prepared_start_session() {
  local receipt
  local result
  local token
  local pid
  local parent_pid
  local session_id
  local start_time
  local state
  local actual_parent_pid
  local actual_session_id
  local actual_start_time

  if receipt="$(read_start_session_receipt)"; then
    :
  else
    result=$?
    return "$result"
  fi
  read -r token pid parent_pid session_id start_time state <<<"$receipt"
  [ "$token" = "$START_GATE_TOKEN" ] || return 2
  [ "$state" = "ready" ] || return 2
  # 新 session 的 leader 必须就是 wrapper；其父进程必须仍是本脚本。若 setsid
  # 因进程组 leader 而 fork，wrapper 会有另一个父进程，绝不能获准启动 app.sh。
  [ "$pid" = "$session_id" ] || return 2
  [ "$parent_pid" = "$$" ] || return 2
  if actual_parent_pid="$(process_parent_pid "$pid")" \
    && actual_session_id="$(process_session_id "$pid")" \
    && actual_start_time="$(process_start_time "$pid")"; then
    :
  else
    return 2
  fi
  [ "$actual_parent_pid" = "$parent_pid" ] || return 2
  [ "$actual_session_id" = "$session_id" ] || return 2
  [ "$actual_start_time" = "$start_time" ] || return 2

  START_SESSION_ID="$session_id"
}

wait_for_prepared_start_session() {
  local deadline=$((SECONDS + START_LAUNCH_TIMEOUT_SECONDS))
  local result

  while [ "$SECONDS" -lt "$deadline" ]; do
    if validate_prepared_start_session; then
      return 0
    else
      result=$?
    fi
    [ "$result" -eq 2 ] && return 2
    sleep "$POLL_SECONDS"
  done

  if validate_prepared_start_session; then
    return 0
  else
    return "$?"
  fi
}

read_start_completion_status() {
  local app_start_result

  [ -n "$START_COMPLETION_FILE" ] || return 2
  [ -e "$START_COMPLETION_FILE" ] || return 1
  [ -r "$START_COMPLETION_FILE" ] || return 2
  IFS= read -r app_start_result <"$START_COMPLETION_FILE" || return 2
  case "$app_start_result" in
    ''|*[!0-9]*) return 2 ;;
  esac
  [ "$app_start_result" -le 255 ] || return 2
  printf '%s\n' "$app_start_result"
}

wait_for_start_completion_status() {
  local deadline=$((SECONDS + START_LAUNCH_TIMEOUT_SECONDS))
  local app_start_result
  local result

  while [ "$SECONDS" -lt "$deadline" ]; do
    if app_start_result="$(read_start_completion_status)"; then
      printf '%s\n' "$app_start_result"
      return 0
    else
      result=$?
    fi
    [ "$result" -eq 2 ] && return 2
    sleep "$POLL_SECONDS"
  done

  if app_start_result="$(read_start_completion_status)"; then
    printf '%s\n' "$app_start_result"
    return 0
  else
    return "$?"
  fi
}

prepare_start_session() {
  local token_file
  local gate_timeout_seconds=$((STOP_TIMEOUT_SECONDS + START_LAUNCH_TIMEOUT_SECONDS + POLL_SECONDS))
  local result

  [ -n "$START_SESSION_FILE" ] || die "Missing start-session receipt path"
  [ -n "$START_COMPLETION_FILE" ] || die "Missing start-session completion path"
  [ -n "$START_DECISION_FILE" ] || die "Missing start-decision path"
  rm -f -- "$START_SESSION_FILE" "$START_COMPLETION_FILE" "$START_DECISION_FILE"
  token_file="$(mktemp "$LOCK_DIR/start-token.XXXXXX")" || die "Unable to allocate the start-session token"
  START_GATE_TOKEN="${token_file##*/}"
  rm -f -- "$token_file"
  if ! write_start_decision pending; then
    die "Unable to write the pending start decision"
  fi

  # 先后台创建受控 session，但 wrapper 没有精确 go 决策绝不执行 app.sh。这样即使
  # 旧版 setsid 因 fork 提前返回，也能在停止旧实例前由 wrapper 的实际 PPID 证明失败。
  # shellcheck disable=SC2016 # 参数和 $$ 必须由新 session 内的 bash 在运行时展开。
  "$SETSID_BIN" "$BASH_BIN" -c '
    set -u
    receipt_file="$1"
    completion_file="$2"
    decision_file="$3"
    token="$4"
    expected_parent_pid="$5"
    proc_root="$6"
    gate_timeout_seconds="$7"
    poll_seconds="$8"
    app_root="$9"
    shift 9
    umask 077

    write_receipt() {
      receipt_state="$1"
      receipt_tmp="${receipt_file}.tmp.$$"
      printf "%s %s %s %s %s %s\\n" "$token" "$$" "$parent_pid" "$session_id" "$start_time" "$receipt_state" > "$receipt_tmp"
      mv -f -- "$receipt_tmp" "$receipt_file"
    }

    if ! IFS= read -r stat_line < "$proc_root/$$/stat"; then
      parent_pid=0
      session_id=0
      start_time=0
      write_receipt invalid-stat || exit 125
      exit 124
    fi
    stat_after_command="${stat_line##*) }"
    read -r -a stat_fields <<< "$stat_after_command"
    if [ "${#stat_fields[@]}" -lt 20 ]; then
      parent_pid=0
      session_id=0
      start_time=0
      write_receipt invalid-stat || exit 125
      exit 124
    fi
    parent_pid="${stat_fields[1]}"
    session_id="${stat_fields[3]}"
    start_time="${stat_fields[19]}"
    case "$parent_pid:$session_id:$start_time" in
      *[!0-9:]*|'::')
        write_receipt invalid-stat || exit 125
        exit 124
        ;;
    esac
    if [ "$parent_pid" != "$expected_parent_pid" ]; then
      write_receipt unsafe-parent || exit 125
      exit 124
    fi
    if [ "$session_id" != "$$" ]; then
      write_receipt unsafe-session || exit 125
      exit 124
    fi
    write_receipt ready || exit 125

    gate_deadline=$((SECONDS + gate_timeout_seconds))
    while [ "$SECONDS" -lt "$gate_deadline" ]; do
      decision=""
      if [ -r "$decision_file" ]; then
        IFS= read -r decision < "$decision_file" || decision=""
      fi
      case "$decision" in
        "go $token") break ;;
        "pending $token") sleep "$poll_seconds" ;;
        *) exit 124 ;;
      esac
    done
    if [ "$decision" != "go $token" ]; then
      exit 124
    fi

    export SMART_RESTART_SERVICE_SESSION_ID="$session_id"
    # 生产 app.sh 用 pwd 推导 APP_HOME 和 config 目录；必须在真实应用根目录执行，
    # 不能继承发布工件 runtime/ 或运维终端的当前目录。
    if cd "$app_root"; then
      "$@"
      app_result=$?
    else
      app_result=126
    fi
    completion_tmp="${completion_file}.tmp.$$"
    printf "%s\\n" "$app_result" > "$completion_tmp"
    if ! mv -f -- "$completion_tmp" "$completion_file"; then
      exit 125
    fi
    exit "$app_result"
  ' bash "$START_SESSION_FILE" "$START_COMPLETION_FILE" "$START_DECISION_FILE" "$START_GATE_TOKEN" "$$" "$PROC_ROOT" "$gate_timeout_seconds" "$POLL_SECONDS" "$APP_ROOT" "$APP_SCRIPT" start "smart-jar/$JAR_NAME" &

  if wait_for_prepared_start_session; then
    return 0
  else
    result=$?
  fi
  abort_prepared_start_session
  [ "$result" -eq 2 ] && die "$SERVICE_NAME start session cannot prove the required direct parent/session contract; old instance was left untouched"
  die "$SERVICE_NAME start session did not become verifiable within ${START_LAUNCH_TIMEOUT_SECONDS}s; old instance was left untouched"
}

start_app_in_isolated_session() {
  local app_start_result
  local result

  # 停止旧实例后再复核一次 wrapper 身份，随后以原子 go 决策放行 app.sh。若复核失败，
  # wrapper 只能看到 abort/缺失决策而退出，绝不会延迟拉起 Java。
  if ! validate_prepared_start_session; then
    abort_prepared_start_session
    die "$SERVICE_NAME prepared start session changed before authorization; refusing to launch app.sh"
  fi
  if ! write_start_decision go; then
    abort_prepared_start_session
    die "$SERVICE_NAME could not authorize the prepared start session"
  fi

  if app_start_result="$(wait_for_start_completion_status)"; then
    :
  else
    result=$?
    cleanup_failed_start "app.sh did not report a completion status within ${START_LAUNCH_TIMEOUT_SECONDS}s" "$START_SESSION_ID"
    [ "$result" -eq 2 ] && die "$SERVICE_NAME app.sh wrote an invalid completion status; started session was stopped when safely possible"
    die "$SERVICE_NAME app.sh did not report a completion status; started session was stopped when safely possible"
  fi
  return "$app_start_result"
}

session_member_matches_identity() {
  local pid="$1"
  local expected_session_id="$2"
  local expected_start_time="$3"
  local current_session_id
  local current_start_time

  if current_session_id="$(process_session_id "$pid")" \
    && current_start_time="$(process_start_time "$pid")"; then
    [ "$current_session_id" = "$expected_session_id" ] && [ "$current_start_time" = "$expected_start_time" ]
    return
  fi
  return 1
}

collect_start_session_pids() {
  local expected_session_id="$1"
  local proc_dir
  local pid
  local session_id
  local confirmed_session_id
  local start_time
  local confirmed_start_time

  SESSION_PIDS=()
  SESSION_START_TIMES=()
  UNSTABLE_SESSION_PIDS=()
  for proc_dir in "$PROC_ROOT"/[0-9]*; do
    [ -d "$proc_dir" ] || continue
    pid="${proc_dir##*/}"
    if session_id="$(process_session_id "$pid")" && [ "$session_id" = "$expected_session_id" ]; then
      # 失败清理需要终止 wrapper 与其所有子进程；两次读取 session/starttime 后才会
      # 发信号，避免 PID 复用或会话已经变化时误杀别的进程。
      if start_time="$(process_start_time "$pid")" \
        && confirmed_session_id="$(process_session_id "$pid")" \
        && confirmed_start_time="$(process_start_time "$pid")" \
        && [ "$confirmed_session_id" = "$expected_session_id" ] \
        && [ "$start_time" = "$confirmed_start_time" ]; then
        SESSION_PIDS+=("$pid")
        SESSION_START_TIMES+=("$start_time")
      else
        UNSTABLE_SESSION_PIDS+=("$pid")
      fi
    fi
  done
}

terminate_start_session() {
  local expected_session_id="$1"
  local deadline=$((SECONDS + STOP_TIMEOUT_SECONDS))
  local index
  local pid
  local start_time
  local result

  case "$expected_session_id" in
    ''|*[!0-9]*) return 2 ;;
  esac

  # app.sh 可以在超时边界派生后台 Java；所以按 session 反复枚举到空集，而不是只
  # 终止第一次扫描到的目标 JAR，避免稍后才启动的子进程遗留。
  while :; do
    collect_start_session_pids "$expected_session_id"
    [ "${#UNSTABLE_SESSION_PIDS[@]}" -eq 0 ] || return 2
    [ "${#SESSION_PIDS[@]}" -eq 0 ] && return 0

    for ((index = 0; index < ${#SESSION_PIDS[@]}; index++)); do
      pid="${SESSION_PIDS[$index]}"
      start_time="${SESSION_START_TIMES[$index]}"
      if ! session_member_matches_identity "$pid" "$expected_session_id" "$start_time"; then
        # 已退出或 PID/会话已变化时不再触碰该 PID；下一轮会重新判断剩余成员。
        continue
      fi
      echo "$SERVICE_NAME failed-start cleanup: stopping session member pid=$pid with SIGTERM"
      if "$KILL_BIN" -TERM "$pid"; then
        :
      elif session_member_matches_identity "$pid" "$expected_session_id" "$start_time"; then
        return 2
      fi
    done

    if [ "$SECONDS" -ge "$deadline" ]; then
      break
    fi
    sleep "$POLL_SECONDS"
  done

  collect_start_session_pids "$expected_session_id"
  [ "${#UNSTABLE_SESSION_PIDS[@]}" -eq 0 ] || return 2
  [ "${#SESSION_PIDS[@]}" -eq 0 ] && return 0
  result=1
  return "$result"
}

cleanup_failed_start() {
  local failure_reason="$1"
  local expected_session_id="$2"
  local index
  local pid
  local start_time
  local session_id
  local result
  local -a owned_pids=()
  local -a owned_start_times=()
  local -a foreign_pids=()
  local -a unreadable_session_pids=()

  case "$expected_session_id" in
    ''|*[!0-9]*) die "$SERVICE_NAME start failed ($failure_reason), but the start session cannot be verified" ;;
  esac

  if terminate_start_session "$expected_session_id"; then
    :
  else
    result=$?
    if [ "$result" -eq 1 ]; then
      die "$SERVICE_NAME start failed ($failure_reason), but session $expected_session_id still has running members after ${STOP_TIMEOUT_SECONDS}s; manual investigation is required"
    fi
    die "$SERVICE_NAME start failed ($failure_reason), but session $expected_session_id cannot be terminated with a stable identity; manual investigation is required"
  fi

  # 会话已清空后，再按 JAR/工作目录检查残留与并发外部实例，给出明确诊断。
  collect_service_pids
  for ((index = 0; index < ${#MATCHED_PIDS[@]}; index++)); do
    pid="${MATCHED_PIDS[$index]}"
    start_time="${MATCHED_START_TIMES[$index]}"
    if session_id="$(process_session_id "$pid")"; then
      if [ "$session_id" = "$expected_session_id" ]; then
        owned_pids+=("$pid")
        owned_start_times+=("$start_time")
      else
        foreign_pids+=("$pid")
      fi
    else
      unreadable_session_pids+=("$pid")
    fi
  done

  for ((index = 0; index < ${#owned_pids[@]}; index++)); do
    pid="${owned_pids[$index]}"
    start_time="${owned_start_times[$index]}"
    echo "$SERVICE_NAME start failed ($failure_reason); stopping pid=$pid from start session $expected_session_id"
    terminate_service_identity "$pid" "$start_time" 1
    echo "$SERVICE_NAME failed-start instance pid=$pid was stopped"
  done

  if [ "${#foreign_pids[@]}" -gt 0 ]; then
    echo "$SERVICE_NAME start failed ($failure_reason); left different-session instance(s) untouched: ${foreign_pids[*]}" >&2
  fi
  if [ "${#UNSTABLE_MATCHED_PIDS[@]}" -gt 0 ] || [ "${#unreadable_session_pids[@]}" -gt 0 ]; then
    die "$SERVICE_NAME start failed ($failure_reason), but one or more matching processes cannot be attributed safely; manual investigation is required"
  fi
  if [ "${#owned_pids[@]}" -eq 0 ]; then
    echo "$SERVICE_NAME start failed ($failure_reason), and no matching process from start session $expected_session_id remains"
  fi
}

start_service() {
  local prepared_start="${1:-0}"
  local result
  local app_start_result

  case "$prepared_start" in
    0|1) ;;
    *) die "Invalid prepared-start state: $prepared_start" ;;
  esac

  if [ "$prepared_start" = "0" ]; then
    # 独立 start 路径同样先完成 gated session 证明；当前 CLI 只暴露 restart，
    # 但保留此处使后续显式 start 不会绕过原子启动契约。
    prepare_start_session
  fi

  if single_service_pid >/dev/null; then
    abort_prepared_start_session
    die "$SERVICE_NAME is already running; refusing to start a duplicate instance"
  else
    result=$?
    if [ "$result" -eq 2 ]; then
      abort_prepared_start_session
      return 2
    fi
  fi
  if port_is_occupied; then
    abort_prepared_start_session
    die "Port $SERVICE_PORT is already listening while $SERVICE_NAME is absent; investigate before starting"
  else
    result=$?
    if [ "$result" -eq 2 ]; then
      abort_prepared_start_session
      die "Cannot inspect listening sockets before starting $SERVICE_NAME"
    fi
  fi

  echo "Starting $SERVICE_NAME from $JAR_PATH"
  # app.sh 可能先派生后台 Java 进程再以非零状态退出；独立 session 既保留其现有
  # 启动协议，也让失败清理能够精确识别本次启动实例，不能让 set -e 直接遗留进程。
  if start_app_in_isolated_session; then
    app_start_result=0
  else
    app_start_result=$?
  fi
  if [ "$app_start_result" -ne 0 ]; then
    cleanup_failed_start "app.sh start exited with status $app_start_result" "$START_SESSION_ID"
    die "$SERVICE_NAME app.sh start failed with status $app_start_result; started instance was stopped when safely possible"
  fi

  if wait_for_start >/dev/null; then
    :
  else
    result=$?
    if [ "$result" -eq 2 ]; then
      cleanup_failed_start "process identity or socket state could not be established" "$START_SESSION_ID"
      die "$SERVICE_NAME start state could not be established; started instance was stopped when safely possible"
    fi
    cleanup_failed_start "did not become ready within ${START_TIMEOUT_SECONDS}s" "$START_SESSION_ID"
    die "$SERVICE_NAME did not become ready within ${START_TIMEOUT_SECONDS}s; started instance was stopped when safely possible"
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

  # 在停止旧实例之前，只创建一个尚未获准执行 app.sh 的会话。它必须证明仍由本脚本
  # 直接派生；否则旧服务保持运行，消除 setsid 提前返回时的非原子重启窗口。
  prepare_start_session
  stop_service "$pid" "$start_time"
  start_service 1
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
