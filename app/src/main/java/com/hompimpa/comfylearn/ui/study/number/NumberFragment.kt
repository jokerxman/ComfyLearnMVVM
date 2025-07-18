package com.hompimpa.comfylearn.ui.study.number

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.databinding.FragmentNumberBinding
import com.hompimpa.comfylearn.helper.SoundManager

class NumberFragment : Fragment() {

    private var _binding: FragmentNumberBinding? = null
    private val binding get() = _binding!!

    private var currentNumber: Int = 0

    private val numberImages = mapOf(
        0 to R.drawable.number_0, 1 to R.drawable.number_1,
        2 to R.drawable.number_2, 3 to R.drawable.number_3,
        4 to R.drawable.number_4, 5 to R.drawable.number_5,
        6 to R.drawable.number_6, 7 to R.drawable.number_7,
        8 to R.drawable.number_8, 9 to R.drawable.number_9
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentNumber = it.getInt(ARG_NUMBER, 0)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNumberBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        displayNumber()
        binding.numberImageView.setOnClickListener {
            SoundManager.playSoundByName(requireContext(), currentNumber.toString())
        }
    }

    private fun displayNumber() {
        val imageResId = numberImages[currentNumber] ?: R.drawable.ic_placeholder_image
        binding.numberImageView.setImageResource(imageResId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_NUMBER = "number"

        fun newInstance(number: Int): NumberFragment {
            return NumberFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_NUMBER, number)
                }
            }
        }
    }
}