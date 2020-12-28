#!/usr/bin/env bash


#rm -rf adapter/build
#rm -rf adapter/*.iml



mkdir om-sample

cp -r app libs adapter adapter_cn build.gradle config.gradle gradle.properties om-sample

rm -r om-sample/adapter/{adcolony,admob,adtiming,fyber,applovin,chartboost,facebook,fyber,ironsource,mopub,tapjoy,tiktok,unity,vungle}
rm -r om-sample/adapter_cn/{alion,tc056d,hyadxopen,inmobi,zyt}

find om-sample -type d -name "build" -exec rm -rf {} \;
find om-sample -type f -name "*.iml" -exec rm -rf {} \;



touch om-sample/settings.gradle
echo  "include ':adapter:mintegral',
        ':adapter:sigmob',
        ':app',
rootProject.name = 'om-sample'
include ':adapter_cn:cloudmobi',
        ':adapter_cn:tiktok',
        ':adapter_cn:baidu',
        ':adapter_cn:tencentad',
        ':adapter_cn:ks',
        ':adapter_cn:agsdk'" > om-sample/settings.gradle
zip -r om-sample.zip om-sample
rm -rf om-sample
mv om-sample.zip release/