# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-dontwarn com.androidquery.**
-keep class com.androidquery.** { *;}

# 如果接入了Bugly，需要添加如下配置
-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}

-keep class MTT.ThirdAppInfoNew {
    *;
}
-dontwarn dalvik.**
-dontwarn com.tencent.smtt.**
-keep class yaq.gdtadv{
    *;
}
-keep class com.qq.e.** {
    *;
}
-keep class com.tencent.** {
    *;
}
-keep class cn.mmachina.JniClient {
    *;
}
-keep class c.t.m.li.tsa.** {
    *;
}

-keep class c.t.maploc.** {
    *;
}

-dontwarn c.t.maploc.**
