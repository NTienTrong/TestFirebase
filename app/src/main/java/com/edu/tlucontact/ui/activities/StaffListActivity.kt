package com.edu.tlucontact.ui.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.edu.tlucontact.databinding.StaffListActivityBinding
import com.edu.tlucontact.ui.adapter.StaffAdapter
import com.edu.tlucontact.ui.viewmodels.StaffViewModel

class StaffListActivity : AppCompatActivity() {

    private lateinit var binding: StaffListActivityBinding
    private lateinit var staffAdapter: StaffAdapter
    private lateinit var staffViewModel: StaffViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = StaffListActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        binding.staffRecyclerView.layoutManager = LinearLayoutManager(this)
        staffAdapter = StaffAdapter(mutableListOf()) { staff ->
            val intent = Intent(this, StaffDetailActivity::class.java)
            intent.putExtra("staffId", staff.staffId)
            intent.putExtra("fullName", staff.fullName)
            intent.putExtra("position", staff.position)
            intent.putExtra("phone", staff.phone)
            intent.putExtra("email", staff.email)
            intent.putExtra("photoURL", staff.photoURL)
            intent.putExtra("unitPath", staff.unit?.path)
            intent.putExtra("userId", staff.userId)
            startActivity(intent)
        }
        binding.staffRecyclerView.adapter = staffAdapter

        staffViewModel = ViewModelProvider(this)[StaffViewModel::class.java]

        staffViewModel.staffList.observe(this, Observer { staffList ->
            staffAdapter.submitList(staffList)
        })

        setupSearch()
        setupFilter()
        setupSort()
    }

    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                staffViewModel.searchStaff(s?.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupFilter() {
        val unitList = listOf("Tất cả", "KCNTT", "PVCTSV", "BMCNPM", "BMHTTT") // Thay thế bằng danh sách đơn vị thực tế
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, unitList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.filterSpinner.adapter = adapter

        binding.filterButton.setOnClickListener {
            if (binding.filterSpinner.visibility == View.VISIBLE) {
                binding.filterSpinner.visibility = View.GONE
            } else {
                binding.filterSpinner.visibility = View.VISIBLE
            }
        }

        binding.filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedUnit = unitList[position]
                staffViewModel.filterStaffByUnit(selectedUnit)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupSort() {
        binding.sortButton.setOnClickListener {
            if (binding.sortOptionsLayout.visibility == View.VISIBLE) {
                binding.sortOptionsLayout.visibility = View.GONE
            } else {
                binding.sortOptionsLayout.visibility = View.VISIBLE
            }
        }

        binding.sortByNameButton.setOnClickListener {
            staffViewModel.sortStaffByName(true)
            binding.sortOptionsLayout.visibility = View.GONE
        }

        binding.sortByNameDescButton.setOnClickListener {
            staffViewModel.sortStaffByName(false)
            binding.sortOptionsLayout.visibility = View.GONE
        }

        binding.sortByPositionButton.setOnClickListener {
            staffViewModel.sortStaffByPosition(true)
            binding.sortOptionsLayout.visibility = View.GONE
        }

        binding.sortByPositionDescButton.setOnClickListener {
            staffViewModel.sortStaffByPosition(false)
            binding.sortOptionsLayout.visibility = View.GONE
        }

        binding.sortByUnitButton.setOnClickListener {
            staffViewModel.sortStaffByUnit(true)
            binding.sortOptionsLayout.visibility = View.GONE
        }

        binding.sortByUnitDescButton.setOnClickListener {
            staffViewModel.sortStaffByUnit(false)
            binding.sortOptionsLayout.visibility = View.GONE
        }
    }
}