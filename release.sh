#!/usr/bin/env bash

rm -rf release/*

gradle clean

cd adapter_cn

gradle build

cd ../adapter/

gradle build

cd ../adapter_host/host-online-plugin

gradle build

cd ../../nm-android-sdk

gradle build

