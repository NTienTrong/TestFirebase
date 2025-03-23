package com.edu.tlucontact

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.edu.tlucontact.databinding.ActivityMainBinding
import com.edu.tlucontact.ui.activities.EditStaffFormActivity
import com.edu.tlucontact.ui.activities.EditStudentFormActivity
import com.edu.tlucontact.ui.activities.LoginActivity
import com.edu.tlucontact.ui.activities.StaffListActivity
import com.edu.tlucontact.ui.activities.StudentsListActivity
import com.edu.tlucontact.ui.activities.UnitsListActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    companion object {
        private const val EDIT_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setupToolbar()
        loadUserData()
        setupDirectoryButtons()
        checkUserRole()
        setupAddButton()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EDIT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Dữ liệu đã được cập nhật, tải lại thông tin người dùng
                loadUserData()
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.logoutImageView.setOnClickListener { showLogoutConfirmationDialog() }
    }

    private fun setupDirectoryButtons() {
        binding.unitsDirectoryButton.setOnClickListener {
            val intent = Intent(this@MainActivity, UnitsListActivity::class.java)
            startActivity(intent)
        }

        binding.facultyStaffDirectoryButton.setOnClickListener {
            val intent = Intent(this@MainActivity, StaffListActivity::class.java)
            startActivity(intent)
        }

        binding.studentDirectoryButton.setOnClickListener {
            val intent = Intent(this@MainActivity, StudentsListActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setMessage("Bạn có chắc chắn muốn đăng xuất?")
            .setPositiveButton("Có") { _, _ -> logout() }
            .setNegativeButton("Không") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun logout() {
        auth.signOut()

        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun checkUserRole() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val email = user.email ?: ""
            if (email.endsWith("@e.tlu.edu.vn")) {
                binding.facultyStaffDirectoryButton.visibility = android.view.View.GONE
            } else if (email.endsWith("@tlu.edu.vn")) {
                binding.facultyStaffDirectoryButton.visibility = android.view.View.VISIBLE
            }
        }
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            firestore.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val userName = document.getString("displayName")
                        val userEmail = document.getString("email")
                        val userPhone = document.getString("phoneNumber")
                        val userRole = document.getString("role")
                        val photoUrl = document.getString("photoURL")

                        binding.userNameTextView.text = userName
                        binding.userEmailTextView.text = userEmail
                        binding.userPhoneTextView.text = userPhone
                        binding.userTypeTextView.text = userRole

                        Glide.with(this)
                            .load(photoUrl)
                            .placeholder(R.drawable.ic_default_avatar)
                            .error(R.drawable.ic_default_avatar)
                            .into(binding.userAvatarImageView)

                        if (userRole == "Sinh viên") {
                            firestore.collection("students").whereEqualTo("userId", user.uid).get()
                                .addOnSuccessListener { querySnapshot ->
                                    if (!querySnapshot.isEmpty) {
                                        val studentId = querySnapshot.documents[0].getString("studentId")
                                        binding.userIdTextView.text = studentId
                                    } else {
                                        binding.userIdTextView.text = user.uid
                                    }
                                }
                                .addOnFailureListener {
                                    binding.userIdTextView.text = user.uid
                                }
                        } else if (userRole == "CBGV") {
                            firestore.collection("staff").whereEqualTo("userId", user.uid).get()
                                .addOnSuccessListener { querySnapshot ->
                                    if (!querySnapshot.isEmpty) {
                                        val staffId = querySnapshot.documents[0].getString("staffId")
                                        binding.userIdTextView.text = staffId
                                    } else {
                                        binding.userIdTextView.text = user.uid
                                    }
                                }
                                .addOnFailureListener {
                                    binding.userIdTextView.text = user.uid
                                }
                        } else {
                            binding.userIdTextView.text = user.uid
                        }
                    }
                }
                .addOnFailureListener {
                    // Xử lý lỗi
                }
        }
    }

    private fun setupAddButton() {
        binding.addButton.setOnClickListener {
            val currentUser = auth.currentUser
            currentUser?.let { user ->
                firestore.collection("users").document(user.uid).get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val userRole = document.getString("role")
                            if (userRole == "Sinh viên") {
                                showEditStudentForm()
                            } else if (userRole == "CBGV") {
                                showEditStaffForm()
                            }
                        }
                    }
                    .addOnFailureListener {
                        // Xử lý lỗi
                    }
            }
        }
    }

    private fun showEditStudentForm() {
        val intent = Intent(this@MainActivity, EditStudentFormActivity::class.java)
        intent.putExtra("userId", auth.currentUser?.uid)
        startActivityForResult(intent, EDIT_REQUEST_CODE)
    }

    private fun showEditStaffForm() {
        val intent = Intent(this@MainActivity, EditStaffFormActivity::class.java)
        intent.putExtra("userId", auth.currentUser?.uid)
        startActivityForResult(intent, EDIT_REQUEST_CODE)
    }
}