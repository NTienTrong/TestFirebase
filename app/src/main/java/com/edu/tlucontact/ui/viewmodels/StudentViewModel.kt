package com.edu.tlucontact.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.edu.tlucontact.data.models.StudentWithDisplayName
import com.edu.tlucontact.data.repositories.StudentRepository

class StudentViewModel : ViewModel() {

    private val repository = StudentRepository()
    private val _students = MutableLiveData<List<StudentWithDisplayName>>()
    val students: LiveData<List<StudentWithDisplayName>> = _students

    init {
        loadStudents()
    }

    private fun loadStudents() {
        repository.getStudentsWithDisplayName().observeForever { students ->
            _students.value = students
        }
    }

    fun filterStudentsByClass(className: String?) {
        Log.d("StudentViewModel", "Lọc theo lớp: $className")
        val allStudents = _students.value ?: emptyList()
        val filteredStudents = if (className.isNullOrEmpty()) {
            allStudents
        } else {
            allStudents.filter { it.student.className.equals(className, ignoreCase = true) }
        }
        _students.value = filteredStudents
        Log.d("StudentViewModel", "Số lượng sinh viên sau lọc: ${filteredStudents.size}")
    }

    fun searchStudents(query: String?) {
        Log.d("StudentViewModel", "Tìm kiếm: $query")
        val allStudents = _students.value ?: emptyList()
        val searchedStudents = if (query.isNullOrEmpty()) {
            allStudents
        } else {
            allStudents.filter { it.student.fullName.contains(query, ignoreCase = true) }
        }
        _students.value = searchedStudents
        Log.d("StudentViewModel", "Số lượng sinh viên sau tìm kiếm: ${searchedStudents.size}")
    }

    fun sortStudentsByName(ascending: Boolean) {
        Log.d("StudentViewModel", "Sắp xếp theo tên (tăng dần: $ascending)")
        val allStudents = _students.value ?: emptyList()
        val sortedStudents = if (ascending) {
            allStudents.sortedBy { it.displayName }
        } else {
            allStudents.sortedByDescending { it.displayName }
        }
        _students.value = sortedStudents
        Log.d("StudentViewModel", "Số lượng sinh viên sau sắp xếp: ${sortedStudents.size}")
    }
}