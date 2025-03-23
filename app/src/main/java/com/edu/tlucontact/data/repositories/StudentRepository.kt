package com.edu.tlucontact.data.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.edu.tlucontact.data.models.StudentWithDisplayName
import com.google.firebase.firestore.FirebaseFirestore

class StudentRepository {

    private val firestore = FirebaseFirestore.getInstance()

    fun getStudentsWithDisplayName(): LiveData<List<StudentWithDisplayName>> {
        val studentsLiveData = MutableLiveData<List<StudentWithDisplayName>>()
        firestore.collection("students").get()
            .addOnSuccessListener { studentDocuments ->
                val studentList = studentDocuments.mapNotNull { studentDoc ->
                    val student = com.edu.tlucontact.data.models.Student.fromMap(studentDoc.data)
                    student?.let {
                        StudentWithDisplayName(it, it.fullName) // Lấy fullName từ bảng students
                    }
                }
                studentsLiveData.value = studentList
            }
            .addOnFailureListener { exception ->
                Log.e("StudentRepository", "Error getting students", exception)
                // Xử lý lỗi lấy thông tin sinh viên
                studentsLiveData.value = emptyList()
            }
        return studentsLiveData
    }
}