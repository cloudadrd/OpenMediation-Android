#!/usr/bin/env bash
rm -rf app/build

rm app/app.iml

mkdir om-sample

cp -r app libs build.gradle config.gradle gradle.properties om-sample
touch om-sample/settings.gradle
echo "include ':app'" > om-sample/settings.gradle
zip -r om-sample.zip om-sample
mv om-sample.zip release/