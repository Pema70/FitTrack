package com.fittrack.service

import com.fittrack.dto.*
import com.fittrack.entity.*
import com.fittrack.repository.*
import com.google.firebase.messaging.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NotificationService(
    private val userRepo: UserRepository,
    private val tokenRepo: DeviceTokenRepository,
    private val settingsRepo: NotificationSettingsRepository,
    private val fmc: FirebaseMessaging
) {
    @Transactional
    fun registerToken(email: String, req: DeviceTokenRequest) {
        val user = userRepo.findByEmail(email).orElseThrow()
        if (tokenRepo.findAllByUserId(user.id).none { it.fcmToken == req.fcmToken }) {
            tokenRepo.save(DeviceToken(user = user, fcmToken = req.fcmToken, deviceName = req.deviceName))
        }
    }

    fun sendWaterReminder(userId: Long) {
        val tokens = tokenRepo.findAllByUserId(userId)
        tokens.forEach { dt ->
            runCatching {
                fmc.send(Message.builder()
                    .setToken(dt.fcmToken)
                    .setNotification(Notification.builder()
                        .setTitle("💧 Czas na wodę!")
                        .setBody("Pamiętaj o odpowiednim nawodnieniu organizmu.")
                        .build())
                    .build())
            }
        }
    }

    @Transactional
    fun updateSettings(email: String, req: NotificationSettingsRequest) {
        val user = userRepo.findByEmail(email).orElseThrow()
        val settings = settingsRepo.findByUserId(user.id)
            .orElse(NotificationSettings(user = user))
        settings.waterReminders   = req.waterReminders
        settings.mealReminders    = req.mealReminders
        settings.workoutReminders = req.workoutReminders
        settingsRepo.save(settings)
    }
}
