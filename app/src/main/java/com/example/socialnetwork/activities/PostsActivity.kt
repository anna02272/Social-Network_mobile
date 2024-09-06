package com.example.socialnetwork.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.socialnetwork.R
import com.example.socialnetwork.adpters.PostAdapter
import com.example.socialnetwork.clients.ClientUtils
import com.example.socialnetwork.model.entity.EReportReason
import com.example.socialnetwork.model.entity.Post
import com.example.socialnetwork.model.entity.Report
import com.example.socialnetwork.model.entity.User
import com.example.socialnetwork.utils.PreferencesManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.time.LocalDate


class PostsActivity : AppCompatActivity(),
    PostAdapter.CommentButtonClickListener,
    PostAdapter.ReportButtonClickListener,
    PostAdapter.DeleteButtonClickListener,
    PostAdapter.EditButtonClickListener {

    private lateinit var createPostTextView: EditText
    private lateinit var dimBackgroundView: View
    private lateinit var createPostPopup: RelativeLayout
    private lateinit var errorMessageTextView: TextView
    private lateinit var fileNameTextView: TextView
    private lateinit var pickImagesLauncher: ActivityResultLauncher<Intent>
    private lateinit var selectedImages: MutableList<Uri>
    private lateinit var storageReference: StorageReference
    private var currentUser: User? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        val token = PreferencesManager.getToken(this)
        if (token == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_posts)

        initializeFirebaseStorage()

        setupBottomNavigation()

        showPostPopup()

        fetchPostsFromServer(token)

        fetchUserData(token)

        initializeActivityResultLauncher()

        findViewById<Button>(R.id.sort).setOnClickListener {

        }

        findViewById<Button>(R.id.chooseFileButton).setOnClickListener {
            chooseImages()
        }

        fileNameTextView = findViewById(R.id.fileNameTextView)

    }

    private fun initializeFirebaseStorage() {
        FirebaseApp.initializeApp(this)
        val firebaseStorage = FirebaseStorage.getInstance()
        storageReference = firebaseStorage.reference
    }

    override fun onCommentButtonClick(post: Post) {
        val commentActivity = CommentActivity().apply {
            arguments = Bundle().apply {
                putLong("postId", post.id)
            }
        }
        commentActivity.show(supportFragmentManager, "commentActivity")
    }

    override fun onReportButtonClick(post: Post) {
        showReportPopup(post)
    }

    override fun onDeleteButtonClick(post: Post) {
       deletePost(post)
    }

    override fun onEditButtonClick(post: Post) {
//        editPost(post)
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        bottomNavigationView.selectedItemId = R.id.bottom_home

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_home -> true
                R.id.bottom_search -> {
                    startActivity(Intent(applicationContext, SearchActivity::class.java))
                    overrideActivityTransition(
                        OVERRIDE_TRANSITION_OPEN,
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                    )
                    finish()
                    true
                }

                R.id.bottom_groups -> {
                    startActivity(Intent(applicationContext, GroupsActivity::class.java))
                    overrideActivityTransition(
                        OVERRIDE_TRANSITION_OPEN,
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                    )
                    finish()
                    true
                }

                R.id.bottom_notifications -> {
                    startActivity(Intent(applicationContext, NotificationsActivity::class.java))
                    overrideActivityTransition(
                        OVERRIDE_TRANSITION_OPEN,
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                    )
                    finish()
                    true
                }

                R.id.bottom_profile -> {
                    startActivity(Intent(applicationContext, ProfileActivity::class.java))
                    overrideActivityTransition(
                        OVERRIDE_TRANSITION_OPEN,
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
        selectedImages.clear()
        fileNameTextView.text = getString(R.string.no_file_chosen)

        createPostTextView.background = ContextCompat.getDrawable(this, R.drawable.edit_text_border)
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(createPostPopup.windowToken, 0)
    }

    private fun showReportPopup(post: Post) {
        val reportPopup = findViewById<RelativeLayout>(R.id.createReportPopup)
        val dimBackgroundView = findViewById<View>(R.id.dimBackgroundView)
        val closeButton = findViewById<ImageView>(R.id.closeReportPopupButton)
        val createButton = findViewById<Button>(R.id.popupCreateReportButton)

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

        fillReportSpinner()

        createButton.setOnClickListener {
            submitReport(post)
        }
    }

    private fun fillReportSpinner() {
        val reportReasons = EReportReason.values().map { it.name.replace('_', ' ') }

        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, reportReasons)

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val reasonSpinner: Spinner = findViewById(R.id.popupReasonSpinner)
        reasonSpinner.adapter = spinnerAdapter

        reasonSpinner.setSelection(0)
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

    private fun fetchUserData(token: String) {
        val userService = ClientUtils.getUserService(token)
        val call = userService.whoAmI()

        call.enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        currentUser = user
                    }
                } else {
                    showToast("Failed to load user data")
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
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

        val imageFiles = getFilesFromUris(selectedImages)
        val images = prepareImages(imageFiles)

        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        progressBar.visibility = View.VISIBLE

        val postService = ClientUtils.getPostService(token)
        val call = postService.create(contentPart, images)

        call.enqueue(object : Callback<Post> {
            override fun onResponse(call: Call<Post>, response: Response<Post>) {
                if (response.isSuccessful) {
                    val post = response.body()
                    post?.let {
                        if (imageFiles.isNotEmpty()) {
                            uploadImagesToFirebase(it.id, imageFiles, progressBar)
                        } else {
                            progressBar.visibility = View.GONE
                            showToast("Post created successfully")
                            fetchPostsFromServer(token)
                            dismissPostPopup()
                        }
                    }
                } else {
                    progressBar.visibility = View.GONE
                    showValidationError("Failed to create post")
                }
            }

            override fun onFailure(call: Call<Post>, t: Throwable) {
                progressBar.visibility = View.GONE
                showValidationError("Error: ${t.message}")
            }
        })
    }

    private fun uploadImagesToFirebase(postId: Long, files: List<File>, progressBar: ProgressBar) {
        val totalFiles = files.size
        var uploadedFiles = 0

        files.forEach { file ->
            val fileUri = Uri.fromFile(file)
            val imageRef = storageReference.child("post_images/$postId/${file.name}")

            val uploadTask = imageRef.putFile(fileUri)

            uploadTask.addOnSuccessListener {
                uploadedFiles++
                if (uploadedFiles == totalFiles) {
                    progressBar.visibility = View.GONE
                    showToast("Post created successfully")
                    fetchPostsFromServer(PreferencesManager.getToken(this)!!)
                    dismissPostPopup()
                }
            }.addOnFailureListener {
                progressBar.visibility = View.GONE
                showToast("Upload failed: ${it.message}")
            }
        }
    }

    private fun prepareImages(files: List<File>): List<MultipartBody.Part> {
        return files.takeIf { it.isNotEmpty() }?.map { file ->
            val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("images", file.name, requestFile)
        } ?: emptyList()
    }


    private fun chooseImages() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        pickImagesLauncher.launch(Intent.createChooser(intent, "Select Pictures"))
    }

    private fun initializeActivityResultLauncher() {
        pickImagesLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                selectedImages = mutableListOf()

                if (data?.clipData != null) {
                    val count = data.clipData!!.itemCount
                    for (i in 0 until count) {
                        val imageUri = data.clipData!!.getItemAt(i).uri
                        selectedImages.add(imageUri)
                    }
                } else if (data?.data != null) {
                    selectedImages.add(data.data!!)
                }

                fileNameTextView.text = if (selectedImages.isEmpty()) {
                    getString(R.string.no_file_chosen)
                } else {
                    resources.getQuantityString(R.plurals.files_chosen_count, selectedImages.size, selectedImages.size)
                }
            } else {
                fileNameTextView.text = getString(R.string.no_file_chosen)
                selectedImages.clear()
            }
        }
    }


    private fun getFilesFromUris(uris: List<Uri>): List<File> {
        val files = mutableListOf<File>()
        uris.forEach { uri ->
            val fileName = uri.lastPathSegment ?: "temp_image"
            val file = File(cacheDir, fileName)
            contentResolver.openInputStream(uri)?.use { inputStream ->
                file.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: run {
                showToast("Failed to open InputStream for $fileName")
            }
            files.add(file)
        }
        return files
    }

    private fun deletePost(post: Post){
        val token = PreferencesManager.getToken(this) ?: return
        val postService = ClientUtils.getPostService(token)

        post.id?.let { postId ->
            postService.delete(postId).enqueue(object : Callback<Post> {
                override fun onResponse(call: Call<Post>, response: Response<Post>) {
                    if (response.isSuccessful) {
                        showToast("Post deleted successfully")
                        fetchPostsFromServer(token)
                    } else {
                        showToast("Failed to delete post: ${response.message()}")
                    }
                }
                override fun onFailure(call: Call<Post>, t: Throwable) {
                    showToast("Error: ${t.message}")
                }
            })
        }
    }
    private fun submitReport(post: Post) {
        val token = PreferencesManager.getToken(this) ?: return
        val reportService = ClientUtils.getReportService(token)
        val reasonSpinner: Spinner = findViewById(R.id.popupReasonSpinner)
        val selectedReason = reasonSpinner.selectedItem.toString()
        val reason = EReportReason.valueOf(selectedReason.replace(' ', '_'))

        val report = currentUser?.let {
            Report(
                id = 0,
                reason = reason,
                timestamp = LocalDate.now(),
                accepted = false,
                isDeleted = false,
                user = it,
                post = post,
                comment = null,
                reportedUser = null
            )
        }

        post.id?.let { postId ->
            if (report != null) {
                reportService.reportPost(postId, report).enqueue(object : Callback<Report> {
                    override fun onResponse(call: Call<Report>, response: Response<Report>) {
                        if (response.isSuccessful) {
                            showToast("Report submitted successfully")

                            findViewById<RelativeLayout>(R.id.createReportPopup).visibility = View.GONE
                            findViewById<View>(R.id.dimBackgroundView).visibility = View.GONE
                        } else {
                            showToast("Failed to submit report: ${response.message()}")
                        }
                    }

                    override fun onFailure(call: Call<Report>, t: Throwable) {
                        showToast("Error: ${t.message}")
                    }
                })
            }
        }
    }

    private fun showValidationError(message: String) {
        createPostTextView.background =
            ContextCompat.getDrawable(this, R.drawable.border_red_square)
        errorMessageTextView.text = message
        errorMessageTextView.visibility = View.VISIBLE
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}