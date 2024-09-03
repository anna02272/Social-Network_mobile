package com.example.socialnetwork.activities

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.socialnetwork.R
import com.example.socialnetwork.adpters.BulletPointAdapter
import com.example.socialnetwork.clients.ClientUtils
import com.example.socialnetwork.model.entity.Group
import com.example.socialnetwork.model.entity.User
import com.example.socialnetwork.utils.CircleTransform
import com.example.socialnetwork.utils.PreferencesManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.time.format.DateTimeFormatter


class ProfileActivity : AppCompatActivity() {

    private lateinit var profileImageView: ImageView
    private lateinit var upperFirstNameTextView: TextView
    private lateinit var upperUsernameTextView: TextView
    private lateinit var lastSeenTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var profileNameEditText: EditText
    private lateinit var aboutEditText: EditText
    private lateinit var deleteProfilePhotoButton: ImageView

    private var filePath: Uri? = null
    private val PICK_IMAGE_REQUEST = 22
    private var storage: FirebaseStorage? = null
    private var storageReference: StorageReference? = null
    private var userId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        initializeViews()
        setupBottomNavigation()

        val token = PreferencesManager.getToken(this)
        if (token == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        fetchUserData(token)
    }
    private fun initializeViews() {
        profileImageView = findViewById(R.id.profileImageView)
        upperFirstNameTextView = findViewById(R.id.upperFirstNameTextView)
        upperUsernameTextView = findViewById(R.id.upperUsernameTextView)
        lastSeenTextView = findViewById(R.id.lastSeenTextView)
        dateTextView = findViewById(R.id.dateTextView)
        firstNameEditText = findViewById(R.id.firstNameEditText)
        lastNameEditText = findViewById(R.id.lastNameEditText)
        usernameEditText = findViewById(R.id.usernameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        profileNameEditText = findViewById(R.id.profileNameEditText)
        aboutEditText = findViewById(R.id.aboutEditText)
        deleteProfilePhotoButton = findViewById(R.id.deleteProfilePhotoButton)
        val chooseFileButton: Button = findViewById(R.id.chooseFileButton)
        val changePhotoButton: Button = findViewById(R.id.changePhotoButton)
        storage = FirebaseStorage.getInstance()
        storageReference = storage!!.reference

        chooseFileButton.setOnClickListener {
            selectImage()
        }

        changePhotoButton.setOnClickListener {
            userId?.let { id ->
                if (filePath != null) {
                    uploadImage(id)
                } else {
                    showToast("Please select an image first")
                }
            }
        }

        deleteProfilePhotoButton.setOnClickListener {
            userId?.let {
                deleteImage(it)
            }
        }

        findViewById<Button>(R.id.logout).setOnClickListener {
            performLogout()
        }
    }
    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        bottomNavigationView.selectedItemId = R.id.bottom_profile

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_home -> {
                    startActivity(Intent(applicationContext, PostsActivity::class.java))
                    overrideActivityTransition(OVERRIDE_TRANSITION_OPEN,
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                    )
                    finish()
                    true
                }
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
                R.id.bottom_profile -> true
                else -> false
            }
        }
    }
    private fun fetchUserData(token: String) {
        val userService = ClientUtils.getUserService(token)
        val call = userService.whoAmI()

        call.enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        populateUserData(user)
                        userId = user.id
                        userId?.let {
                            fetchUserGroups(it, token);
                            fetchFriends(it, token)
                            loadProfileImage(it)
                        }
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
    private fun loadProfileImage(userId: Long) {
        val ref = storageReference!!.child("images/$userId")
        ref.downloadUrl.addOnSuccessListener { uri ->
            Picasso.get().load(uri).transform(CircleTransform()).into(profileImageView)
        }.addOnFailureListener {
            profileImageView.setImageResource(R.drawable.smiley_circle)
        }
    }
    private fun populateUserData(user: User) {
        upperFirstNameTextView.text = user.firstName + " " + user.lastName
        upperUsernameTextView.text = user.profileName?.takeIf { it.isNotEmpty() } ?: user.username
        val formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")
        dateTextView.text =  user.lastLogin?.format(formatter) ?: ""
        firstNameEditText.setText(user.firstName)
        lastNameEditText.setText(user.lastName)
        usernameEditText.setText(user.username)
        emailEditText.setText(user.email)
        profileNameEditText.setText(user.profileName)
        aboutEditText.setText(user.description)

    }
    private fun fetchUserGroups(userId: Long, token: String) {
        val groupRequestService = ClientUtils.getGroupRequestService(token)
        val groupCall = groupRequestService.getApprovedGroupsForUser(userId)

        groupCall.enqueue(object : Callback<Set<Group>> {
            override fun onResponse(call: Call<Set<Group>>, response: Response<Set<Group>>) {
                if (response.isSuccessful) {
                    val groups = response.body() ?: emptySet()

                    val groupNames = groups.mapNotNull { group ->
                        group.name?.takeIf { it.isNotEmpty() }
                    }

                    val recyclerView: RecyclerView = findViewById(R.id.groupsProfileRecyclerView)
                    recyclerView.layoutManager = LinearLayoutManager(this@ProfileActivity)
                    recyclerView.adapter = BulletPointAdapter(this@ProfileActivity, groupNames)
                } else {
                    showToast("Failed to load groups")
                }
            }

            override fun onFailure(call: Call<Set<Group>>, t: Throwable) {
                showToast("Error: ${t.message}")
            }
        })
    }
    private fun fetchFriends(userId: Long, token: String) {
        val friendRequestService = ClientUtils.getFriendRequestService(token)
        val friendCall = friendRequestService.getApprovedFriendsForUser(userId)

        friendCall.enqueue(object : Callback<Set<User>> {
            override fun onResponse(call: Call<Set<User>>, response: Response<Set<User>>) {
                if (response.isSuccessful) {
                    val friends = response.body() ?: emptySet()

                    val friendUsernames = friends.mapNotNull { user ->
                        user.username?.takeIf { it.isNotEmpty() }
                    }

                    val recyclerView: RecyclerView = findViewById(R.id.friendsRecyclerView)
                    recyclerView.layoutManager = LinearLayoutManager(this@ProfileActivity)
                    recyclerView.adapter = BulletPointAdapter(this@ProfileActivity, friendUsernames)
                } else {
                    showToast("Failed to load friends")
                }
            }

            override fun onFailure(call: Call<Set<User>>, t: Throwable) {
                showToast("Error: ${t.message}")
            }
        })
    }
    private fun selectImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent, "Select Image from here..."),
            PICK_IMAGE_REQUEST
        )
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            filePath = data.data
            try {
                Picasso.get()
                    .load(filePath)
                    .transform(CircleTransform())
                    .into(profileImageView)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
    private fun uploadImage(userId: Long) {
        if (filePath != null) {
            val progressDialog = ProgressDialog(this)
            progressDialog.setTitle("Uploading...")
            progressDialog.show()

            val ref = storageReference!!.child("images/$userId")
            ref.putFile(filePath!!)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener {
                        updateProfilePictureOnBackend(userId)
                        progressDialog.dismiss()
                        showToast("Image Uploaded!")
                    }
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    showToast("Failed: ${e.message}")
                }
                .addOnProgressListener { taskSnapshot ->
                    val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
                    progressDialog.setMessage("Uploaded ${progress.toInt()}%")
                }
        }
    }
    private fun updateProfilePictureOnBackend(userId: Long) {
        if (filePath == null) {
            showToast("No image selected")
            return
        }

        val token = PreferencesManager.getToken(this) ?: return
        val userService = ClientUtils.getUserService(token)

        val inputStream = contentResolver.openInputStream(filePath!!)
        val requestFile = inputStream?.let {
            it.readBytes().toRequestBody("image/jpeg".toMediaTypeOrNull())
        }
        val body = requestFile?.let {
            MultipartBody.Part.createFormData("profilePicture", "profile_picture.jpg", it)
        }

        if (body == null) {
            showToast("Failed to create request body")
            return
        }

        val call = userService.updateProfilePicture(userId, body)

        call.enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                } else {
                    showToast("Error: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                showToast("Error: ${t.message}")
            }
        })
    }

    private fun deleteImage(userId: Long) {
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Deleting...")
        progressDialog.show()

        val ref = storageReference!!.child("images/$userId")
        ref.delete()
            .addOnSuccessListener {
                deleteProfilePictureFromBackend(userId)
                progressDialog.dismiss()
                showToast("Profile photo deleted!")
                profileImageView.setImageResource(R.drawable.smiley_circle)
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                showToast("Failed to delete photo: " + e.message)
            }
    }

    private fun deleteProfilePictureFromBackend(userId: Long) {
        val userService = ClientUtils.getUserService(PreferencesManager.getToken(this))
        val call = userService.deleteProfilePicture(userId)

        call.enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    showToast("Profile picture deleted from backend!")
                } else {
                    showToast("Failed to delete profile picture from backend")
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                showToast("Error: ${t.message}")
            }
        })
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
                    PreferencesManager.clearToken(this@ProfileActivity)
                    val intent = Intent(this@ProfileActivity, LoginActivity::class.java)
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
