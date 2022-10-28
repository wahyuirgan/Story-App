package com.hokagelab.storyapp.ui.detail

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hokagelab.storyapp.R
import com.hokagelab.storyapp.data.source.local.entity.StoryEntity
import com.hokagelab.storyapp.databinding.ActivityDetailStoryBinding
import com.hokagelab.storyapp.utils.Utils

@RequiresApi(Build.VERSION_CODES.O)
class DetailStoryActivity : AppCompatActivity() {

    private var _binding: ActivityDetailStoryBinding? = null
    private val binding get() = _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityDetailStoryBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val dataDetail = intent.getParcelableExtra<StoryEntity>("detail_story")

        with(binding){
            if (dataDetail != null){
                this?.ivStoryDetail?.let {
                    Glide.with(this@DetailStoryActivity)
                        .load(dataDetail.photoUrl)
                        .apply(RequestOptions().placeholder(R.drawable.ic_image_placeholder))
                        .centerInside()
                        .into(it)
                }
                (getString(R.string.text_upload_by) + " " + dataDetail.name).also { this?.tvNameDetail?.text = it }
                this?.tvCreatedAtDetail?.text = Utils.withDateFormat(dataDetail.createdAt)
                this?.tvDescriptionDetail?.text = dataDetail.description
            }
        }

        playAnimation()

    }

    private fun playAnimation() {

        val imageDetail = ObjectAnimator.ofFloat(binding?.ivStoryDetail, View.ALPHA, 1f).setDuration(300)
        val name = ObjectAnimator.ofFloat(binding?.tvNameDetail, View.ALPHA, 1f).setDuration(500)
        val createAt = ObjectAnimator.ofFloat(binding?.tvCreatedAtDetail, View.ALPHA, 1f).setDuration(500)
        val desc = ObjectAnimator.ofFloat(binding?.tvDescriptionDetail, View.ALPHA, 1f).setDuration(500)

        AnimatorSet().apply {
            playSequentially(imageDetail, name, createAt, desc)
            start()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}