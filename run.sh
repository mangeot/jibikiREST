#!/bin/sh
cd -- "$(dirname -- "$0")"
dir=$(pwd)

# démarre docker
PROCESS=`ps A | grep 'Docker.app' | grep -v grep`
if [ "$?" -ne "0" ]; then
        echo "Docker not running"
        ### COMMAND TO EXECUTE HERE ###
        open -a "Docker"
        sleep 2
fi

# démarre le serveur de BD
cd /Users/mangeot/Projets/jibiki && docker compose -f compose-dbonly.yaml up -d

# compile le java
cd $dir
ant

# ouvre un navigateur
open http://localhost:8000/api/

#exécute le java
java -cp dist/lib/JibikiRest.jar:lib/postgresql-42.7.8.jar fr.jibiki.RestHttpServer

