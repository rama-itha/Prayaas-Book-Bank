# Keep data model classes for Firestore deserialization
-keep class com.prayaas.bookbank.data.model.** { *; }

# Keep QrPayload for Gson serialization/deserialization
-keepclassmembers class com.prayaas.bookbank.data.model.QrPayload { *; }

# ZXing
-keep class com.google.zxing.** { *; }
-keep class com.journeyapps.** { *; }

# Firebase
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
