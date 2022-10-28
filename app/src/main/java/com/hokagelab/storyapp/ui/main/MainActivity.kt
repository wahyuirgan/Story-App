package com.hokagelab.storyapp.ui.main

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityOptionsCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.hokagelab.storyapp.R
import com.hokagelab.storyapp.adapter.StoryAdapter
import com.hokagelab.storyapp.data.source.local.entity.StoryEntity
import com.hokagelab.storyapp.databinding.ActivityMainBinding
import com.hokagelab.storyapp.databinding.ContentMainBinding
import com.hokagelab.storyapp.databinding.LayoutListStoryBinding
import com.hokagelab.storyapp.ui.uploadstory.AddStoryActivity
import com.hokagelab.storyapp.ui.SettingsActivity
import com.hokagelab.storyapp.ui.detail.DetailStoryActivity
import com.hokagelab.storyapp.ui.login.LoginActivity
import com.hokagelab.storyapp.ui.login.LoginViewModel
import com.hokagelab.storyapp.utils.LoginViewModelFactory
import com.hokagelab.storyapp.utils.StoryViewModelFactory
import com.hokagelab.storyapp.utils.Utils

@RequiresApi(Build.VERSION_CODES.O)
class MainActivity : AppCompatActivity(), Toolbar.OnMenuItemClickListener {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding
    private var _contentMainBinding: ContentMainBinding? = null
    private val contentMainBinding get() = _contentMainBinding
    var token: String? = ""
    private var dialog: Dialog? = null

    private val storyAdapter: StoryAdapter by lazy { StoryAdapter() }

    private val loginViewModel: LoginViewModel by viewModels {
        LoginViewModelFactory.getInstance(this)
    }

    private val mainViewModel: MainViewModel by viewModels {
        StoryViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        _contentMainBinding = binding?.contentMain
        setContentView(binding?.root)

        binding?.toolbar?.setOnMenuItemClickListener(this)

        token = intent.getStringExtra("token")
        val bundle = Bundle()
        bundle.putString("token", token)

        dialog = Dialog(this@MainActivity)

        binding?.fab?.setOnClickListener {
            val intent = Intent(this@MainActivity, AddStoryActivity::class.java)
            intent.putExtra("token", token)
            startActivity(intent)
        }

        contentMainBinding?.tvGreeting?.text = resources.getString(Utils.setGreeting())

        contentMainBinding?.swipeStory?.setOnRefreshListener {
            contentMainBinding?.rvListStory?.visibility = View.GONE
            contentMainBinding?.shimmerLayout?.visibility = View.VISIBLE
            showListStories()
            getListStories()
        }

        showListStories()
        getListStories()
        directToDetailStory()

        applyTheme()
    }

    private fun showListStories(){
        Handler(Looper.getMainLooper()).postDelayed({
            contentMainBinding?.swipeStory?.isRefreshing = false
            contentMainBinding?.rvListStory?.visibility = View.VISIBLE
            contentMainBinding?.shimmerLayout?.visibility = View.GONE
            contentMainBinding?.rvListStory?.apply {
                layoutManager = LinearLayoutManager(context)

                adapter = storyAdapter
            }
            if (storyAdapter.itemCount == 0){
                contentMainBinding?.baseNoStoryList?.root?.visibility = View.VISIBLE
                contentMainBinding?.rvListStory?.visibility = View.INVISIBLE
            }
        }, 3000)
    }

    private fun getListStories(){
        if (!token.equals("")) {
            mainViewModel.getStories("Bearer $token").observe(this) { result ->
                storyAdapter.submitData(lifecycle, result)
            }
        }
    }

    private fun directToDetailStory() {
        storyAdapter.setOnItemClickCallback(object : StoryAdapter.OnItemClickCallback {
            override fun onItemClicked(
                story: StoryEntity,
                view: LayoutListStoryBinding,
                itemView: View
            ) {
                val optionsCompat: ActivityOptionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        itemView.context as Activity,
                        androidx.core.util.Pair(view.ivStory, "photo"),
                        androidx.core.util.Pair(view.tvName, "name"),
                        androidx.core.util.Pair(view.tvCreatedAt, "createAt")
                    )
                val detailIntent = Intent(this@MainActivity, DetailStoryActivity::class.java)
                detailIntent.putExtra("detail_story", story)
                startActivity(detailIntent, optionsCompat.toBundle())
            }
        })
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.btn_logout -> {
                showLogoutDialog()
                true
            }
            R.id.btn_setting -> {
                val settingIntent = Intent(this@MainActivity, SettingsActivity::class.java)
                startActivity(settingIntent)
                true
            }
            else -> false
        }
    }

    private fun showLogoutDialog() {
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCancelable(false)
        dialog?.setContentView(R.layout.layout_dialog)
        val body = dialog?.findViewById(R.id.tvDialogInformation) as TextView
        body.text = getString(R.string.message_logout)
        val iconDialog = dialog?.findViewById(R.id.ivDialogLogo) as ImageView
        iconDialog.setImageResource(R.drawable.ic_logout_dialog)
        val layoutDialogConfirm = dialog?.findViewById(R.id.layoutConfirmation) as LinearLayout
        layoutDialogConfirm.visibility = View.VISIBLE
        val confirmBtnYes = dialog?.findViewById(R.id.btnDialogYes) as Button
        val confirmBtnNo = dialog?.findViewById(R.id.btnDialogNo) as Button
        confirmBtnYes.setOnClickListener {
            loginViewModel.deleteToken()
            val loginIntent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(loginIntent)
            finish()
        }
        confirmBtnNo.setOnClickListener {
            dialog?.dismiss()
        }
        dialog?.show()
    }

    private fun applyTheme() {
        PreferenceManager.getDefaultSharedPreferences(this)
            .getString(getString(R.string.key_app_theme), Utils.DEFAULT_MODE)?.apply {
                Utils.applyTheme(this)
            }
    }

    override fun onResume() {
        super.onResume()
        contentMainBinding?.shimmerLayout?.startShimmer()
    }

    override fun onPause() {
        contentMainBinding?.shimmerLayout?.stopShimmer()
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        dialog?.dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        dialog?.dismiss()
    }
}