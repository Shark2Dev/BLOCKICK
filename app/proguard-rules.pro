# Proguard rules for Blockick
# Keep dnsjava classes
-keep class org.xbill.DNS.** { *; }
-dontwarn org.xbill.DNS.**

# Keep Hilt/Dagger
-keep class dagger.hilt.android.internal.managers.** { *; }

# Keep Room
-keep class * extends androidx.room.RoomDatabase

