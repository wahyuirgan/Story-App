package com.hokagelab.storyapp.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hokagelab.storyapp.R
import com.hokagelab.storyapp.data.source.local.entity.StoryEntity
import com.hokagelab.storyapp.databinding.LayoutListStoryBinding
import com.hokagelab.storyapp.utils.Utils
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
class StoryAdapter : PagingDataAdapter<StoryEntity, StoryAdapter.ListViewHolder>(DIFF_CALLBACK) {

    private lateinit var onItemClickCallback: OnItemClickCallback

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val binding = LayoutListStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val story = getItem(position)
        if (story != null) {
            holder.bind(story)
        }
    }

    inner class ListViewHolder(private val binding: LayoutListStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(storyResponse: StoryEntity) {
            binding.tvName.text = storyResponse.name
            binding.tvCreatedAt.text = Utils.withDateFormat(storyResponse.createdAt)
            binding.ivStory.let {
                Glide.with(itemView.context)
                    .load(storyResponse.photoUrl)
                    .apply(RequestOptions().placeholder(R.drawable.ic_image_placeholder))
                    .centerInside()
                    .into(it)
            }
            itemView.setOnClickListener {
                onItemClickCallback.onItemClicked(storyResponse, binding, itemView)
            }
        }
    }

    interface OnItemClickCallback {
        fun onItemClicked(story: StoryEntity, view: LayoutListStoryBinding, itemView: View)
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<StoryEntity>() {
            override fun areItemsTheSame(oldItem: StoryEntity, newItem: StoryEntity): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: StoryEntity, newItem: StoryEntity): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}