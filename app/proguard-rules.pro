# Add project specific ProGuard rules here.
# Keep Room entities
-keep class com.example.follower.data.model.** { *; }

# Keep USB-related classes
-keep class com.example.follower.usb.** { *; }
