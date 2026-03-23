# Keep Room entities
-keep class com.astrolog.app.data.entity.** { *; }
-keep class com.astrolog.app.data.dao.** { *; }

# Keep Apache POI
-keep class org.apache.poi.** { *; }
-dontwarn org.apache.poi.**
-dontwarn org.openxmlformats.**
-dontwarn com.microsoft.**
-dontwarn org.etsi.**
-dontwarn org.w3c.**

# Keep WorkManager worker
-keep class com.astrolog.app.util.VisibilityAlertWorker { *; }
-keep class com.astrolog.app.util.BootReceiver { *; }
