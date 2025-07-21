package com.hompimpa.comfylearn.ui.study.spelling

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.databinding.ActivitySpellingCategoryBinding
import com.hompimpa.comfylearn.helper.CategoryAdapter
import com.hompimpa.comfylearn.helper.GameContentProvider

class SpellingCategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySpellingCategoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySpellingCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.spelling_categories)

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val mainCategories = GameContentProvider.getGameCategories(this)
        val consonantCategories = resources.getStringArray(R.array.consonants).toList()
        val combinedCategories = mainCategories + consonantCategories

        binding.recyclerViewCategories.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerViewCategories.adapter =
            CategoryAdapter(combinedCategories) { selectedCategory ->
                val isConsonant = !mainCategories.contains(selectedCategory)
                val intent = Intent(this, SpellingDetailActivity::class.java).apply {
                    putExtra("CATEGORY_NAME", selectedCategory)
                    putExtra("IS_CONSONANT", isConsonant)
                }
                startActivity(intent)
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}