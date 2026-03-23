package com.astrolog.app.ui.calendar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.astrolog.app.R
import com.astrolog.app.data.entity.AstroObject
import com.astrolog.app.databinding.ItemCalendarBinding

class CalendarAdapter(
    private val onItemClick: (AstroObject) -> Unit
) : ListAdapter<AstroObject, CalendarAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val b = ItemCalendarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(b)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    inner class ViewHolder(private val b: ItemCalendarBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(obj: AstroObject) {
            b.textCalObjectName.text = obj.name
            b.textCalFilter.text = obj.mainFilter.ifEmpty { "—" }
            b.textMar.text = obj.visibilityMarch
            b.textAbr.text = obj.visibilityApril
            b.textMay.text = obj.visibilityMay
            b.textJun.text = obj.visibilityJune

            listOf(
                b.textMar to obj.visibilityMarch,
                b.textAbr to obj.visibilityApril,
                b.textMay to obj.visibilityMay,
                b.textJun to obj.visibilityJune
            ).forEach { (tv, vis) ->
                val (bg, fg) = when (vis) {
                    "★" -> R.drawable.bg_optimal to R.color.vis_optimal
                    "✓" -> R.drawable.bg_good to R.color.vis_good
                    "~" -> R.drawable.bg_low to R.color.vis_low
                    else -> R.drawable.bg_none to R.color.vis_none
                }
                tv.setBackgroundResource(bg)
                tv.setTextColor(tv.context.getColor(fg))
            }

            b.root.setOnClickListener { onItemClick(obj) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<AstroObject>() {
        override fun areItemsTheSame(a: AstroObject, b: AstroObject) = a.id == b.id
        override fun areContentsTheSame(a: AstroObject, b: AstroObject) = a == b
    }
}
