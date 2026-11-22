#!/bin/sh
# Downloads the SQLite JDBC driver (and its small logging helper) into the lib folder.
# You only need to run this once. Requires an internet connection.
cd "$(dirname "$0")" || exit 1
mkdir -p lib
BASE=https://repo1.maven.org/maven2
fetch() {
    if command -v curl >/dev/null 2>&1; then curl -fL -o "lib/$2" "$BASE/$1/$2"
    elif command -v wget >/dev/null 2>&1; then wget -O "lib/$2" "$BASE/$1/$2"
    else echo "Please install curl or wget, or download the jars manually (see README.md)."; return 1; fi
}
fetch org/xerial/sqlite-jdbc/3.45.3.0 sqlite-jdbc-3.45.3.0.jar || exit 1
fetch org/slf4j/slf4j-api/1.7.36 slf4j-api-1.7.36.jar || exit 1
echo "Done. Driver jars are in the lib folder."
