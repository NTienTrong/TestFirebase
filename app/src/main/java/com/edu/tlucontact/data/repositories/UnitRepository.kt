package com.edu.tlucontact.data.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.edu.tlucontact.data.models.Unit
import com.google.firebase.firestore.FirebaseFirestore

class UnitRepository {

    private val firestore = FirebaseFirestore.getInstance()

    fun getUnits(): LiveData<List<Unit>> {
        val unitsLiveData = MutableLiveData<List<Unit>>()
        firestore.collection("units")
            .get()
            .addOnSuccessListener { documents ->
                val unitList = documents.mapNotNull { Unit.fromMap(it.data) }
                unitsLiveData.value = unitList
            }
            .addOnFailureListener { exception ->
                // Xử lý lỗi (ví dụ: log, hiển thị thông báo)
            }
        return unitsLiveData
    }
}