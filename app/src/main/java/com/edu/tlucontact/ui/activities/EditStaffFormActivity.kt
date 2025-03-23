package com.edu.tlucontact.ui.activities

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.edu.tlucontact.R
import com.edu.tlucontact.data.models.Staff
import com.edu.tlucontact.databinding.EditStaffFormBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import androidx.appcompat.app.AlertDialog

class EditStaffFormActivity : AppCompatActivity() {

    private lateinit var binding: EditStaffFormBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var selectedImageUri: Uri? = null
    private var userId: String? = null
    private var staff: Staff? = null

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = EditStaffFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        userId = intent.getStringExtra("userId")

        setupToolbar()
        loadStaffData()
        setupChangeAvatarButton()
        setupSaveButton()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.editStaffToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadStaffData() {
        userId?.let { uid ->
            firestore.collection("users").document(uid).get()
                .addOnSuccessListener { userDocument ->
                    if (userDocument.exists()) {
                        val userName = userDocument.getString("displayName")
                        val photoUrl = userDocument.getString("photoURL")
                        val email = userDocument.getString("email")

                        binding.staffName.setText(userName)
                        binding.staffName.isEnabled = false
                        binding.staffEmail.setText(email)
                        binding.staffEmail.isEnabled = false

                        Glide.with(this)
                            .load(photoUrl)
                            .placeholder(R.drawable.ic_default_avatar)
                            .error(R.drawable.ic_default_avatar)
                            .into(binding.staffAvatar)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("EditStaffFormActivity", "Error loading user data: ${e.message}")
                }

            firestore.collection("staff").whereEqualTo("userId", uid).get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        staff = Staff.fromMap(querySnapshot.documents[0].data)

                        staff?.let { staffInfo ->
                            binding.staffId.setText(staffInfo.staffId)
                            binding.staffPhone.setText(staffInfo.phone)
                        }
                    } else {
                        Log.w("EditStaffFormActivity", "No staff data found for userId: $uid")
                    }
                    Log.d("EditStaffFormActivity", "querySnapshot size: ${querySnapshot.size()}") // Thêm log
                }
                .addOnFailureListener { e ->
                    Log.e("EditStaffFormActivity", "Error loading staff data: ${e.message}")
                }
        }
    }

    private fun setupChangeAvatarButton() {
        binding.changeStaffAvatar.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data
            Glide.with(this).load(selectedImageUri).into(binding.staffAvatar)
        }
    }

    private fun setupSaveButton() {
        binding.saveStaffInfo.setOnClickListener {
            saveStaffData()
        }
    }

    private fun saveStaffData() {
        userId?.let { uid ->
            val staffId = binding.staffId.text.toString()
            val phone = binding.staffPhone.text.toString()

            val staffData = hashMapOf(
                "staffId" to staffId,
                "phone" to phone,
                "userId" to uid
            )

            firestore.collection("staff").whereEqualTo("userId", uid).get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.isEmpty) {
                        Log.w("EditStaffFormActivity", "No staff document found for update, adding new document.")
                        firestore.collection("staff").add(staffData)
                            .addOnSuccessListener {
                                Log.d("EditStaffFormActivity", "Staff data added successfully.")
                                updateUserData(uid, phone)
                            }
                            .addOnFailureListener { e ->
                                Log.e("EditStaffFormActivity", "Error adding staff data: ${e.message}")
                            }
                    } else {
                        val staffDocument = querySnapshot.documents[0]
                        Log.d("EditStaffFormActivity", "staffDocument.id: ${staffDocument.id}")
                        firestore.collection("staff").document(staffDocument.id).update(staffData as Map<String, Any>)
                            .addOnSuccessListener {
                                Log.d("EditStaffFormActivity", "Staff data updated successfully.")
                                updateUserData(uid, phone)
                            }
                            .addOnFailureListener { e ->
                                Log.e("EditStaffFormActivity", "Error updating staff data: ${e.message}")
                            }
                    }

                    selectedImageUri?.let { uri ->
                        uploadImage(uri, uid)
                    } ?: run {
                        updateUserData(uid, phone)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("EditStaffFormActivity", "Error getting staff data: ${e.message}")
                }
        }
    }

    private fun updateUserData(userId: String, phone: String) {
        firestore.collection("users").document(userId).update("phoneNumber", phone)
            .addOnSuccessListener {
                Log.d("EditStaffFormActivity", "User phone number updated successfully.")
                showSuccessDialog()
            }
            .addOnFailureListener { e ->
                Log.e("EditStaffFormActivity", "Error updating user phone number: ${e.message}")
                showSuccessDialog()
            }
    }

    private fun uploadImage(uri: Uri, userId: String) {
        val ref = storage.reference.child("images/$userId/${UUID.randomUUID().toString()}")
        ref.putFile(uri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { downloadUri ->
                    firestore.collection("users").document(userId).update("photoURL", downloadUri.toString())
                        .addOnSuccessListener {
                            updateUserData(userId, binding.staffPhone.text.toString())
                        }
                        .addOnFailureListener { e ->
                            Log.e("EditStaffFormActivity", "Error updating photoURL: ${e.message}")
                            updateUserData(userId, binding.staffPhone.text.toString())
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("EditStaffFormActivity", "Error uploading image: ${e.message}")
                updateUserData(userId, binding.staffPhone.text.toString())
            }
    }

    private fun showSuccessDialog() {
        val dialog = SuccessDialogFragment()
        dialog.show(supportFragmentManager, "SuccessDialog")
    }

    class SuccessDialogFragment : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            return AlertDialog.Builder(requireContext())
                .setTitle("Thành công")
                .setMessage("Cập nhật thông tin thành công!")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                    activity?.finish()
                }
                .create()
        }
    }
}