package com.example.socialnetwork.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.socialnetwork.R
import com.example.socialnetwork.adpters.PostAdapter
import com.example.socialnetwork.clients.ClientUtils
import com.example.socialnetwork.fragments.ConfirmationDialogFragment
import com.example.socialnetwork.model.entity.EReportReason
import com.example.socialnetwork.model.entity.Post
import com.example.socialnetwork.utils.PreferencesManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody


class PostsActivity : AppCompatActivity(),
    PostAdapter.CommentButtonClickListener,
    PostAdapter.ReportButtonClickListener,
    PostAdapter.DeleteButtonClickListener,
    ConfirmationDialogFragment.ConfirmationDialogListener  {

    private lateinit var createPostTextView: EditText
    private lateinit var dimBackgroundView: View
    private lateinit var createPostPopup: RelativeLayout
    private lateinit var errorMessageTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_posts)

        val token = PreferencesManager.getToken(this)
        if (token == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        setupBottomNavigation()

        showPostPopup()

        fillReportSpinner()

        fetchPostsFromServer(token)

        findViewById<Button>(R.id.sort).setOnClickListener {
            performLogout()
        }

    }
    override fun onCommentButtonClick(post: Post) {
        val commentActivity = CommentActivity()
        commentActivity.show(supportFragmentManager, "commentActivity")
    }
    override fun onReportButtonClick(post: Post) {
        showReportPopup()
    }

    override fun onDeleteButtonClick() {
        showConfirmationDialog()
    }

    override fun onDialogPositiveClick() {
        Toast.makeText(this, "Post deleted", Toast.LENGTH_SHORT).show()
    }

    override fun onDialogNegativeClick() {
        Toast.makeText(this, "Delete canceled", Toast.LENGTH_SHORT).show()
    }
    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        bottomNavigationView.selectedItemId = R.id.bottom_home

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_home -> true
                R.id.bottom_search -> {
                    startActivity(Intent(applicationContext, SearchActivity::class.java))
                    overrideActivityTransition(OVERRIDE_TRANSITION_OPEN,
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                    )
                    finish()
                    true
                }
                R.id.bottom_groups -> {
                    startActivity(Intent(applicationContext, GroupsActivity::class.java))
                    overrideActivityTransition(OVERRIDE_TRANSITION_OPEN,
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                    )
                    finish()
                    true
                }
                R.id.bottom_notifications -> {
                    startActivity(Intent(applicationContext, NotificationsActivity::class.java))
                    overrideActivityTransition(OVERRIDE_TRANSITION_OPEN,
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                    )
                    finish()
                    true
                }
                R.id.bottom_profile -> {
                    startActivity(Intent(applicationContext, ProfileActivity::class.java))
                    overrideActivityTransition(OVERRIDE_TRANSITION_OPEN,
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                    )
                    finish()
                    true
                }
                else -> false
            }
        }
    }
    private fun showPostPopup() {
        val createPostButton = findViewById<Button>(R.id.createPostButton)
        createPostTextView = findViewById(R.id.popupPostEditText)
        dimBackgroundView = findViewById(R.id.dimBackgroundView)
        createPostPopup = findViewById(R.id.createPostPopup)
        val closePopupButton = findViewById<ImageView>(R.id.closePostPopupButton)
        val popupCreatePostButton = findViewById<Button>(R.id.popupCreatePostButton)
        errorMessageTextView = findViewById(R.id.post_error_message)


        createPostButton.setOnClickListener {
            dimBackgroundView.visibility = View.VISIBLE
            createPostPopup.visibility = View.VISIBLE
        }

        closePopupButton.setOnClickListener {
            dismissPostPopup()
        }

        popupCreatePostButton.setOnClickListener {
            handleCreatePost()
        }
    }
    private fun dismissPostPopup() {
        dimBackgroundView.visibility = View.GONE
        createPostPopup.visibility = View.GONE
        errorMessageTextView.visibility = View.GONE
        createPostTextView.text.clear()

        createPostTextView.background = ContextCompat.getDrawable(this, R.drawable.edit_text_border)
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(createPostPopup.windowToken, 0)
    }
    private fun showReportPopup() {
        val reportPopup = findViewById<RelativeLayout>(R.id.createReportPopup)
        val dimBackgroundView = findViewById<View>(R.id.dimBackgroundView)
        val closeButton = findViewById<ImageView>(R.id.closeReportPopupButton)

        reportPopup.visibility = View.VISIBLE
        dimBackgroundView.visibility = View.VISIBLE

        closeButton.setOnClickListener {
            reportPopup.visibility = View.GONE
            dimBackgroundView.visibility = View.GONE
        }

        dimBackgroundView.setOnClickListener {
            reportPopup.visibility = View.GONE
            dimBackgroundView.visibility = View.GONE
        }
    }
    private fun fillReportSpinner() {
        val reportReasons = EReportReason.values().map { it.name.replace('_', ' ') }

        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, reportReasons)

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val reasonSpinner: Spinner = findViewById(R.id.popupReasonSpinner)
        reasonSpinner.adapter = spinnerAdapter
    }
    private fun showConfirmationDialog() {
        val dialog = ConfirmationDialogFragment()
        dialog.setMessage(getString(R.string.confirm_delete_post))
        dialog.listener = this
        dialog.show(supportFragmentManager, "ConfirmationDialog")
    }
    private fun fetchPostsFromServer(token: String) {
        val postService = ClientUtils.getPostService(token)
        val call = postService.getAll()

        call.enqueue(object : Callback<ArrayList<Post>> {
            override fun onResponse(
                call: Call<ArrayList<Post>>,
                response: Response<ArrayList<Post>>
            ) {
                if (response.isSuccessful) {
                    val posts = response.body() ?: arrayListOf()
                    updateListView(posts)
                } else if (response.code() == 401) {
                    handleTokenExpired()
                } else {
                    showToast("Failed to load posts")
                }
            }

            override fun onFailure(call: Call<ArrayList<Post>>, t: Throwable) {
                showToast("Error: ${t.message}")
            }
        })
    }
    private fun handleTokenExpired() {
        PreferencesManager.clearToken(this)
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
    private fun updateListView(posts: ArrayList<Post>) {
        val listView: ListView = findViewById(R.id.postsListView)
        val adapter = PostAdapter(this, posts)
        adapter.commentButtonClickListener = this
        adapter.reportButtonClickListener = this
        adapter.deleteButtonClickListener = this
        listView.adapter = adapter
    }

    private fun handleCreatePost() {
        val token = PreferencesManager.getToken(this) ?: return
        val postContent = createPostTextView.text.toString().trim()

        if (postContent.isEmpty()) {
            showValidationError("Post content cannot be empty")
            return
        }

        val contentPart = postContent.toRequestBody("text/plain".toMediaTypeOrNull())

        val imageFiles = listOf<File>()
        val images = prepareImages(imageFiles)

        val postService = ClientUtils.getPostService(token)
        val call = postService.create(contentPart, images)

        call.enqueue(object : Callback<Post> {
            override fun onResponse(call: Call<Post>, response: Response<Post>) {
                if (response.isSuccessful) {
                    showToast("Post created successfully")
                    fetchPostsFromServer(token)
                    dismissPostPopup()
                } else {
                    showValidationError("Failed to create post")
                }
            }

            override fun onFailure(call: Call<Post>, t: Throwable) {
                showValidationError("Error: ${t.message}")
            }
        })
    }
    private fun prepareImages(files: List<File>): List<MultipartBody.Part> {
        return files.map { file ->
            val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("images", file.name, requestFile)
        }
    }

    private fun showValidationError(message: String) {
        createPostTextView.background = ContextCompat.getDrawable(this, R.drawable.border_red_square)
        errorMessageTextView.text = message
        errorMessageTextView.visibility = View.VISIBLE
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    private fun performLogout() {
        val userService = ClientUtils.userService
        val call = userService.logout()

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    PreferencesManager.clearToken(this@PostsActivity)
                    val intent = Intent(this@PostsActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    showToast("Logout failed: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                showToast("Logout error: ${t.message}")
            }
        })
    }

}
