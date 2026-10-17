# Shizuku reaches the shell through a private method resolved by name at
# runtime, so keep every member and name of both Shizuku packages.
-keep class rikka.shizuku.** { *; }
-keep class moe.shizuku.** { *; }
-dontwarn rikka.shizuku.**
-dontwarn moe.shizuku.**

# Keep annotations and generic signatures used by reflection/Compose.
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod

# Keep the app's entry points and Compose-generated members.
-keep class com.nexun.nexres.** { *; }
