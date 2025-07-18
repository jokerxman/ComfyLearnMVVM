package com.hompimpa.comfylearn.ui.study.alphabet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.databinding.FragmentAlphabetBinding
import com.hompimpa.comfylearn.helper.SoundManager
import com.hompimpa.comfylearn.helper.setOnSoundClickListener

class AlphabetFragment : Fragment() {

    private var _binding: FragmentAlphabetBinding? = null
    private val binding get() = _binding!!

    private var currentLetter: Char = 'A'

    private val letterImages = mapOf(
        'A' to R.drawable.letter_a, 'B' to R.drawable.letter_b,
        'C' to R.drawable.letter_c, 'D' to R.drawable.letter_d,
        'E' to R.drawable.letter_e, 'F' to R.drawable.letter_f,
        'G' to R.drawable.letter_g, 'H' to R.drawable.letter_h,
        'I' to R.drawable.letter_i, 'J' to R.drawable.letter_j,
        'K' to R.drawable.letter_k, 'L' to R.drawable.letter_l,
        'M' to R.drawable.letter_m, 'N' to R.drawable.letter_n,
        'O' to R.drawable.letter_o, 'P' to R.drawable.letter_p,
        'Q' to R.drawable.letter_q, 'R' to R.drawable.letter_r,
        'S' to R.drawable.letter_s, 'T' to R.drawable.letter_t,
        'U' to R.drawable.letter_u, 'V' to R.drawable.letter_v,
        'W' to R.drawable.letter_w, 'X' to R.drawable.letter_x,
        'Y' to R.drawable.letter_y, 'Z' to R.drawable.letter_z
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentLetter = it.getChar(ARG_LETTER, 'A')
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlphabetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        displayLetter()
        binding.letterImageView.setOnSoundClickListener {
            SoundManager.playSoundByName(requireContext(), currentLetter.toString())
        }
    }

    private fun displayLetter() {
        val imageResId = letterImages[currentLetter] ?: R.drawable.ic_placeholder_image
        binding.letterImageView.setImageResource(imageResId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_LETTER = "letter"

        fun newInstance(letter: Char): AlphabetFragment {
            return AlphabetFragment().apply {
                arguments = Bundle().apply {
                    putChar(ARG_LETTER, letter)
                }
            }
        }
    }
}