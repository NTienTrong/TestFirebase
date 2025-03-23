package com.edu.tlucontact.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edu.tlucontact.data.models.Staff
import com.edu.tlucontact.data.repositories.StaffRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StaffViewModel : ViewModel() {

    private val repository = StaffRepository()
    private val _staffList = MutableLiveData<List<Staff>>()
    val staffList: LiveData<List<Staff>> = _staffList
    private val allStaffList = mutableListOf<Staff>() // Lưu trữ danh sách gốc

    init {
        loadStaffList()
    }

    private fun loadStaffList() {
        repository.getStaffList().observeForever { staffList ->
            _staffList.value = staffList
            allStaffList.clear()
            allStaffList.addAll(staffList)
        }
    }

    fun searchStaff(query: String?) {
        if (query.isNullOrEmpty()) {
            _staffList.value = allStaffList
        } else {
            val filteredList = allStaffList.filter { staff ->
                staff.fullName.contains(query, ignoreCase = true) ||
                        staff.position.contains(query, ignoreCase = true) ||
                        staff.staffId.contains(query, ignoreCase = true)
            }
            _staffList.value = filteredList
        }
    }

    fun filterStaffByUnit(unitName: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            val filteredList = withContext(Dispatchers.IO) {
                if (unitName.isNullOrEmpty() || unitName == "Tất cả") {
                    allStaffList
                } else {
                    allStaffList.filter { staff ->
                        staff.unit?.let { unitRef ->
                            val unit = repository.getUnitDetailsCoroutine(unitRef)
                            unit?.name?.equals(unitName, ignoreCase = true) ?: false
                        } ?: false
                    }
                }
            }
            withContext(Dispatchers.Main) {
                _staffList.value = filteredList
            }
        }
    }

    fun sortStaffByName(ascending: Boolean) {
        val sortedList = if (ascending) {
            allStaffList.sortedBy { it.fullName }
        } else {
            allStaffList.sortedByDescending { it.fullName }
        }
        _staffList.value = sortedList
    }

    fun sortStaffByPosition(ascending: Boolean) {
        val sortedList = if (ascending) {
            allStaffList.sortedBy { it.position }
        } else {
            allStaffList.sortedByDescending { it.position }
        }
        _staffList.value = sortedList
    }

    fun sortStaffByUnit(ascending: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val sortedList = withContext(Dispatchers.IO) {
                if (ascending) {
                    allStaffList.sortedBy { it.unit?.let { unitRef -> repository.getUnitDetailsSync(unitRef)?.name ?: "" } ?: "" }
                } else {
                    allStaffList.sortedByDescending { it.unit?.let { unitRef -> repository.getUnitDetailsSync(unitRef)?.name ?: "" } ?: "" }
                }
            }
            withContext(Dispatchers.Main) {
                _staffList.value = sortedList
            }
        }
    }
}