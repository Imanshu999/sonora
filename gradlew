#!/bin/sh

# Gradle Wrapper launcher for Unix-like systems.
# The GitHub Actions workflow installs Gradle 9.3.1 explicitly, so CI does not
# depend on a preinstalled Gradle binary or a missing wrapper JAR.

APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd -P)

if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
  JAVACMD="$JAVA_HOME/bin/java"
else
  JAVACMD="java"
fi

if ! command -v "$JAVACMD" >/dev/null 2>&1; then
  echo "ERROR: Java was not found. Install JDK 17 or set JAVA_HOME." >&2
  exit 1
fi

exec "$JAVACMD" -version >/dev/null 2>&1 || exit 1

echo "This repository is configured for Gradle 9.3.1."
echo "In GitHub Actions, use gradle/actions/setup-gradle with gradle-version: '9.3.1'."
echo "For local builds, install Gradle 9.3.1 or regenerate the standard wrapper JAR with:"
echo "  gradle wrapper --gradle-version 9.3.1"
exit 1
