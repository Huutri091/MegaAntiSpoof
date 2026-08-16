#!/bin/sh
exec "$(dirname "$0")/blockmodclient/Auto-Clicker/gradlew" -p "$(dirname "$0")" "$@"
