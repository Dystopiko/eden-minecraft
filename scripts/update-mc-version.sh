#!/usr/bin/env bash
# =============================================================================
# update-mc-version.sh
# =============================================================================
# Automatically updates the Minecraft version, Fabric ecosystem versions
# (fabric-loader, fabric-api, fabric-language-kotlin), and re-downloads the
# Modrinth mod JARs (Floodgate, Geyser, LuckPerms) in this project.
#
# Usage:
#   ./scripts/update-mc-version.sh [MC_VERSION]
#
# Arguments:
#   MC_VERSION  The Minecraft version to target (e.g. "26.2").
#               If omitted, the script uses the value already set in
#               libs.versions.toml and only refreshes dependencies.
#
# Environment:
#   AUTO_YES=1  Skip the confirmation prompt and apply changes automatically.
#
# Requirements: bash, curl, jq, python3
# =============================================================================

set -euo pipefail

# ── Colour helpers ─────────────────────────────────────────────────────────────
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'
CYAN='\033[0;36m'; BOLD='\033[1m'; RESET='\033[0m'

info()    { echo -e "${CYAN}${BOLD}[INFO]${RESET}  $*"; }
success() { echo -e "${GREEN}${BOLD}[ OK ]${RESET}  $*"; }
warn()    { echo -e "${YELLOW}${BOLD}[WARN]${RESET}  $*"; }
error()   { echo -e "${RED}${BOLD}[ERR]${RESET}   $*" >&2; }
die()     { error "$*"; exit 1; }

# ── Dependency check ──────────────────────────────────────────────────────────
for cmd in curl jq python3 sed; do
    command -v "$cmd" &>/dev/null || die "Required command not found: $cmd"
done

# ── Locate project root ───────────────────────────────────────────────────────
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
VERSIONS_FILE="$PROJECT_ROOT/libs.versions.toml"
BUILD_FILE="$PROJECT_ROOT/fabric/build.gradle.kts"
MODS_DIR="$PROJECT_ROOT/fabric/run/mods"

[[ -f "$VERSIONS_FILE" ]] || die "Cannot find libs.versions.toml at: $VERSIONS_FILE"
[[ -f "$BUILD_FILE" ]]    || die "Cannot find fabric/build.gradle.kts at: $BUILD_FILE"

# ── Read current versions from libs.versions.toml ────────────────────────────
read_version() {
    # Reads a version value by key from the [versions] section.
    # e.g. read_version "minecraft" → "26.2"
    grep -E "^${1}\s*=" "$VERSIONS_FILE" | sed -E 's/.*=\s*"([^"]+)".*/\1/' | head -1
}

CURRENT_MC="$(read_version "minecraft")"
[[ -n "$CURRENT_MC" ]] || die "Could not read current minecraft version from $VERSIONS_FILE"

# ── Resolve target MC version ─────────────────────────────────────────────────
TARGET_MC="${1:-$CURRENT_MC}"

echo
echo -e "${BOLD}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"
echo -e "${BOLD}  EdenMC — Minecraft Version Updater${RESET}"
echo -e "${BOLD}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"
echo
info "Current Minecraft version : ${YELLOW}${CURRENT_MC}${RESET}"
info "Target  Minecraft version : ${GREEN}${TARGET_MC}${RESET}"
echo

# ── API constants ─────────────────────────────────────────────────────────────
MODRINTH_API="https://api.modrinth.com/v2"
FABRIC_META_API="https://meta.fabricmc.net/v2"

# Fetch JSON from a URL; die on error.
fetch() {
    curl -fsSL \
        --retry 3 \
        --retry-delay 2 \
        -A "eden-minecraft-updater/1.0 (github.com/Dystopiko/eden-minecraft)" \
        "$1" \
      || die "HTTP request failed: $1"
}

# Returns JSON: { version_number, version_id, url, filename } for the
# latest Modrinth release of $1 compatible with Fabric + $2 (MC version).
# Prints "null" if no compatible version is found.
modrinth_latest() {
    local project_id="$1"
    local mc_ver="$2"

    # Use curl's --data-urlencode via -G for clean URL encoding.
    local data
    data="$(curl -fsSL \
        --retry 3 --retry-delay 2 \
        -A "eden-minecraft-updater/1.0" \
        -G "${MODRINTH_API}/project/${project_id}/version" \
        --data-urlencode "loaders=[\"fabric\"]" \
        --data-urlencode "game_versions=[\"${mc_ver}\"]" \
        --data-urlencode "include_changelog=false" \
      )" || die "Modrinth API request failed for project: ${project_id}"

    local count
    count="$(echo "$data" | jq 'length')"
    if [[ "$count" -eq 0 ]]; then
        echo "null"
        return
    fi

    # First entry is the most recent. Pick the primary file (fallback to [0]).
    echo "$data" | jq -r '
        .[0] as $v |
        (($v.files[] | select(.primary == true)) // $v.files[0]) as $f |
        {
            version_number: $v.version_number,
            version_id:     $v.id,
            url:            $f.url,
            filename:       $f.filename,
            sha512:         $f.hashes.sha512
        }
    '
}

# ── 1. fabric-loader ──────────────────────────────────────────────────────────
info "Fetching latest stable fabric-loader …"
LOADER_JSON="$(fetch "${FABRIC_META_API}/versions/loader")"
NEW_LOADER="$(echo "$LOADER_JSON" | jq -r 'map(select(.stable == true)) | .[0].version')"
[[ -n "$NEW_LOADER" && "$NEW_LOADER" != "null" ]] \
    || die "Could not determine latest stable fabric-loader version."
success "fabric-loader        ${NEW_LOADER}"

# ── 2. fabric-api ─────────────────────────────────────────────────────────────
info "Fetching latest fabric-api for MC ${TARGET_MC} …"
FABRIC_API_JSON="$(modrinth_latest "P7dR8mSH" "$TARGET_MC")"
if [[ "$FABRIC_API_JSON" == "null" ]]; then
    warn "No fabric-api found for MC ${TARGET_MC} — keeping current version."
    NEW_FABRIC_API="$(read_version "fabric-api")"
else
    NEW_FABRIC_API="$(echo "$FABRIC_API_JSON" | jq -r '.version_number')"
    success "fabric-api           ${NEW_FABRIC_API}"
fi

# ── 3. fabric-language-kotlin ─────────────────────────────────────────────────
info "Fetching latest fabric-language-kotlin for MC ${TARGET_MC} …"
FK_JSON="$(modrinth_latest "Ha28R6CL" "$TARGET_MC")"
if [[ "$FK_JSON" == "null" ]]; then
    warn "No fabric-language-kotlin found for MC ${TARGET_MC} — keeping current version."
    NEW_FABRIC_KOTLIN="$(read_version "fabric-kotlin")"
else
    NEW_FABRIC_KOTLIN="$(echo "$FK_JSON" | jq -r '.version_number')"
    success "fabric-language-kotlin  ${NEW_FABRIC_KOTLIN}"
fi

# ── 4. Modrinth mod JARs ──────────────────────────────────────────────────────
info "Fetching latest Floodgate for MC ${TARGET_MC} …"
FLOODGATE_JSON="$(modrinth_latest "bWrNNfkb" "$TARGET_MC")"
if [[ "$FLOODGATE_JSON" == "null" ]]; then
    warn "No Floodgate release found for MC ${TARGET_MC} — skipping."
    FLOODGATE_URL=""; FLOODGATE_VER="(unchanged)"
else
    FLOODGATE_URL="$(echo "$FLOODGATE_JSON" | jq -r '.url')"
    FLOODGATE_VER="$(echo "$FLOODGATE_JSON" | jq -r '.version_number')"
    success "Floodgate            ${FLOODGATE_VER}"
fi

info "Fetching latest Geyser-Fabric for MC ${TARGET_MC} …"
GEYSER_JSON="$(modrinth_latest "wKkoqHrH" "$TARGET_MC")"
if [[ "$GEYSER_JSON" == "null" ]]; then
    warn "No Geyser release found for MC ${TARGET_MC} — skipping."
    GEYSER_URL=""; GEYSER_VER="(unchanged)"
else
    GEYSER_URL="$(echo "$GEYSER_JSON" | jq -r '.url')"
    GEYSER_VER="$(echo "$GEYSER_JSON" | jq -r '.version_number')"
    success "Geyser               ${GEYSER_VER}"
fi

info "Fetching latest LuckPerms-Fabric for MC ${TARGET_MC} …"
LP_JSON="$(modrinth_latest "Vebnzrzj" "$TARGET_MC")"
if [[ "$LP_JSON" == "null" ]]; then
    warn "No LuckPerms release found for MC ${TARGET_MC} — skipping."
    LP_URL=""; LP_VER="(unchanged)"
else
    LP_URL="$(echo "$LP_JSON" | jq -r '.url')"
    LP_VER="$(echo "$LP_JSON" | jq -r '.version_number')"
    success "LuckPerms            ${LP_VER}"
fi

# ── 5. Planned-changes summary ────────────────────────────────────────────────
echo
echo -e "${BOLD}━━ Planned changes ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"
printf "  %-36s %s → %s\n" \
    "libs.versions.toml › minecraft"      "${YELLOW}${CURRENT_MC}${RESET}"     "${GREEN}${TARGET_MC}${RESET}" | sed 's/  / /g'
echo -e "  libs.versions.toml"
echo -e "    minecraft              ${YELLOW}${CURRENT_MC}${RESET} → ${GREEN}${TARGET_MC}${RESET}"
echo -e "    fabric-loader          ${YELLOW}$(read_version "fabric-loader")${RESET} → ${GREEN}${NEW_LOADER}${RESET}"
echo -e "    fabric-api             ${YELLOW}$(read_version "fabric-api")${RESET} → ${GREEN}${NEW_FABRIC_API}${RESET}"
echo -e "    fabric-kotlin          ${YELLOW}$(read_version "fabric-kotlin")${RESET} → ${GREEN}${NEW_FABRIC_KOTLIN}${RESET}"
echo -e "  fabric/build.gradle.kts"
echo -e "    Floodgate download     ${GREEN}${FLOODGATE_VER}${RESET}"
echo -e "    Geyser download        ${GREEN}${GEYSER_VER}${RESET}"
echo -e "    LuckPerms download     ${GREEN}${LP_VER}${RESET}"
echo -e "  fabric/run/mods/         (JAR files re-downloaded)"
echo

# ── Confirmation ──────────────────────────────────────────────────────────────
if [[ "${AUTO_YES:-0}" != "1" ]]; then
    read -rp "$(echo -e "${BOLD}Apply these changes? [y/N]${RESET} ")" CONFIRM
    [[ "$CONFIRM" =~ ^[Yy]$ ]] || { echo "Aborted — no files were modified."; exit 0; }
fi
echo

# ── Helper: update a key in [versions] section of libs.versions.toml ─────────
set_version() {
    local key="$1" new_val="$2"
    sed -i -E "s|^(${key}[[:space:]]*=[[:space:]]*)\"[^\"]+\"|\1\"${new_val}\"|" "$VERSIONS_FILE"
}

# ── Helper: update target_mc_version in processResources block ───────────────
update_target_mc_in_build() {
    local new_mc="$1"
    sed -i -E "s|(\"target_mc_version\"[[:space:]]+to[[:space:]]+\")~[^\"]+\"|\1~${new_mc}\"|" "$BUILD_FILE"
}

# ── Helper: replace url = "…" inside a named DownloadTask block ───────────────
# Uses Python for reliable multi-line regex patching of the Kotlin build file.
update_download_url() {
    local task_name="$1" new_url="$2"
    python3 - "$BUILD_FILE" "$task_name" "$new_url" <<'PYEOF'
import sys, re

build_file, task_name, new_url = sys.argv[1], sys.argv[2], sys.argv[3]

with open(build_file, "r") as f:
    content = f.read()

# Match the register<DownloadTask>("…") { … url = "…" … } block.
pattern = re.compile(
    r'(tasks\.register<DownloadTask>\("' + re.escape(task_name) + r'"\)\s*\{[^}]*?\burl\s*=\s*)"[^"]+"',
    re.DOTALL
)
new_content, count = pattern.subn(r'\g<1>"' + new_url.replace('\\', '\\\\') + '"', content)

if count == 0:
    print(f"  WARNING: Could not find DownloadTask('{task_name}') in {build_file}", file=sys.stderr)
    sys.exit(0)

with open(build_file, "w") as f:
    f.write(new_content)
PYEOF
}

# ── Helper: download a JAR with integrity check ───────────────────────────────
download_jar() {
    local url="$1" output="$2" name="$3" expected_sha512="${4:-}"
    if [[ -z "$url" ]]; then
        warn "Skipping ${name} — no URL available."
        return
    fi

    info "Downloading ${name} …"
    if curl -fsSL --retry 3 --retry-delay 2 --progress-bar -o "$output" "$url"; then
        if [[ -n "$expected_sha512" ]] && command -v sha512sum &>/dev/null; then
            local actual_sha512
            actual_sha512="$(sha512sum "$output" | awk '{print $1}')"
            if [[ "$actual_sha512" == "$expected_sha512" ]]; then
                success "Downloaded & verified ${name}  →  $(basename "$output")"
            else
                warn "${name} checksum mismatch! File may be corrupt."
                warn "  Expected: ${expected_sha512}"
                warn "  Actual:   ${actual_sha512}"
            fi
        else
            success "Downloaded ${name}  →  $(basename "$output")"
        fi
    else
        warn "Download failed for ${name}: ${url}"
    fi
}

# ── 6. Apply: libs.versions.toml ─────────────────────────────────────────────
info "Patching libs.versions.toml …"
set_version "minecraft"     "$TARGET_MC"
set_version "fabric-loader" "$NEW_LOADER"
set_version "fabric-api"    "$NEW_FABRIC_API"
set_version "fabric-kotlin" "$NEW_FABRIC_KOTLIN"
success "libs.versions.toml updated."

# ── 7. Apply: fabric/build.gradle.kts ────────────────────────────────────────
info "Patching fabric/build.gradle.kts …"
update_target_mc_in_build "$TARGET_MC"
[[ -n "$FLOODGATE_URL" ]] && update_download_url "downloadFloodgate" "$FLOODGATE_URL"
[[ -n "$GEYSER_URL" ]]    && update_download_url "downloadGeyser"    "$GEYSER_URL"
[[ -n "$LP_URL" ]]        && update_download_url "downloadLuckPerms" "$LP_URL"
success "fabric/build.gradle.kts updated."

# ── 8. Download mod JARs ──────────────────────────────────────────────────────
mkdir -p "$MODS_DIR"
info "Downloading mod JARs into fabric/run/mods/ …"
download_jar "$FLOODGATE_URL" "$MODS_DIR/Floodgate.jar"  "Floodgate" \
    "$(echo "$FLOODGATE_JSON" | jq -r '.sha512 // empty' 2>/dev/null)"
download_jar "$GEYSER_URL"    "$MODS_DIR/Geyser.jar"     "Geyser" \
    "$(echo "$GEYSER_JSON"    | jq -r '.sha512 // empty' 2>/dev/null)"
download_jar "$LP_URL"        "$MODS_DIR/LuckPerms.jar"  "LuckPerms" \
    "$(echo "$LP_JSON"        | jq -r '.sha512 // empty' 2>/dev/null)"

# ── Done ──────────────────────────────────────────────────────────────────────
echo
echo -e "${BOLD}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"
success "All done!  MC ${YELLOW}${CURRENT_MC}${RESET} → ${GREEN}${TARGET_MC}${RESET}"
echo -e "${BOLD}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"
echo
info "Suggested next steps:"
echo "  git diff                          # review all patched files"
echo "  ./gradlew :fabric:runServer       # verify the updated setup boots"
echo
