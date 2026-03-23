package com.astrolog.app.ui.stats

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.astrolog.app.data.dao.ObjectSummary
import com.astrolog.app.databinding.ItemObjectSummaryBinding

class ObjectSummaryAdapter : ListAdapter<ObjectSummary, ObjectSummaryAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemObjectSummaryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    inner class ViewHolder(private val b: ItemObjectSummaryBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(s: ObjectSummary) {
            b.textObjectName.text = s.objectName
            b.textLproSummary.text = if (s.lproSubs > 0) "L-Pro: ${s.lproSubs} subs · ${s.lproTime}" else "L-Pro: —"
            b.textHaSummary.text = if (s.haSubs > 0) "Hα: ${s.haSubs} subs · ${s.haTime}" else "Hα: —"
            b.textOiiiSummary.text = if (s.oiiiSubs > 0) "OIII: ${s.oiiiSubs} subs · ${s.oiiiTime}" else "OIII: —"
            b.textTotalSummary.text = "Total: ${s.totalTime}"
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ObjectSummary>() {
        override fun areItemsTheSame(a: ObjectSummary, b: ObjectSummary) = a.objectName == b.objectName
        override fun areContentsTheSame(a: ObjectSummary, b: ObjectSummary) = a == b
    }
}
