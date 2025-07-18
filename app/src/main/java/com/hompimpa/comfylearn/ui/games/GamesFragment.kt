package com.hompimpa.comfylearn.ui.games

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.hompimpa.comfylearn.databinding.FragmentGamesBinding
import com.hompimpa.comfylearn.helper.GameContentProvider
import com.hompimpa.comfylearn.helper.setupScrollIndicator
import java.util.Locale

class GamesFragment : Fragment() {

    private var _binding: FragmentGamesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GamesViewModel by viewModels()

    private val activityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data ?: return@registerForActivityResult
            val gameType = data.getStringExtra(DifficultySelectionActivity.EXTRA_GAME_TYPE)
            val category = data.getStringExtra(DifficultySelectionActivity.EXTRA_GAME_CATEGORY)
            val difficulty =
                data.getStringExtra(DifficultySelectionActivity.EXTRA_SELECTED_DIFFICULTY)

            if (gameType != null && category != null && difficulty != null) {
                viewModel.onGameReady(gameType, category, difficulty)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGamesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        setupObservers()
        binding.scrollView.setupScrollIndicator(binding.scrollIndicator)
    }

    private fun setupClickListeners() {
        binding.buttonOpenGameDrawing.setOnClickListener {
            viewModel.onGameSelected("DRAWING")
        }
        binding.buttonOpenGameFill.setOnClickListener {
            showCategorySelectionDialog("FILL_IN")
        }
        binding.buttonOpenGamePuzzle.setOnClickListener {
            showCategorySelectionDialog("PUZZLE")
        }
        binding.buttonOpenGameArithmetic.setOnClickListener {
            viewModel.onDifficultySelectionNeeded("arithmetic", "MATH")
        }
    }

    private fun showCategorySelectionDialog(gameType: String) {
        val categories = GameContentProvider.getGameCategories(requireContext())
        val displayCategories = categories.map {
            it.replaceFirstChar { char -> char.titlecase(Locale.getDefault()) }
        }.toTypedArray()

        AlertDialog.Builder(requireContext())
            .setTitle("Select a Category")
            .setItems(displayCategories) { dialog, which ->
                viewModel.onDifficultySelectionNeeded(categories[which], gameType)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun setupObservers() {
        viewModel.navigationEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { navAction ->
                val intent = Intent(requireContext(), navAction.targetClass).apply {
                    putExtras(navAction.extras)
                }
                if (navAction.forResult) {
                    activityLauncher.launch(intent)
                } else {
                    startActivity(intent)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}