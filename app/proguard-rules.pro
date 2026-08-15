# R8 / ProGuard rules for Google Play Protect & Release Optimization

# Keep Hilt / Dagger generated classes
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager
-keep class dagger.hilt.android.internal.lifecycle.HiltViewModelFactory
-keep class * implements dagger.hilt.internal.UnstableApi

# Keep Room DB models & DAOs
-keep class com.vibefy.musicwtf.data.db.** { *; }
-dontwarn androidx.room.paging.**

# Keep Kotlinx Serialization models
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
    @kotlinx.serialization.Serializable <fields>;
}

# Keep OkHttp & Retrofit
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }

# Keep Media3 / ExoPlayer
-keep class androidx.media3.exoplayer.** { *; }
-keep class androidx.media3.session.** { *; }

# Keep WebKit
-keep class android.webkit.** { *; }
