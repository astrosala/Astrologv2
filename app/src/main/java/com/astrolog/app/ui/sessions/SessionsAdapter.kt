package com.astrolog.app.ui.sessions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.astrolog.app.data.entity.Session
import com.astrolog.app.databinding.ItemSessionBinding

class SessionsAdapter(
    private val onItemClick: (Session) -> Unit
) : ListAdapter<Session, SessionsAdapter.ViewHolder>(DiffCallback()) {

    fun getSessionAt(position: Int): Session = getItem(position)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSessionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val b: ItemSessionBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(s: Session) {
            b.textSessionNumber.text = "#${s.sessionNumber}"
            b.textObjectName.text = s.objectName
            b.textDate.text = s.date
            b.textConditions.text = "${s.conditions} · Seeing ${s.seeing}/5"

            // Filtros
            b.textLpro.text = if (s.lproSubs > 0) "L-Pro: ${s.lproSubs}×${s.lproExpSec}s (${s.lproTime})" else "L-Pro: —"
            b.textHa.text = if (s.haSubs > 0) "Hα: ${s.haSubs}×${s.haExpSec}s (${s.haTime})" else "Hα: —"
            b.textOiii.text = if (s.oiiiSubs > 0) "OIII: ${s.oiiiSubs}×${s.oiiiExpSec}s (${s.oiiiTime})" else "OIII: —"
            b.textTotal.text = "Total: ${s.totalTime}"

            // Indicador seeing (5 puntos)
            val dots = listOf(b.dot1, b.dot2, b.dot3, b.dot4, b.dot5)
            dots.forEachIndexed { i, dot ->
                dot.isSelected = i < s.seeing
            }

            b.root.setOnClickListener { onItemClick(s) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Session>() {
        override fun areItemsTheSame(a: Session, b: Session) = a.id == b.id
        override fun areContentsTheSame(a: Session, b: Session) = a == b
    }
}
