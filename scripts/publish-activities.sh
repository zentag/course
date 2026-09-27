#!/usr/bin/env bash
# Publish each activity folder as its own repo (zentag/course-<name>) using git subtree split.
#   main      <- split of activities/<path> on course:main      (starter)
#   solutions <- split of activities/<path> on course:solutions (answer)
#
# Usage: scripts/publish-activities.sh [--force] [filter ...]
#   filter matches any part of the activity path, e.g. "1-enums" or "misc/interfaces".
set -euo pipefail

OWNER=zentag
BRANCHES=(main solutions)
PUSH_FLAGS=()
FILTERS=()

for arg in "$@"; do
	case "$arg" in
		--force) PUSH_FLAGS+=(--force) ;;
		*) FILTERS+=("$arg") ;;
	esac
done

cd "$(git rev-parse --show-toplevel)"

git fetch --quiet origin "${BRANCHES[@]}"
for b in "${BRANCHES[@]}"; do
	if [ -n "$(git rev-list "$b..origin/$b")" ]; then
		echo "warning: local $b is behind origin/$b" >&2
	fi
done

# activities/misc/<name> and activities/by-session/<session>/<name>, on any branch
list_activities() {
	local b=$1 session
	git ls-tree -d --name-only "$b" activities/misc/
	git ls-tree -d --name-only "$b" activities/by-session/ | while read -r session; do
		git ls-tree -d --name-only "$b" "$session/"
	done
}

activities=$(for b in "${BRANCHES[@]}"; do list_activities "$b"; done | sort -u)

matches_filter() {
	[ ${#FILTERS[@]} -eq 0 ] && return 0
	local f
	for f in "${FILTERS[@]}"; do
		[[ "$1" == *"$f"* ]] && return 0
	done
	return 1
}

for subdir in $activities; do
	matches_filter "$subdir" || continue

	name=${subdir#activities/}
	name=${name#by-session/}
	name=${name//\//-}
	repo=$OWNER/course-$name

	if ! gh repo view "$repo" >/dev/null 2>&1; then
		gh repo create "$repo" --public --description "Activity from $OWNER/course ($subdir)" >/dev/null
		echo "$repo: created"
	fi

	default=""
	for b in "${BRANCHES[@]}"; do
		if [ -z "$(git ls-tree -d "$b" -- "$subdir")" ]; then
			echo "$repo: $b skipped (not on course:$b)"
			continue
		fi
		sha=$(git subtree split --quiet --prefix="$subdir" "$b")
		git push --quiet "${PUSH_FLAGS[@]}" "https://github.com/$repo.git" "$sha:refs/heads/$b"
		echo "$repo: $b -> ${sha:0:7}"
		default=${default:-$b}
	done

	gh repo edit "$repo" --default-branch "$default" >/dev/null
done
