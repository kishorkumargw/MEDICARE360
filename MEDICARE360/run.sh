#!/bin/sh
# Compiles and starts MEDICARE360 (macOS / Linux).
cd "$(dirname "$0")" || exit 1

if ! ls lib/sqlite-jdbc*.jar >/dev/null 2>&1; then
    echo "SQLite driver not found in lib/ - downloading it now..."
    sh ./setup-driver.sh || exit 1
fi

mkdir -p out
javac --release 17 -encoding UTF-8 -d out -sourcepath src src/com/medicare360/Main.java || exit 1
java -cp "out:lib/*" com.medicare360.Main
