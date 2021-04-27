# OpenMediation SDK for Android
Thanks for taking a look at OpenMediation! We offers diversified and competitive monetization solution and supports a variety of Ad formats including Native Ad, Interstitial Ad, Banner Ad, and Rewarded Video Ad. The OpenMediation platform works with multiple ad networks include AdMob, Facebook, UnityAds, Vungle, AdColony, AppLovin, MoPub, Tapjoy, Chartboost and Mintegral etc.

## Communication

- If you **found a bug**, _and can provide steps to reliably reproduce it_, open an issue.
- If you **have a feature request**, open an issue.

## Installation

```
android {
  ...
  defaultConfig {
        minSdkVersion 16
    }
}

dependencies {
  implementation 'nbmediation:nm-android-sdk:2.1.1'

  // AdTiming-Adapter
  implementation 'nbmediation.adapters:adtiming:2.0.1'
  // AdMob-Adapter
  implementation 'nbmediation.adapters:admob:2.1.0'
  // Facebook-Adapter
  implementation 'nbmediation.adapters:facebook:2.1.0'
  // Unity-Adapter
  implementation 'nbmediation.adapters:unity:2.0.1'
  // Vungle-Adapter
  implementation 'nbmediation.adapters:vungle:2.1.1'
  // AdColony-Adapter
  implementation 'nbmediation.adapters:adcolony:2.1.0'
  // AppLovin-Adapter
  implementation 'nbmediation.adapters:applovin:2.1.1'
  // MoPub-Adapter
  implementation 'nbmediation.adapters:mopub:2.1.0'
  // Tapjoy-Adapter
  implementation 'nbmediation.adapters:tapjoy:2.0.0'
  // Chartboost-Adapter
  implementation 'nbmediation.adapters:chartboost:2.1.0'
  // Mintegral-Adapter
  implementation 'nbmediation.adapters:mintegral:2.1.2'
  //TikTok-Adapter
  implementation 'nbmediation.adapters:tiktok:2.1.0'
  //IronSource-Adapter
  implementation 'nbmediation.adapters:ironsource:2.1.0'
  //Fyber-Adapter
  implementation 'nbmediation.adapters:fyber:2.0.0'
  //Helium-Adapter
  implementation 'nbmediation.adapters:helium:2.1.1'
  // PubNative-Adapter
  implementation 'nbmediation.adapters:pubnative:1.1.0'
}
```

## ProGuard
```
-keep class nbmediation.sdk.** { *; }
```

## Requirements
We support Android Operating Systems Version 4.1 (API Level 16) and up. Be sure to:

- Use Android Studio 2.0 and up
- Target Android API level 28
- MinSdkVersion level 16 and up

## LICENSE
See the [LICENSE](LICENSE) file.


