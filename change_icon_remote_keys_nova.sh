#!/bin/sh
tmp=/data/local/tmp
queryfile=$tmp/queryico.sql
imgdir=/sdcard/TaskerJS/ico
mainqueryfile=$tmp/mainquery.sql
resultsfile=$tmp/mainqueryres.sh
dbpath=/data/data/com.teslacoilsw.launcher/databases/launcher.db
echo "SELECT 'name' || (select count(*) FROM favorites AS B WHERE B._id<=A._id AND title NOT NULL) || '=\"' || title || '\";id' || (select count(*) FROM favorites AS B WHERE B._id<=A._id AND title NOT NULL) || '=' || _id || ';lnk' || (select count(*) FROM favorites AS B WHERE B._id<=A._id AND title NOT NULL) || '=\"' || intent || '\"' FROM favorites AS A WHERE title NOT NULL ORDER BY _id;" > "$mainqueryfile"
sqlite3 "$dbpath" < "$mainqueryfile" > "$resultsfile"
source "$resultsfile"
lastid=1
echo -n > "$queryfile"
while true; do
    var=name$lastid
    name=$(eval echo "\$$var")
    var=lnk$lastid
    lnk="$(eval echo "\$$var")"
    var=id$lastid
    key=$(eval echo "\$$var")
    if [ -z "$name" ]; then
        break
    fi
    echo "Proc $name"
    lastid=$((lastid+1))
    cont=$(echo $lnk | grep emitir)
    if [ -z "$cont" ]; then
        continue
    fi
    fp=$(echo $name | cut -d':' -f1)
    sp=$(echo $name | cut -d':' -f2)
    if [ -z "$sp" ]; then
        sp=$(echo $fp | cut -c2-)
    fi
    fl=$imgdir/$sp.png
    if [ ! -f "$fl" ]; then
        fl=$imgdir/default.png
    fi
    echo "UPDATE favorites set icon=readfile('$fl') WHERE _id==$key;" >> "$queryfile"
done
sqlite3 "$dbpath" < "$queryfile"
