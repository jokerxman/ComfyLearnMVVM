package com.hompimpa.comfylearn.ui.games.drawing

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.dhaval2404.colorpicker.ColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.hompimpa.comfylearn.databinding.ActivityDrawingBinding
import com.hompimpa.comfylearn.helper.setOnSoundClickListener

class DrawingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDrawingBinding
    private val viewModel: DrawingViewModel by viewModels()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) viewModel.saveDrawing(binding.board.exportBitmap())
            else showToast("Permission denied. Cannot save drawing.")
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDrawingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        setupObservers()
    }

    private fun setupClickListeners() {
        binding.btnClear.setOnSoundClickListener { binding.board.clear() }
        binding.btnSave.setOnSoundClickListener { checkPermissionAndSave() }
        binding.btnColoringPage.setOnSoundClickListener { viewModel.onColoringPageClicked() }
        binding.btnPickColor.setOnSoundClickListener { viewModel.onPickColorClicked() }
        binding.btnEraser.setOnSoundClickListener { viewModel.onSetEraserMode() }
        binding.btnBrushSmall.setOnSoundClickListener { viewModel.onSetBrushSize(10f) }
        binding.btnBrushMedium.setOnSoundClickListener { viewModel.onSetBrushSize(20f) }
        binding.btnBrushLarge.setOnSoundClickListener { viewModel.onSetBrushSize(40f) }
        binding.btnDrawShape.setOnSoundClickListener { viewModel.onSetShapeMode() }
    }

    private fun setupObservers() {
        viewModel.uiState.observe(this) { state ->
            binding.board.setPenColor(state.penColor)
            binding.board.setBrushSize(state.brushSize)
            binding.board.setEraserMode(state.isEraser)
            binding.board.setDrawingMode(state.drawingMode)
        }

        viewModel.showColorPicker.observe(this) { event ->
            event.getContentIfNotHandled()?.let { showColorPickerDialog(it) }
        }

        viewModel.showColoringPages.observe(this) { event ->
            event.getContentIfNotHandled()?.let { showColoringPageDialog(it) }
        }

        viewModel.newBackgroundImage.observe(this) { bitmap ->
            binding.board.setBackgroundImage(bitmap)
        }

        viewModel.saveResultToast.observe(this) { event ->
            event.getContentIfNotHandled()?.let { showToast(it) }
        }
    }

    private fun showColoringPageDialog(pages: Map<String, Int>) {
        val pageNames = pages.keys.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Choose a Coloring Page")
            .setItems(pageNames) { dialog, which ->
                pages[pageNames[which]]?.let { viewModel.onColoringPageSelected(it) }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showColorPickerDialog(initialColor: Int) {
        ColorPickerDialog.Builder(this)
            .setTitle("Pick Color")
            .setColorShape(ColorShape.SQAURE)
            .setDefaultColor(initialColor)
            .setColorListener { color, _ -> viewModel.onSetPenColor(color) }
            .show()
    }

    private fun checkPermissionAndSave() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.saveDrawing(binding.board.exportBitmap())
        } else {
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}