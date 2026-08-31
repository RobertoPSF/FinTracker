-keep class net.zetetic.database.** { *; }
-keep class net.sqlcipher.** { *; }
-keepclassmembers class com.rfilh.fintracker.parser.** { *; }

# androidx.security-crypto (EncryptedSharedPreferences) traz o Google Tink, que referencia
# anotações opcionais (error-prone, JSR305) que não existem em runtime — sem isso o R8
# falha o build com "Missing classes" mesmo elas nunca sendo usadas de verdade.
-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.annotation.**
