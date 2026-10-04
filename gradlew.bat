@echo off
setlocal

echo This repository is configured for Gradle 9.3.1.
echo In GitHub Actions, Gradle 9.3.1 is installed by setup-gradle.
echo For local builds, install Gradle 9.3.1 or regenerate the standard wrapper JAR with:
echo   gradle wrapper --gradle-version 9.3.1
exit /b 1
