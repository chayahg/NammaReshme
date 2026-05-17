# Firebase Firestore model preservation
-keepclassmembers class com.example.nammareshme.models.** {
    *;
}

# Preserve Firebase classes
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
