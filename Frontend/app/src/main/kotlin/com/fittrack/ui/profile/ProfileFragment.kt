package com.fittrack.ui.profile

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fittrack.R
import com.fittrack.data.model.ProfileUpdateRequest
import com.fittrack.databinding.FragmentProfileBinding
import com.fittrack.notification.CustomNotificationReceiver
import com.fittrack.util.Resource
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar

@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val vm: ProfileViewModel by viewModels()
    private var _b: FragmentProfileBinding? = null
    private val b get() = _b!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _b = FragmentProfileBinding.bind(view)

        lifecycleScope.launch {
            vm.profile.collect { state ->
                b.progressBar.isVisible = state is Resource.Loading
                if (state is Resource.Success) {
                    val p = state.data
                    b.etName.setText(p.displayName ?: "")
                    b.etWeight.setText(p.weightKg?.toString() ?: "")
                    b.etHeight.setText(p.heightCm?.toString() ?: "")
                    b.etBirthDate.setText(p.birthDate ?: "")
                    b.tvKcalGoal.text = "Cel kaloryczny: ${p.dailyKcalGoal ?: "—"} kcal"
                    when (p.gender) {
                        "MALE"   -> b.rgGender.check(R.id.rbMale)
                        "FEMALE" -> b.rgGender.check(R.id.rbFemale)
                    }
                    val activityValues = resources.getStringArray(R.array.activity_levels_values)
                    val idx = activityValues.indexOf(p.activityLevel ?: "")
                    if (idx >= 0) b.spinnerActivity.setSelection(idx)

                    when (p.goal) {
                        "LOSE_WEIGHT"  -> b.rgGoal.check(R.id.rbLose)
                        "MAINTAIN"     -> b.rgGoal.check(R.id.rbMaintain)
                        "GAIN_MUSCLE"  -> b.rgGoal.check(R.id.rbGain)
                    }
                }
            }
        }

        b.etBirthDate.setOnClickListener { showDatePicker() }

        b.btnSave.setOnClickListener {
            val activityValues = resources.getStringArray(R.array.activity_levels_values)
            val activityLevel = activityValues.getOrNull(b.spinnerActivity.selectedItemPosition)

            vm.updateProfile(ProfileUpdateRequest(
                displayName   = b.etName.text.toString().ifBlank { null },
                gender        = when (b.rgGender.checkedRadioButtonId) {
                    R.id.rbMale   -> "MALE"
                    R.id.rbFemale -> "FEMALE"
                    else          -> null
                },
                birthDate     = b.etBirthDate.text.toString().ifBlank { null },
                weightKg      = b.etWeight.text.toString().toDoubleOrNull(),
                heightCm      = b.etHeight.text.toString().toDoubleOrNull(),
                activityLevel = activityLevel,
                goal          = when (b.rgGoal.checkedRadioButtonId) {
                    R.id.rbLose     -> "LOSE_WEIGHT"
                    R.id.rbMaintain -> "MAINTAIN"
                    R.id.rbGain     -> "GAIN_MUSCLE"
                    else            -> null
                }
            ))
        }

        lifecycleScope.launch {
            vm.saveState.collect { state ->
                when (state) {
                    is Resource.Success ->
                        Snackbar.make(view, "Profil zapisany ✓", Snackbar.LENGTH_SHORT).show()
                    is Resource.Error ->
                        Snackbar.make(view, state.message, Snackbar.LENGTH_LONG).show()
                    else -> {}
                }
            }
        }

        b.btnChangePassword.setOnClickListener { showChangePasswordDialog() }

        b.btnNotificationSettings.setOnClickListener { showNotificationSettingsDialog() }

        b.btnLogout.setOnClickListener {
            vm.logout()
            findNavController().navigate(R.id.profile_to_login)
        }
    }

    private fun showDatePicker() {
        val c = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, y, m, d ->
            val dateStr = String.format("%04d-%02d-%02d", y, m + 1, d)
            b.etBirthDate.setText(dateStr)
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun showChangePasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val etOld  = dialogView.findViewById<EditText>(R.id.etOldPassword)
        val etNew  = dialogView.findViewById<EditText>(R.id.etNewPassword)
        val etNew2 = dialogView.findViewById<EditText>(R.id.etNewPasswordConfirm)

        AlertDialog.Builder(requireContext())
            .setTitle("Zmień hasło")
            .setView(dialogView)
            .setPositiveButton("Zapisz") { _, _ ->
                val old  = etOld.text.toString()
                val new  = etNew.text.toString()
                val new2 = etNew2.text.toString()
                when {
                    old.isBlank() || new.isBlank() ->
                        Snackbar.make(b.root, "Wypełnij wszystkie pola", Snackbar.LENGTH_SHORT).show()
                    new.length < 8 || !new.any { it.isDigit() } ->
                        Snackbar.make(b.root, "Min. 8 znaków i jedna cyfra", Snackbar.LENGTH_SHORT).show()
                    new != new2 ->
                        Snackbar.make(b.root, "Nowe hasła nie są zgodne", Snackbar.LENGTH_SHORT).show()
                    else -> vm.changePassword(old, new)
                }
            }
            .setNegativeButton("Anuluj", null)
            .show()

        lifecycleScope.launch {
            vm.passwordState.collect { state ->
                when (state) {
                    is Resource.Success ->
                        Snackbar.make(b.root, "Hasło zmienione ✓", Snackbar.LENGTH_SHORT).show()
                    is Resource.Error ->
                        Snackbar.make(b.root, state.message, Snackbar.LENGTH_LONG).show()
                    else -> {}
                }
            }
        }
    }

    private fun showNotificationSettingsDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_notification_settings, null)
        val btnTime = view.findViewById<Button>(R.id.btnPickTime)
        val etContent = view.findViewById<EditText>(R.id.etNotificationContent)
        val chipWorkout = view.findViewById<Chip>(R.id.chipWorkout)
        val chipWater = view.findViewById<Chip>(R.id.chipWater)

        var selectedHour = 8
        var selectedMinute = 0

        btnTime.setOnClickListener {
            TimePickerDialog(requireContext(), { _, h, m ->
                selectedHour = h
                selectedMinute = m
                btnTime.text = String.format("Godzina: %02d:%02d", h, m)
            }, selectedHour, selectedMinute, true).show()
        }

        chipWorkout.setOnClickListener { etContent.setText("Czas na trening! Ruszaj się!") }
        chipWater.setOnClickListener { etContent.setText("Pamiętaj o nawodnieniu! Pij wodę.") }

        AlertDialog.Builder(requireContext())
            .setTitle("Ustawienia powiadomień")
            .setView(view)
            .setPositiveButton("Zapisz") { _, _ ->
                val content = etContent.text.toString()
                if (content.isNotBlank()) {
                    scheduleNotification(selectedHour, selectedMinute, content)
                    Snackbar.make(b.root, "Powiadomienie zaplanowane na $selectedHour:$selectedMinute", Snackbar.LENGTH_SHORT).show()
                } else {
                    Snackbar.make(b.root, "Wpisz treść powiadomienia", Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Anuluj", null)
            .show()
    }

    private fun scheduleNotification(hour: Int, minute: Int, message: String) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), CustomNotificationReceiver::class.java).apply {
            putExtra("title", "FitTrack")
            putExtra("message", message)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            200,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}