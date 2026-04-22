#!/usr/bin/env bash
set -euo pipefail

OWNER="${GITHUB_OWNER:-hiccup90}"
REPO="${GITHUB_REPO:-routeros-manager}"
WORKFLOW_FILE="${GITHUB_WORKFLOW_FILE:-android-debug-apk.yml}"
ARTIFACT_NAME="${GITHUB_ARTIFACT_NAME:-routeros-manager-debug-apk}"
OUTPUT_DIR="${1:-$HOME/Downloads/routeros-manager}"
API_ROOT="https://api.github.com/repos/${OWNER}/${REPO}"

get_token() {
  if [[ -n "${GITHUB_TOKEN:-}" ]]; then
    printf '%s' "$GITHUB_TOKEN"
    return 0
  fi

  if [[ -f "$HOME/.git-credentials" ]]; then
    python3 - <<'PY'
from pathlib import Path
from urllib.parse import urlparse
for line in Path.home().joinpath('.git-credentials').read_text().splitlines():
    if 'github.com' in line:
        parsed = urlparse(line.strip())
        if parsed.password:
            print(parsed.password)
            break
PY
    return 0
  fi

  echo "未找到 GITHUB_TOKEN。请先 export GITHUB_TOKEN=... 或配置 ~/.git-credentials" >&2
  return 1
}

TOKEN="$(get_token)"
AUTH_HEADER="Authorization: Bearer ${TOKEN}"
ACCEPT_HEADER="Accept: application/vnd.github+json"
ARTIFACT_ZIP="$(mktemp /tmp/routeros-manager-artifact.XXXXXX.zip)"
RUN_JSON="$(mktemp /tmp/routeros-manager-runs.XXXXXX.json)"
ARTIFACT_JSON="$(mktemp /tmp/routeros-manager-artifacts.XXXXXX.json)"
trap 'rm -f "$ARTIFACT_ZIP" "$RUN_JSON" "$ARTIFACT_JSON"' EXIT

mkdir -p "$OUTPUT_DIR"

curl -fsSL \
  -H "$AUTH_HEADER" \
  -H "$ACCEPT_HEADER" \
  "${API_ROOT}/actions/workflows/${WORKFLOW_FILE}/runs?branch=main&status=success&per_page=1" \
  -o "$RUN_JSON"

read -r RUN_ID RUN_NUMBER HEAD_SHA <<<"$(python3 - <<'PY' "$RUN_JSON"
import json, sys
with open(sys.argv[1]) as f:
    data = json.load(f)
runs = data.get('workflow_runs', [])
if not runs:
    raise SystemExit('没有找到成功的工作流运行')
run = runs[0]
print(run['id'], run['run_number'], run['head_sha'])
PY
)"

curl -fsSL \
  -H "$AUTH_HEADER" \
  -H "$ACCEPT_HEADER" \
  "${API_ROOT}/actions/runs/${RUN_ID}/artifacts" \
  -o "$ARTIFACT_JSON"

ARTIFACT_ID="$(python3 - <<'PY' "$ARTIFACT_JSON" "$ARTIFACT_NAME"
import json, sys
with open(sys.argv[1]) as f:
    data = json.load(f)
name = sys.argv[2]
for artifact in data.get('artifacts', []):
    if artifact.get('name') == name and not artifact.get('expired', False):
        print(artifact['id'])
        raise SystemExit(0)
raise SystemExit(f'未找到 artifact: {name}')
PY
)"

curl -fsSL \
  -H "$AUTH_HEADER" \
  -H 'Accept: application/vnd.github+json' \
  "${API_ROOT}/actions/artifacts/${ARTIFACT_ID}/zip" \
  -o "$ARTIFACT_ZIP"

unzip -o "$ARTIFACT_ZIP" -d "$OUTPUT_DIR" >/dev/null

APK_PATH="$OUTPUT_DIR/app-debug.apk"
if [[ ! -f "$APK_PATH" ]]; then
  echo "下载成功，但未找到 app-debug.apk" >&2
  exit 1
fi

FINAL_PATH="$OUTPUT_DIR/routeros-manager-run${RUN_NUMBER}-${HEAD_SHA:0:7}.apk"
mv -f "$APK_PATH" "$FINAL_PATH"

echo "$FINAL_PATH"
