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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.socialnetwork.R
import com.example.socialnetwork.adapters.ImageEditPostAdapter
import com.example.socialnetwork.adapters.PostAdapter
import com.example.socialnetwork.clients.ClientUtils
import com.example.socialnetwork.model.entity.EReactionType
import com.example.socialnetwork.model.entity.EReportReason
import com.example.socialnetwork.model.entity.Group
import com.example.socialnetwork.model.entity.Image
import com.example.socialnetwork.model.entity.Post
import com.example.socialnetwork.model.entity.Reaction
import com.example.socialnetwork.model.entity.Report
import com.example.socialnetwork.model.entity.User
import com.example.socialnetwork.services.BannedService
import com.example.socialnetwork.services.FriendRequestService
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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


open class PostsActivity : AppCompatActivity(),
    PostAdapter.CommentButtonClickListener,
    PostAdapter.ReportButtonClickListener,
    PostAdapter.DeleteButtonClickListener,
    PostAdapter.EditButtonClickListener,
    PostAdapter.LikeButtonClickListener,
    PostAdapter.DislikeButtonClickListener,
    PostAdapter.HeartButtonClickListener{

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
    private lateinit var postService: PostService
    private lateinit var reactionService: ReactionService
    private lateinit var userService: UserService
    private lateinit var reportService: ReportService
    private lateinit var friendRequestService: FriendRequestService
    private lateinit var groupRequestService: GroupRequestService
    private lateinit var bannedService: BannedService
    private lateinit var adapter: PostAdapter
    private var currentToast: Toast? = null
    private lateinit var sortByDateButton: Button
    private var sortingOrder = "descending"

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        token = PreferencesManager.getToken(this)
        if (token == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_posts)

        initializeServices()

        initializeFirebaseStorage()

        setupBottomNavigation()

        fetchPostsFromServer()

        fetchUserData()

        initializeActivityResultLauncher()

        initializeViews()
    }

    private fun initializeViews() {
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

        sortByDateButton.setOnClickListener {
            toggleSortingOrder()
        }

        findViewById<Button>(R.id.createPostButton).setOnClickListener {
            showPostPopup()
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

    private fun initializeServices() {
        val token = PreferencesManager.getToken(this) ?: return
        postService = ClientUtils.getPostService(token)
        reactionService = ClientUtils.getReactionService(token)
        userService = ClientUtils.getUserService(token)
        reportService = ClientUtils.getReportService(token)
        friendRequestService = ClientUtils.getFriendRequestService(token)
        groupRequestService = ClientUtils.getGroupRequestService(token)
        bannedService = ClientUtils.getBannedService(token)
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
        showPostPopup(post)
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

    private fun showPostPopup(post: Post? = null) {
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
            if (post == null) {
                handleCreatePost()
            } else {
                handleEditPost(post.id)
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
    private fun toggleSortingOrder() {
        sortingOrder = if (sortingOrder == "descending") "ascending" else "descending"
        updateButtonText()
        fetchPostsFromServer()
    }

    private fun updateButtonText() {
        val newText = if (sortingOrder == "ascending") getString(R.string.ascending) else getString(R.string.descending)
        sortByDateButton.text = newText
    }

    private fun fetchPostsFromServer() {
        val call = if (sortingOrder == "ascending") postService.getAllAscending() else postService.getAllDescending()

        call.enqueue(object : Callback<List<Post>> {
            override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
                if (response.isSuccessful) {
                    val posts = response.body() ?: arrayListOf()
                    fetchFriendsAndGroups(posts)
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
    private fun fetchFriendsAndGroups(posts: List<Post>) {
        val friendsCall = friendRequestService.getApprovedFriendsForUser(currentUser?.id ?: return)
        val groupsCall = groupRequestService.getApprovedGroupsForUser(currentUser?.id ?: return)

        friendsCall.enqueue(object : Callback<Set<User>> {
            override fun onResponse(call: Call<Set<User>>, response: Response<Set<User>>) {
                if (response.isSuccessful) {
                    val friends = response.body() ?: emptyList()
                    groupsCall.enqueue(object : Callback<Set<Group>> {
                        override fun onResponse(call: Call<Set<Group>>, response: Response<Set<Group>>) {
                            if (response.isSuccessful) {
                                val groups = response.body() ?: emptyList()
                                CoroutineScope(Dispatchers.Main).launch {
                                    filterPosts(posts, friends, groups)
                                }
                            }
                        }

                        override fun onFailure(call: Call<Set<Group>>, t: Throwable) {
                            showToast("Error: ${t.message}")
                        }
                    })
                }
            }

            override fun onFailure(call: Call<Set<User>>, t: Throwable) {
                showToast("Error: ${t.message}")
            }
        })
    }
    private suspend fun filterPosts(posts: List<Post>, friends: Collection<User>, groups: Collection<Group>) {
        val filteredPosts = posts.filter { post ->
            val isUserPost = post.user?.username == currentUser?.username
            val isFriendPost = friends.any { it.username == post.user?.username }
            val isGroupPost = post.group?.let { group ->
             groups.any { it.id == group.id } &&
                        !checkIfUserIsBlocked(group.id!!)
            } ?: false

            isUserPost || isFriendPost || isGroupPost
        }
        updateListView(filteredPosts)
    }
    private suspend fun checkIfUserIsBlocked(groupId: Long): Boolean {
        return withContext(Dispatchers.IO) {
            val call = bannedService.getAllBlockedGroupUsers(groupId)
            val response = call.execute()
            if (response.isSuccessful) {
                val blockedUsers = response.body()
                blockedUsers?.any { it.bannedUser?.id == currentUser?.id } ?: false
            } else {
                true
            }
        }
    }

    private fun updateListView(posts: List<Post>) {
        val listView: ListView = findViewById(R.id.postsListView)
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

    private fun handleCreatePost() {
        val postContent = popupPostEditText.text.toString().trim()

        if (postContent.isEmpty()) {
            showValidationError("Post content cannot be empty")
            return
        }

        val contentPart = postContent.toRequestBody("text/plain".toMediaTypeOrNull())

        val imageFiles = getFilesFromUris(selectedImages)
        val images = prepareImages(imageFiles)

        progressBar.visibility = View.VISIBLE

        val call = postService.create(contentPart, images)

        call.enqueue(object : Callback<Post> {
            override fun onResponse(call: Call<Post>, response: Response<Post>) {
                if (response.isSuccessful) {
                    val post = response.body()
                    post?.let {
                        if (imageFiles.isNotEmpty() && post.images.isNotEmpty()) {
                            uploadImagesToFirebase(it.id, post.images, imageFiles, progressBar)
                        } else {
                            progressBar.visibility = View.GONE
                            showToast("Post created successfully")
                            token?.let { it1 -> fetchPostsFromServer() }
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

    private fun handleEditPost(postId: Long) {
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
                            uploadImagesToFirebase(it.id, post.images, imageFiles, progressBar)
                        } else {
                            progressBar.visibility = View.GONE
                            showToast("Post updated successfully")
                            token?.let { fetchPostsFromServer() }
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

    private fun uploadImagesToFirebase(postId: Long, images: List<Image>, files: List<File>, progressBar: ProgressBar) {
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
                    fetchPostsFromServer()
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

    private fun deletePost(post: Post){
        post.id?.let { postId ->
            postService.delete(postId).enqueue(object : Callback<Post> {
                override fun onResponse(call: Call<Post>, response: Response<Post>) {
                    if (response.isSuccessful) {
                        showToast("Post deleted successfully")
                        token?.let { fetchPostsFromServer() }
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
                        PostAdapter.updateReactionCounts(this@PostsActivity, post, likeCountTextView, dislikeCountTextView, heartCountTextView)
                        currentUser?.let {
                            PostAdapter.checkUserReaction(this@PostsActivity, it, post, likeButton, dislikeButton, heartButton)
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

    private fun showToast(message: String) {
        currentToast?.cancel()
        currentToast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        currentToast?.show()
    }
}