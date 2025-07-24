# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserve line numbers for debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep annotation information
-keepattributes *Annotation*

# Appwrite SDK
-keep class io.appwrite.** { *; }
-keep class io.appwrite.models.** { *; }
-keep class io.appwrite.services.** { *; }
-keepclassmembers class io.appwrite.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Keep data models
-keep class com.example.snacktrack.data.model.** { *; }
-keepclassmembers class com.example.snacktrack.data.model.** { *; }

# Keep ViewModels
-keep class com.example.snacktrack.ui.viewmodel.** { *; }

# Jetpack Compose
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }
-keep class androidx.activity.** { *; }

# Coil
-keep class coil.** { *; }

# ZXing for barcode scanning
-keep class com.google.zxing.** { *; }
-keep class com.journeyapps.** { *; }

# CameraX
-keep class androidx.camera.** { *; }
-keepclassmembers class androidx.camera.** { *; }

# Navigation
-keep class androidx.navigation.** { *; }

# Datastore
-keep class androidx.datastore.** { *; }

# Remove Logging in Release
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# Keep Parcelable implementations
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# Keep Serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}