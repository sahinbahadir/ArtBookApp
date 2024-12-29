package com.example.artbookapp.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.artbookapp.databinding.ArtRecyclerRowBinding
import com.example.artbookapp.entities.ArtModel

class ArtAdapter(val artList: ArrayList<ArtModel>) : RecyclerView.Adapter<ArtAdapter.ArtHolder>(){

    class ArtHolder(val binding : ArtRecyclerRowBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtHolder {
        val binding = ArtRecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArtHolder(binding)
    }

    override fun onBindViewHolder(holder: ArtHolder, position: Int) {
        holder.binding.recyclerRowTextView.setText(artList.get(position).artName)
        holder.binding.recyclerRowTextView.setOnClickListener {
            // intent to art detail
        }
    }

    override fun getItemCount(): Int {
        return artList.size
    }
}