#!/usr/bin/env bash
# Claude 结束回合前的质量门禁:
#   - 没动过 Java 文件 → 直接放行
#   - 动过了 → 先编译,失败就让 Claude 修编译错误
#   - 编译通过 → 跑测试,失败就让 Claude 修测试
#   - 全通过 → 放行
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

# 选构建工具
if [[ -f "mvnw" ]]; then
  build=(./mvnw)
elif [[ -f "pom.xml" ]]; then
  build=(mvn)
elif [[ -f "gradlew" ]]; then
  build=(./gradlew)
else
  exit 0
fi

# ---------- Step 1: 编译 ----------
if [[ "${build[0]}" == *"gradle"* ]]; then
  compile_goal="compileTestJava"   # Gradle 这个目标会连带编译 main
else
  compile_goal="test-compile"      # Maven 同理,test-compile 依赖 compile
fi

compile_out=$("${build[@]}" "$compile_goal" -q 2>&1)
compile_rc=$?

if [[ $compile_rc -ne 0 ]]; then
  errors=$(printf '%s' "$compile_out" | grep -E '(\[ERROR\]|error:|FAILED)' | head -30)
  [[ -z "$errors" ]] && errors=$(printf '%s' "$compile_out" | tail -40)

  printf '编译失败,请先修复以下编译错误再结束任务:\n\n%s\n' "$errors" >&2
  exit 2
fi

# ---------- Step 2: 测试 ----------
if [[ "${build[0]}" == *"gradle"* ]]; then
  test_goal="test"
else
  test_goal="test"
fi

test_out=$("${build[@]}" "$test_goal" -q 2>&1)
test_rc=$?

if [[ $test_rc -ne 0 ]]; then
  # 优先展示失败摘要
  failures=$(printf '%s' "$test_out" \
    | grep -E '(Tests run:|FAILED|\[ERROR\]|<<< FAILURE|Expected|AssertionError)' \
    | head -40)
  [[ -z "$failures" ]] && failures=$(printf '%s' "$test_out" | tail -50)

  printf '测试未通过,请修复后再结束任务:\n\n%s\n' "$failures" >&2
  exit 2
fi

exit 0