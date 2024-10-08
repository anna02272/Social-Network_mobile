package com.example.socialnetwork.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.socialnetwork.R
import com.example.socialnetwork.adapters.ImageEditPostAdapter
import com.example.socialnetwork.adapters.PostAdapter
import com.example.socialnetwork.clients.ClientUtils
import com.example.socialnetwork.fragments.GroupFragment
import com.example.socialnetwork.model.entity.Banned
import com.example.socialnetwork.model.entity.EReactionType
import com.example.socialnetwork.model.entity.EReportReason
import com.example.socialnetwork.model.entity.Group
import com.example.socialnetwork.model.entity.GroupRequest
import com.example.socialnetwork.model.entity.Image
import com.example.socialnetwork.model.entity.Post
import com.example.socialnetwork.model.entity.Reaction
import com.example.socialnetwork.model.entity.Report
import com.example.socialnetwork.model.entity.User
import com.example.socialnetwork.services.BannedService
import com.example.socialnetwork.services.GroupRequestService
import com.example.socialnetwork.services.PostService
import com.example.socialnetwork.services.ReactionService
import com.example.socialnetwork.services.ReportService
import com.example.socialnetwork.services.UserService
import com.example.socialnetwork.utils.CircleTransform
import com.example.socialnetwork.utils.PreferencesManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate

class GroupActivity : AppCompatActivity(),
    PostAdapter.CommentButtonClickListener,
    PostAdapter.ReportButtonClickListener,
    PostAdapter.DeleteButtonClickListener,
    PostAdapter.EditButtonClickListener,
    PostAdapter.LikeButtonClickListener,
    PostAdapter.DislikeButtonClickListener,
    PostAdapter.HeartButtonClickListener {

    private lateinit var group: Group
    private lateinit var postService: PostService
    private var currentToast: Toast? = null
    private lateinit var adapter: PostAdapter
    private lateinit var createPostTextView: TextView
    private lateinit var popupPostEditText: EditText
    private lateinit var errorMessageTextView: TextView
    private lateinit var fileNameTextView: TextView
    private lateinit var dimBackgroundView: View
    private lateinit var createPostPopup: RelativeLayout
    private lateinit var closePopupButton: ImageView
    private lateinit var popupCreatePostButton: Button
    private lateinit var pickImagesLauncher: ActivityResultLauncher<Intent>
    private lateinit var selectedImages: MutableList<Uri>
    private lateinit var progressBar: ProgressBar
    private lateinit var storageReference: StorageReference
    private lateinit var recyclerView : RecyclerView
    private var currentUser: User? = null
    private var token: String? = null
    private lateinit var reactionService: ReactionService
    private lateinit var userService: UserService
    private lateinit var reportService: ReportService
    private lateinit var groupRequestService: GroupRequestService
    private lateinit var bannedService: BannedService
    private lateinit var sortByDateButton: Button
    private var sortingOrder = "descending"
    private lateinit var createPostBox: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group)

        @Suppress("DEPRECATION")
        group = intent.getParcelableExtra("group")!!
        val groupFragment = GroupFragment.newInstance(group)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, groupFragment)
            .commit()

        initializeServices()
        initializeFirebaseStorage()
        fetchUserData()
        initializeActivityResultLauncher()
        group.id?.let { initializeViews(it) }
        setupBottomNavigation()
    }

    private fun initializeServices() {
        token = PreferencesManager.getToken(this) ?: return
        postService = ClientUtils.getPostService(token)
        reactionService = ClientUtils.getReactionService(token)
        userService = ClientUtils.getUserService(token)
        reportService = ClientUtils.getReportService(token)
        groupRequestService = ClientUtils.getGroupRequestService(token)
        bannedService = ClientUtils.getBannedService(token)
    }

    private fun initializeViews(groupId: Long) {
        fileNameTextView = findViewById(R.id.fileNameTextView)
        createPostTextView = findViewById(R.id.createPostTextView)
        popupPostEditText = findViewById(R.id.popupPostEditText)
        errorMessageTextView = findViewById(R.id.post_error_message)
        dimBackgroundView = findViewById(R.id.dimBackgroundView)
        createPostPopup = findViewById(R.id.createPostPopup)
        closePopupButton = findViewById(R.id.closePostPopupButton)
        popupCreatePostButton = findViewById(R.id.popupCreatePostButton)
        selectedImages = mutableListOf()
        progressBar = findViewById(R.id.progressBar)
        sortByDateButton = findViewById(R.id.sortByDateButton)
        createPostBox = findViewById(R.id.createPostBox)

        createPostBox.visibility = View.GONE

        sortByDateButton.setOnClickListener {
            toggleSortingOrder(groupId)
        }

        findViewById<Button>(R.id.createPostButton).setOnClickListener {
            showPostPopup(groupId)
        }

        findViewById<Button>(R.id.chooseFileButton).setOnClickListener {
            chooseImages()
        }

        recyclerView = findViewById(R.id.imagesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun initializeFirebaseStorage() {
        FirebaseApp.initializeApp(this)
        val firebaseStorage = FirebaseStorage.getInstance()
        storageReference = firebaseStorage.reference
    }
    private fun showToast(message: String) {
        currentToast?.cancel()
        currentToast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        currentToast?.show()
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
        post.group?.id?.let { deletePost(post, it) }
    }

    override fun onEditButtonClick(post: Post) {
        showPostPopup(post.group?.id, post)
    }

    override fun onLikeButtonClick(post: Post, view: View) {
        reactToPost(post, EReactionType.LIKE, view)
    }

    override fun onDislikeButtonClick(post: Post, view: View) {
        reactToPost(post, EReactionType.DISLIKE, view)
    }

    override fun onHeartButtonClick(post: Post, view: View) {
        reactToPost(post, EReactionType.HEART, view)
    }
    private fun showPostPopup(groupId: Long? = null, post: Post? = null) {
        recyclerView.visibility = View.GONE
        createPostTextView.text = if (post == null) {
            getString(R.string.create_post)
        } else {
            getString(R.string.edit_post)
        }

        popupCreatePostButton.text = if (post == null) {
            getString(R.string.create_post)
        } else {
            getString(R.string.edit_post)
        }

        if (post != null) {
            popupPostEditText.setText(post.content)
            loadImages(post.id)
        } else {
            popupPostEditText.text.clear()
            selectedImages.clear()
        }

        dimBackgroundView.visibility = View.VISIBLE
        createPostPopup.visibility = View.VISIBLE

        closePopupButton.setOnClickListener {
            dismissPostPopup()
        }

        popupCreatePostButton.setOnClickListener {
            if (groupId != null) {
                if (post == null) {
                    handleCreatePost(groupId)
                } else {
                    handleEditPost(post.id, groupId)
                }
            }
        }

        val popupProfileImage = findViewById<ImageView>(R.id.popupPostProfileImage)
        val usernameTextView = findViewById<TextView>(R.id.usernamePostTextView)

        currentUser?.let { user ->
            usernameTextView.text = user.profileName?.takeIf { it.isNotEmpty() } ?: user.username

            popupProfileImage?.let { imageView ->
                user.id?.let { loadProfileImage(it, imageView) }
            }
        }

    }

    private fun dismissPostPopup() {
        dimBackgroundView.visibility = View.GONE
        createPostPopup.visibility = View.GONE
        popupPostEditText.text.clear()
        errorMessageTextView.visibility = View.GONE
        selectedImages.clear()
        fileNameTextView.text = getString(R.string.no_file_chosen)

        popupPostEditText.background = ContextCompat.getDrawable(this, R.drawable.edit_text_border)
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(createPostPopup.windowToken, 0)
    }

    private fun showReportPopup(post: Post) {
        val reportPopup = findViewById<RelativeLayout>(R.id.createReportPopup)
        val closeButton = findViewById<ImageView>(R.id.closeReportPopupButton)
        val createButton = findViewById<Button>(R.id.popupCreateReportButton)
        val popupProfileImage = findViewById<ImageView>(R.id.popupProfileImage)
        val usernameTextView = findViewById<TextView>(R.id.usernameReportTextView)

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

        currentUser?.let { user ->
            if (usernameTextView != null) {
                usernameTextView.text = user.profileName?.takeIf { it.isNotEmpty() } ?: user.username
            }
            popupProfileImage?.let { imageView ->
                user.id?.let { loadProfileImage(it, imageView) }
            }
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
    private fun toggleSortingOrder(groupId: Long) {
        sortingOrder = if (sortingOrder == "descending") "ascending" else "descending"
        updateButtonText()
        fetchPostsFromServer(groupId)
    }

    private fun updateButtonText() {
        val newText = if (sortingOrder == "ascending") getString(R.string.ascending) else getString(R.string.descending)
        sortByDateButton.text = newText
    }

    fun fetchPostsFromServer(groupId: Long) {
        val call = if (sortingOrder == "ascending") postService.getByGroupAscending(groupId) else postService.getByGroupDescending(groupId)

        call.enqueue(object : Callback<List<Post>> {
            override fun onResponse(
                call: Call<List<Post>>,
                response: Response<List<Post>>
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

            override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                showToast("Error: ${t.message}")
            }
        })
    }
    private fun updateListView(posts: List<Post>) {
        val listView: ListView = findViewById(R.id.groupPostsListView)
        adapter = PostAdapter(this, posts)
        adapter.commentButtonClickListener = this
        adapter.reportButtonClickListener = this
        adapter.deleteButtonClickListener = this
        adapter.editButtonClickListener = this
        adapter.likeButtonClickListener = this
        adapter.dislikeButtonClickListener = this
        adapter.heartButtonClickListener = this
        listView.adapter = adapter
    }

    private fun fetchUserData() {
        val call = userService.whoAmI()

        call.enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        currentUser = user
                        checkGroupRequestStatus()
                        val profileImage = findViewById<ImageView>(R.id.postProfileImage)
                        user.id?.let {
                            if (profileImage != null) {
                                loadProfileImage(it, profileImage)
                            }
                        }
                        }
                    }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                showToast("Error: ${t.message}")
            }
        })
    }
    private fun checkGroupRequestStatus() {
        val call = groupRequestService.getGroupRequestByUserAndGroup(currentUser?.id ?: return, group.id ?: return)

        val isUserGroupAdmin = group.groupAdmin.any { it.user?.username == currentUser?.username }

        call.enqueue(object : Callback<GroupRequest> {
            override fun onResponse(call: Call<GroupRequest>, response: Response<GroupRequest>) {
                if (response.isSuccessful) {
                    val groupRequest = response.body()
                    if (groupRequest != null && groupRequest.approved) {
                        checkIfUserIsBlocked(group.id!!)
                    } else {
                        if (!isUserGroupAdmin) {
                            createPostBox.visibility = View.GONE
                        }
                    }
                } else {
                    if (isUserGroupAdmin) {
                        checkIfUserIsBlocked(group.id!!)
                    }
                }
            }

            override fun onFailure(call: Call<GroupRequest>, t: Throwable) {
                if (isUserGroupAdmin) {
                    checkIfUserIsBlocked(group.id!!)
                } else {
                    createPostBox.visibility = View.GONE
                }
            }
        })
    }

    private fun checkIfUserIsBlocked(groupId: Long) {
        val call = bannedService.getAllBlockedGroupUsers(groupId)

        call.enqueue(object : Callback<List<Banned>> {
            override fun onResponse(call: Call<List<Banned>>, response: Response<List<Banned>>) {
                if (response.isSuccessful) {
                    val blockedUsers = response.body()
                    val isUserBlocked = blockedUsers?.any { it.bannedUser?.id == currentUser?.id } ?: false

                    if (isUserBlocked) {
                        createPostBox.visibility = View.GONE
                    } else {
                        fetchPostsFromServer(groupId)
                        createPostBox.visibility = View.VISIBLE
                    }
                } else {
                    createPostBox.visibility = View.GONE
                }
            }

            override fun onFailure(call: Call<List<Banned>>, t: Throwable) {
                createPostBox.visibility = View.GONE
            }
        })
    }

    private fun loadProfileImage(userId: Long, profileImageView: ImageView) {
        val ref = storageReference.child("profile_images/$userId")
        ref.downloadUrl.addOnSuccessListener { uri ->
            Picasso.get().load(uri).transform(CircleTransform()).into(profileImageView)
        }.addOnFailureListener {
            profileImageView.setImageResource(R.drawable.smiley_circle)
        }
    }

    private fun handleTokenExpired() {
        PreferencesManager.clearToken(this)
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun loadImages(postId: Long) {
        val imageFolderRef = storageReference.child("post_images/$postId/")
        imageFolderRef.listAll()
            .addOnSuccessListener { listResult ->
                val imageUrls = mutableListOf<Pair<String, String>>()
                val imageItems = listResult.items.size

                if (imageItems == 0) {
                    recyclerView.visibility = View.GONE
                } else {
                    listResult.items.forEachIndexed { index, item ->
                        val imageId = item.name
                        item.downloadUrl.addOnSuccessListener { uri ->
                            imageUrls.add(Pair(imageId, uri.toString()))
                            if (index == imageItems - 1) {
                                val adapter = ImageEditPostAdapter(this, imageUrls, postId)
                                recyclerView.adapter = adapter
                                recyclerView.visibility = View.VISIBLE
                            }
                        }.addOnFailureListener {
                            showToast("Failed to load image: ${it.message}")
                        }
                    }
                }
            }
            .addOnFailureListener {
                showToast("Failed to list images: ${it.message}")
            }
    }

    private fun handleCreatePost(groupId: Long) {
        val postContent = popupPostEditText.text.toString().trim()

        if (postContent.isEmpty()) {
            showValidationError("Post content cannot be empty")
            return
        }

        val contentPart = postContent.toRequestBody("text/plain".toMediaTypeOrNull())

        val imageFiles = getFilesFromUris(selectedImages)
        val images = prepareImages(imageFiles)

        progressBar.visibility = View.VISIBLE

        val call = postService.createGroupPost(groupId, contentPart, images)

        call.enqueue(object : Callback<Post> {
            override fun onResponse(call: Call<Post>, response: Response<Post>) {
                if (response.isSuccessful) {
                    val post = response.body()
                    post?.let {
                        if (imageFiles.isNotEmpty() && post.images.isNotEmpty()) {
                            uploadImagesToFirebase(it.id, groupId, post.images, imageFiles, progressBar)
                        } else {
                            progressBar.visibility = View.GONE
                            showToast("Post created successfully")
                            fetchPostsFromServer(groupId)
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

    private fun handleEditPost(postId: Long, groupId: Long) {
        val postContent = popupPostEditText.text.toString().trim()

        if (postContent.isEmpty()) {
            showValidationError("Post content cannot be empty")
            return
        }

        val imageFiles = getFilesFromUris(selectedImages)
        val images = prepareImages(imageFiles)

        val postContentRequestBody = postContent.toRequestBody("text/plain".toMediaTypeOrNull())

        progressBar.visibility = View.VISIBLE

        val call = postService.update(postId, postContentRequestBody, images)

        call.enqueue(object : Callback<Post> {
            override fun onResponse(call: Call<Post>, response: Response<Post>) {
                if (response.isSuccessful) {
                    val post = response.body()
                    post?.let {
                        if (imageFiles.isNotEmpty() && post.images.isNotEmpty()) {
                            uploadImagesToFirebase(it.id, groupId, post.images, imageFiles, progressBar)
                        } else {
                            progressBar.visibility = View.GONE
                            showToast("Post updated successfully")
                            fetchPostsFromServer(groupId)
                            dismissPostPopup()
                        }
                    }
                } else {
                    progressBar.visibility = View.GONE
                    showValidationError("Failed to update post")
                }
            }

            override fun onFailure(call: Call<Post>, t: Throwable) {
                progressBar.visibility = View.GONE
                showValidationError("Error: ${t.message}")
            }
        })
    }

    private fun uploadImagesToFirebase(postId: Long, groupId: Long,  images: List<Image>, files: List<File>, progressBar: ProgressBar) {
        val totalFiles = files.size
        if (images.size != totalFiles) {
            showToast("Mismatch between images and files")
            progressBar.visibility = View.GONE
            return
        }
        var uploadedFiles = 0

        files.forEachIndexed { index, file ->
            val fileUri = Uri.fromFile(file)
            val imageRef = storageReference.child("post_images/$postId/${images[index].id}")

            val uploadTask = imageRef.putFile(fileUri)

            uploadTask.addOnSuccessListener {
                uploadedFiles++
                if (uploadedFiles == totalFiles) {
                    progressBar.visibility = View.GONE
                    showToast("Post created successfully")
                    fetchPostsFromServer(groupId)
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
                val bitmap = BitmapFactory.decodeStream(inputStream)
                val compressedFile = compressBitmapToFile(bitmap, file)
                files.add(compressedFile)
            } ?: run {
                showToast("Failed to open InputStream for $fileName")
            }
        }
        return files
    }
    private fun compressBitmapToFile(bitmap: Bitmap, file: File): File {
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
        outputStream.flush()
        outputStream.close()
        return file
    }

    private fun deletePost(post: Post, groupId: Long){
        post.id.let { postId ->
            postService.delete(postId).enqueue(object : Callback<Post> {
                override fun onResponse(call: Call<Post>, response: Response<Post>) {
                    if (response.isSuccessful) {
                        showToast("Post deleted successfully")
                        fetchPostsFromServer(groupId)
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
                            dimBackgroundView.visibility = View.GONE
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
    private fun reactToPost(post: Post, reactionType: EReactionType, view: View) {
        val likeCountTextView: TextView = view.findViewById(R.id.likeCountTextView)
        val dislikeCountTextView: TextView = view.findViewById(R.id.dislikeCountTextView)
        val heartCountTextView: TextView = view.findViewById(R.id.heartCountTextView)
        val likeButton: ImageButton = view.findViewById(R.id.likeButton)
        val dislikeButton: ImageButton = view.findViewById(R.id.dislikeButton)
        val heartButton: ImageButton = view.findViewById(R.id.heartButton)
        val reaction =
            currentUser?.let { Reaction(0, reactionType, LocalDate.now(), it, post, null) }
        reaction?.let { reactionService.reactToPost(post.id, it) }
            ?.enqueue(object : Callback<Reaction> {
                override fun onResponse(call: Call<Reaction>, response: Response<Reaction>) {
                    if (response.isSuccessful) {
                        PostAdapter.updateReactionCounts(this@GroupActivity, post, likeCountTextView, dislikeCountTextView, heartCountTextView)
                        currentUser?.let {
                            PostAdapter.checkUserReaction(this@GroupActivity, it, post, likeButton, dislikeButton, heartButton)
                        }
                        showToast("Reacted successfully")
                    }
                }

                override fun onFailure(call: Call<Reaction>, t: Throwable) {
                    showToast("Error: ${t.message}")
                }
            })
    }
    private fun showValidationError(message: String) {
        popupPostEditText.background =
            ContextCompat.getDrawable(this, R.drawable.border_red_square)
        errorMessageTextView.text = message
        errorMessageTextView.visibility = View.VISIBLE
    }
    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        bottomNavigationView.selectedItemId = R.id.bottom_groups

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_home -> {
                    startActivity(Intent(applicationContext, PostsActivity::class.java))
                    overrideActivityTransition(
                        OVERRIDE_TRANSITION_OPEN,
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                    )
                    finish()
                    true
                }

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
}