package com.edu.tlucontact.data.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.edu.tlucontact.data.models.Staff
import com.edu.tlucontact.data.models.Unit
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class StaffRepository {

    private val firestore = FirebaseFirestore.getInstance()

    fun getStaffList(): LiveData<List<Staff>> {
        val staffListLiveData = MutableLiveData<List<Staff>>()
        firestore.collection("staff").get()
            .addOnSuccessListener { staffDocuments ->
                val staffList = staffDocuments.mapNotNull { staffDoc ->
                    Staff.fromMap(staffDoc.data)
                }
                staffListLiveData.value = staffList
            }
            .addOnFailureListener { exception ->
                Log.e("StaffRepository", "Error getting staff list", exception)
                staffListLiveData.value = emptyList()
            }
        return staffListLiveData
    }

    fun getUnitDetails(unitRef: com.google.firebase.firestore.DocumentReference): LiveData<Unit?> {
        val unitLiveData = MutableLiveData<Unit?>()
        unitRef.get()
            .addOnSuccessListener { unitDocument ->
                unitLiveData.value = Unit.fromMap(unitDocument.data)
            }
            .addOnFailureListener { exception ->
                Log.e("StaffRepository", "Error getting unit details", exception)
                unitLiveData.value = null
            }
        return unitLiveData
    }

    fun getUnitDetailsSync(unitRef: com.google.firebase.firestore.DocumentReference): Unit? {
        val latch = CountDownLatch(1)
        var unit: Unit? = null

        unitRef.get()
            .addOnSuccessListener { unitDocument ->
                unit = Unit.fromMap(unitDocument.data)
                latch.countDown()
            }
            .addOnFailureListener {
                latch.countDown()
                Log.e("StaffRepository", "Error getting unit details", it) // Thêm log lỗi
            }

        try {
            latch.await(5, TimeUnit.SECONDS) // Thêm timeout
        } catch (e: InterruptedException) {
            Log.e("StaffRepository", "Latch interrupted", e) // Thêm log lỗi
        }

        return unit
    }

    suspend fun getUnitDetailsCoroutine(unitRef: DocumentReference): Unit? = withContext(Dispatchers.IO) {
        return@withContext try {
            val unitDocument = unitRef.get().await()
            Unit.fromMap(unitDocument.data)
        } catch (e: Exception) {
            Log.e("StaffRepository", "Error getting unit details", e)
            null // Hoặc trả về giá trị mặc định
        }
    }
}