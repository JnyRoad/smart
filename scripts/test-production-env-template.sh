#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
nacos_config_dir="$repo_root/docker/nacos/config/dev"
production_template="$repo_root/docker/.env.production.example"

fail() {
  echo "FAIL: $*" >&2
  exit 1
}

assert_empty_mapping() {
  local variable_name="$1"

  grep -Eq "^${variable_name}=$" "$production_template" || \
    fail "expected production template to declare an empty mapping for $variable_name"
}

main() {
  local variable_name
  local -a variables

  [[ -d "$nacos_config_dir" ]] || fail "missing Nacos config directory: $nacos_config_dir"
  [[ -f "$production_template" ]] || fail "missing production environment template: $production_template"

  # 从所有实际 Nacos 配置提取变量，防止模板与服务配置漂移。
  while IFS= read -r variable_name; do
    [[ -n "$variable_name" ]] && variables+=("$variable_name")
  done < <(
    rg -o --no-filename '\$\{[A-Z0-9_]+[:}]' "$nacos_config_dir" --glob '*.yml' |
      sed -E 's/^\$\{([A-Z0-9_]+)[:}]$/\1/' |
      sort -u
  )

  (( ${#variables[@]} > 0 )) || fail "expected Nacos configs to contain environment placeholders"

  for variable_name in "${variables[@]}"; do
    assert_empty_mapping "$variable_name"
  done

  echo "All production environment template tests passed."
}

main "$@"
