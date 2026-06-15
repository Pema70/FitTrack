package com.fittrack.ui.recipes

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fittrack.R
import com.fittrack.data.model.RecipeRequest
import com.fittrack.databinding.FragmentAddRecipeBinding
import com.fittrack.util.Resource
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class AddRecipeFragment : Fragment(R.layout.fragment_add_recipe) {

    private val vm: RecipeViewModel by viewModels()
    private var _b: FragmentAddRecipeBinding? = null
    private val b get() = _b!!

    private var photoFile: File? = null
    private var photoUri: Uri? = null

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) openCamera()
            else Snackbar.make(b.root, "Brak uprawnień do aparatu", Snackbar.LENGTH_LONG).show()
        }

    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
            if (ok && photoFile != null) {
                b.ivPreview.isVisible = true
                b.ivPreview.setImageURI(Uri.fromFile(photoFile))
            } else {
                Snackbar.make(b.root, "Anulowano zdjęcie", Snackbar.LENGTH_SHORT).show()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _b = FragmentAddRecipeBinding.bind(view)

        b.btnCapturePhoto.setOnClickListener {
            launchCamera()
        }

        b.btnSave.setOnClickListener {
            saveRecipe()
        }

        lifecycleScope.launch {
            vm.addRecipeState.collect { state ->
                b.progressBar.isVisible = state is Resource.Loading
                b.btnSave.isEnabled = state !is Resource.Loading

                when (state) {
                    is Resource.Success -> {
                        Snackbar.make(view, "Przepis dodany!", Snackbar.LENGTH_SHORT).show()
                        vm.resetAddRecipeState()
                        findNavController().popBackStack()
                    }
                    is Resource.Error -> {
                        Snackbar.make(view, state.message, Snackbar.LENGTH_LONG).show()
                        vm.resetAddRecipeState()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun launchCamera() {
        if (requireContext().checkSelfPermission(android.Manifest.permission.CAMERA)
            == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            openCamera()
        } else {
            requestPermission.launch(android.Manifest.permission.CAMERA)
        }
    }

    private fun openCamera() {
        val dir = File(requireContext().filesDir, "recipe_photos").apply { mkdirs() }
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(dir, "recipe_$stamp.jpg")
        photoFile = file
        photoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            file
        )
        takePicture.launch(photoUri!!)
    }

    private fun saveRecipe() {
        val title = b.etTitle.text.toString().trim()
        val desc = b.etDescription.text.toString().trim()
        val kcal = b.etKcal.text.toString().toDoubleOrNull()
        val protein = b.etProtein.text.toString().toDoubleOrNull()
        val fat = b.etFat.text.toString().toDoubleOrNull()
        val carbs = b.etCarbs.text.toString().toDoubleOrNull()
        val time = b.etTime.text.toString().toIntOrNull() ?: 1
        val servings = b.etServings.text.toString().toIntOrNull() ?: 1
        val tagsInput = b.etTags.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val imageUrl = photoFile?.absolutePath

        if (title.isEmpty()) {
            b.etTitle.error = "Tytuł jest wymagany"
            return
        }

        val validatedTime = if (time < 1) 1 else time

        val request = RecipeRequest(
            title = title,
            description = desc,
            kcalPerServing = kcal,
            proteinG = protein,
            fatG = fat,
            carbsG = carbs,
            prepTimeMin = validatedTime,
            servings = servings,
            imageUrl = imageUrl,
            tags = tagsInput,
            ingredients = emptyList()
        )

        vm.addCustomRecipe(request)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}