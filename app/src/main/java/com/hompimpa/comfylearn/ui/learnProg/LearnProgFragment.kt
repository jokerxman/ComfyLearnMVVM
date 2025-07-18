package com.hompimpa.comfylearn.ui.learnProg

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.hompimpa.comfylearn.databinding.FragmentLearnprogBinding
import com.hompimpa.comfylearn.helper.ProgressionAdapter
import com.hompimpa.comfylearn.ui.games.DifficultySelectionActivity
import com.hompimpa.comfylearn.ui.games.drawing.DrawingActivity
import com.hompimpa.comfylearn.ui.games.mathgame.MathGameActivity
import com.hompimpa.comfylearn.ui.games.puzzle.PuzzleActivity
import com.hompimpa.comfylearn.ui.study.alphabet.AlphabetActivity
import com.hompimpa.comfylearn.ui.study.arithmetic.ArithmeticActivity
import com.hompimpa.comfylearn.ui.study.number.NumberActivity
import com.hompimpa.comfylearn.ui.study.spelling.SpellingDetailActivity

class LearnProgFragment : Fragment() {

    private var _binding: FragmentLearnprogBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LearnProgViewModel by viewModels()
    private lateinit var progressionAdapter: ProgressionAdapter

    private val gameLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.loadProgression()
    }

    private val difficultyLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data ?: return@registerForActivityResult
            val gameType = data.getStringExtra(DifficultySelectionActivity.EXTRA_GAME_TYPE)
            val category = data.getStringExtra(DifficultySelectionActivity.EXTRA_GAME_CATEGORY)
            val difficulty =
                data.getStringExtra(DifficultySelectionActivity.EXTRA_SELECTED_DIFFICULTY)

            if (gameType != null && category != null && difficulty != null) {
                launchGameWithDifficulty(gameType, category, difficulty)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLearnprogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadProgression()
    }

    private fun setupRecyclerView() {
        progressionAdapter = ProgressionAdapter(emptyList()) { item ->
            handleItemClick(item)
        }
        binding.progressionRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = progressionAdapter
        }
    }

    private fun setupObservers() {
        viewModel.progressionItems.observe(viewLifecycleOwner) { items ->
            binding.noDataTextView.isVisible = items.isEmpty()
            progressionAdapter = ProgressionAdapter(items) { item ->
                handleItemClick(item)
            }
            binding.progressionRecyclerView.adapter = progressionAdapter
        }
    }

    private fun handleItemClick(item: ProgressionItem) {
        val intent: Intent?

        when (item.gameType) {
            "MATH", "PUZZLE" -> {
                val difficultyIntent = DifficultySelectionActivity.newIntent(
                    requireContext(),
                    item.category,
                    item.gameType
                )
                difficultyLauncher.launch(difficultyIntent)
                return
            }

            "ALPHABET" -> intent = Intent(activity, AlphabetActivity::class.java)
            "NUMBER" -> intent = Intent(activity, NumberActivity::class.java)
            "ARITHMETIC" -> intent = Intent(activity, ArithmeticActivity::class.java)
            "DRAWING" -> intent = Intent(activity, DrawingActivity::class.java)
            "SPELLING" -> intent = Intent(activity, SpellingDetailActivity::class.java).apply {
                putExtra("category", item.category)
            }

            else -> {
                intent = null
                Toast.makeText(requireContext(), "Coming soon!", Toast.LENGTH_SHORT).show()
            }
        }

        intent?.let { gameLauncher.launch(it) }
    }

    private fun launchGameWithDifficulty(gameType: String, category: String, difficulty: String) {
        val intent = when (gameType) {
            "PUZZLE" -> Intent(requireContext(), PuzzleActivity::class.java)
            "MATH" -> Intent(requireContext(), MathGameActivity::class.java)
            else -> null
        }

        intent?.apply {
            putExtra("CATEGORY", category)
            putExtra("DIFFICULTY", difficulty)
            putExtra(MathGameActivity.EXTRA_SELECTED_DIFFICULTY, difficulty)
        }?.let {
            gameLauncher.launch(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}