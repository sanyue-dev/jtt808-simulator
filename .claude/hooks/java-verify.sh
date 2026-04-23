#!/usr/bin/env bash
# Claude 结束回合前的质量门禁:
#   - 没动过 Java 文件 → 直接放行
#   - 动过了 → 先编译,失败就让 Claude 修编译错误
#   - 编译通过 → 放行
set -u

input=$(cat)
stop_active=$(printf '%s' "$input" | jq -r '.stop_hook_active // false')

# 防无限循环:如果本轮已经因为这个 hook 继续过一次,就放行
# (官方:stop_hook_active is true when Claude Code is already continuing as a result of a stop hook)
[[ "$stop_active" == "true" ]] && exit 0

cd "$CLAUDE_PROJECT_DIR" || exit 0

# 快速短路:本 session 没改过 Java 就不浪费时间
# 覆盖已跟踪修改 + 未跟踪新文件
changed=$(git status --porcelain 2>/dev/null | grep -E '\.java$' || true)
[[ -z "$changed" ]] && exit 0

# 仅使用 Maven Wrapper
if [[ ! -f "mvnw" ]]; then
  printf '未找到 Maven Wrapper，请先在仓库根目录配置 mvnw 后再结束任务。\n' >&2
  exit 2
fi

build=(./mvnw)
compile_goal="compile"

# ---------- Step 1: 编译 ----------
compile_out=$("${build[@]}" "$compile_goal" -q 2>&1)
compile_rc=$?

if [[ $compile_rc -ne 0 ]]; then
  errors=$(printf '%s' "$compile_out" | grep -E '(\[ERROR\]|error:|FAILED)' | head -30)
  [[ -z "$errors" ]] && errors=$(printf '%s' "$compile_out" | tail -40)

  printf '编译失败,请先修复以下编译错误再结束任务:\n\n%s\n' "$errors" >&2
  exit 2
fi

exit 0