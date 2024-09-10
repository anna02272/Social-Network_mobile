package com.example.socialnetwork.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.socialnetwork.R
import androidx.recyclerview.widget.RecyclerView


class BulletPointAdapter(private val context: Context, private val items: List<String>) :
    RecyclerView.Adapter<BulletPointAdapter.BulletPointViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BulletPointViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item_with_bullet, parent, false)
        return BulletPointViewHolder(view)
    }

    override fun onBindViewHolder(holder: BulletPointViewHolder, position: Int) {
        val item = items[position]
        holder.itemText.text = item
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class BulletPointViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bulletPoint: TextView = itemView.findViewById(R.id.bullet_point)
        val itemText: TextView = itemView.findViewById(R.id.item_text)
    }
}
