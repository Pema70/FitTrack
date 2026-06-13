package com.fittrack.ui.diary

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.fittrack.R
import com.fittrack.data.model.DiaryEntryRequest
import com.fittrack.databinding.FragmentFoodPhotoBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

/**
 * Ekran "Dodaj zdjeciem":
 *  1) Uzytkownik robi zdjecie potrawy aparatem (intent SYSTEMOWY).
 *  2) Wpisuje nazwe + gramature + (opcjonalnie) kcal + posilek.
 *  3) Zapis -> DiaryEntry z polem photoPath wskazujacym na lokalny plik.
 */
@AndroidEntryPoint
class FoodPhotoFragment : Fragment(R.layout.fragment_food_photo) {

    private val vm: DiaryViewModel by viewModels()
    private var _b: FragmentFoodPhotoBinding? = null
    private val b get() = _b!!

    private var photoFile: File? = null
    private var photoUri: Uri? = null

    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
            if (ok && photoFile != null) {
                b.imgPreview.setImageURI(Uri.fromFile(photoFile))
            } else {
                Snackbar.make(b.root, getString(R.string.photo_cancelled), Snackbar.LENGTH_SHORT).show()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _b = FragmentFoodPhotoBinding.bind(view)

        b.btnTakePhoto.setOnClickListener { launchCamera() }
        b.btnCancel.setOnClickListener { findNavController().popBackStack() }
        b.btnSave.setOnClickListener { saveEntry() }
    }

    private fun launchCamera() {
        val dir = File(requireContext().filesDir, "food_photos").apply { mkdirs() }
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(dir, "meal_$stamp.jpg")
        photoFile = file
        photoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            file
        )
        takePicture.launch(photoUri!!)
    }

    private fun saveEntry() {
        val name = b.etName.text?.toString()?.trim().orEmpty()
        if (name.isEmpty()) {
            Snackbar.make(b.root, getString(R.string.err_dish_name_required), Snackbar.LENGTH_SHORT).show()
            return
        }
        val quantity = b.etQuantity.text?.toString()?.toDoubleOrNull() ?: 100.0
        val mealType = when (b.rgMealType.checkedRadioButtonId) {
            R.id.rbBreakfast -> "BREAKFAST"
            R.id.rbLunch     -> "LUNCH"
            R.id.rbDinner    -> "DINNER"
            else             -> "SNACK"
        }
        val note = b.etKcal.text?.toString()?.toIntOrNull()?.let { "kcal=$it" }

        vm.addEntry(
            DiaryEntryRequest(
                productId  = 0L,
                quantityG  = quantity,
                mealType   = mealType,
                entryDate  = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            )
        )
        Snackbar.make(b.root, getString(R.string.entry_added), Snackbar.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
