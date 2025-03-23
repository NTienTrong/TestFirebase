package com.edu.tlucontact.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.edu.tlucontact.R
import com.edu.tlucontact.data.models.Staff
import com.edu.tlucontact.data.repositories.StaffRepository
import com.edu.tlucontact.databinding.StaffItemBinding

class StaffAdapter(
    private var staffList: MutableList<Staff>,
    private val onItemClick: (Staff) -> Unit
) : RecyclerView.Adapter<StaffAdapter.StaffViewHolder>() {

    private val staffRepository = StaffRepository()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StaffViewHolder {
        val binding = StaffItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StaffViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StaffViewHolder, position: Int) {
        val staff = staffList[position]
        holder.bind(staff)
    }

    override fun getItemCount(): Int = staffList.size

    fun submitList(newList: List<Staff>) {
        staffList.clear()
        staffList.addAll(newList)
        notifyDataSetChanged()
    }

    inner class StaffViewHolder(private val binding: StaffItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(staff: Staff) {
            binding.staffNameTextView.text = staff.fullName
            binding.staffPositionTextView.text = staff.position

            // Lấy tên đơn vị từ DocumentReference
            staff.unit?.let { unitRef ->
                staffRepository.getUnitDetails(unitRef).observeForever { unit ->
                    binding.staffUnitTextView.text = unit?.name ?: "Chưa rõ đơn vị"
                }
            } ?: run {
                binding.staffUnitTextView.text = "Chưa rõ đơn vị"
            }

            Glide.with(binding.root.context)
                .load(staff.photoURL)
                .placeholder(R.drawable.ic_default_avatar)
                .error(R.drawable.ic_default_avatar)
                .into(binding.staffImageView)

            binding.root.setOnClickListener {
                onItemClick(staff)
            }
        }
    }
}