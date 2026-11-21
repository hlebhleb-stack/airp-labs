#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

: "${CATALINA_HOME:=$HOME/Applications/apache-tomcat-9.0.124}"
export CATALINA_HOME
export CATALINA_BASE="${CATALINA_BASE:-$CATALINA_HOME}"
if [[ ! -x "$CATALINA_HOME/bin/catalina.sh" ]]; then
    echo "Укажи CATALINA_HOME с путём к Apache Tomcat 9." >&2
    exit 1
fi

if [[ "$(uname)" == Darwin ]]; then
    export JAVA_HOME="$(/usr/libexec/java_home -v 21)"
fi
if [[ ! -f target/airp-lab3.war || "${1:-}" != --run-built ]]; then
    maven=mvn
    if ! command -v "$maven" >/dev/null; then
        maven="$HOME/Applications/Apache NetBeans 32.app/Contents/Resources/netbeans/java/maven/bin/mvn"
    fi
    "$maven" package
fi
if [[ -f dictionary.db && ! -e "$CATALINA_BASE/dictionary.db" ]]; then
    cp -n dictionary.db "$CATALINA_BASE/dictionary.db"
fi
cp target/airp-lab3.war "$CATALINA_BASE/webapps/"
exec "$CATALINA_HOME/bin/catalina.sh" run
