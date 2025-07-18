package com.hompimpa.comfylearn.ui.study

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.hompimpa.comfylearn.databinding.FragmentStudyBinding
import com.hompimpa.comfylearn.helper.setOnSoundClickListener
import com.hompimpa.comfylearn.helper.setupScrollIndicator
import com.hompimpa.comfylearn.ui.study.spelling.SpellingCategoryActivity

class StudyFragment : Fragment() {

    private var _binding: FragmentStudyBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StudyViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        setupObservers()
        binding.scrollView.setupScrollIndicator(binding.scrollIndicator)
    }

    private fun setupClickListeners() {
        binding.buttonOpenAlphabet.setOnSoundClickListener {
            viewModel.onAlphabetSelected('A')
        }
        binding.buttonOpenSpelling.setOnSoundClickListener {
            startActivity(Intent(requireContext(), SpellingCategoryActivity::class.java))
        }
        binding.buttonOpenArithmetic.setOnSoundClickListener {
            viewModel.onArithmeticSelected()
        }
    }

    private fun setupObservers() {
        viewModel.navigationEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { (activityClass, bundle) ->
                val intent = Intent(requireContext(), activityClass)
                bundle?.let { intent.putExtras(it) }
                startActivity(intent)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}