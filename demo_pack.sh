#!/usr/bin/env bash


#rm -rf adapter/build
#rm -rf adapter/*.iml



mkdir om-sample

cp -r app libs adapter adapter_cn build.gradle config.gradle gradle.properties om-sample


find om-sample -type d -name "build" -exec rm -rf {} \;
find om-sample -type f -name "*.iml" -exec rm -rf {} \;

touch om-sample/settings.gradle
echo  "include ':adapter:applovin',
        ':adapter:chartboost',
        ':adapter:adcolony',
        ':adapter:mintegral',
        ':adapter:adtiming',
        ':adapter:tapjoy',
        ':adapter:vungle',
//        ':adapter:tiktok',
        ':adapter:ironsource',
        ':adapter:unity',
        ':adapter:facebook',
        ':adapter:mopub',
        ':adapter:fyber',
        ':adapter_cn:inmobi',
        ':adapter:admob',
        ':adapter:sigmob',
        ':app',
        ':adapter_cn:tiktok',
        ':adapter_cn:tencentad'
rootProject.name = 'om-sample'
include ':adapter_cn:hyadxopen',
        ':adapter_cn:cloudmobi',
        ':adapter_cn:tc056d',
        ':adapter_cn:alion',
        ':adapter_cn:zyt',
        ':adapter_cn:ks',
        ':adapter_cn:agsdk'" > om-sample/settings.gradle
zip -r om-sample.zip om-sample
rm -rf om-sample
mv om-sample.zip release/