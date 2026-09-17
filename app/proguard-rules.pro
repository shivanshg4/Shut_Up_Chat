# Hilt
-keep class dagger.hilt.android.internal.managers.** { *; }
-keep class com.chat.shutup.di.** { *; }

# Room
-keep class com.chat.shutup.data.local.entity.** { *; }
-keep class com.chat.shutup.data.local.**Dao { *; }

# Retrofit
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**
-keepattributes Signature, InnerClasses, EnclosingMethod

# Kotlin Serialization
-keepattributes *Annotation*, EnclosingMethod, InnerClasses
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class com.chat.shutup.data.remote.dto.** {
    *** Companion;
}
-keep @kotlinx.serialization.Serializable class com.chat.shutup.data.remote.dto.** { *; }

# Firebase
-keep class com.google.firebase.** { *; }

# Google Maps
-keep class com.google.android.gms.maps.** { *; }
-keep class com.google.android.gms.location.** { *; }

# Agora
-keep class io.agora.** { *; }

# Coil
-keep class coil3.** { *; }
-dontwarn coil3.**

# Facebook
-keep class com.facebook.** { *; }

# ML Kit / CameraX
-keep class com.google.mlkit.** { *; }
-keep class androidx.camera.** { *; }
