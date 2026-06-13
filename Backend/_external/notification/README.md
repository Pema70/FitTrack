# NotificationService — wyłączony z głównego buildu

`NotificationService.kt` używa Firebase Admin SDK (`com.google.firebase.messaging.*`) do wysyłania powiadomień push do urządzeń Android (FCM).

W wersji do lokalnego uruchomienia (SQLite, bez kluczy Firebase) ten serwis jest niepotrzebny — aplikacja Android i tak ma stub powiadomień (`WaterReminderWorker` używa lokalnego `NotificationManager`).

## Jak włączyć

1. Skopiuj `NotificationService.kt` do `backend/src/main/kotlin/com/fittrack/service/`
2. Dodaj do `build.gradle.kts`:
   ```kotlin
   implementation("com.google.firebase:firebase-admin:9.3.0")
   ```
3. Dodaj plik `firebase-service-account.json` (z Firebase Console → Project Settings → Service Accounts)
4. Skonfiguruj bean `FirebaseMessaging` w `FirebaseConfig.kt`:
   ```kotlin
   @Configuration
   class FirebaseConfig {
       @Bean
       fun firebaseApp(): FirebaseApp {
           val credentials = GoogleCredentials.fromStream(
               ClassPathResource("firebase-service-account.json").inputStream
           )
           val options = FirebaseOptions.builder().setCredentials(credentials).build()
           return FirebaseApp.initializeApp(options)
       }
       @Bean
       fun firebaseMessaging(app: FirebaseApp): FirebaseMessaging = FirebaseMessaging.getInstance(app)
   }
   ```
5. Dodaj `NotificationController` z endpointami `/api/notifications/token` i `/api/notifications/settings`
6. Dodaj encje `DeviceToken`, `NotificationSettings` oraz odpowiednie repozytoria
