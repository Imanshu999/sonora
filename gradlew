#!/bin/sh
set -eu
if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
fi
echo "Gradle is not installed. Run this project through GitHub Actions or install Gradle 9.3.1." >&2
exit 1
