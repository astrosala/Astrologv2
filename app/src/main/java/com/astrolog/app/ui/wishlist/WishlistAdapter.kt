package com.astrolog.app.ui.wishlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.astrolog.app.R
import com.astrolog.app.data.entity.AstroObject
import com.astrolog.app.databinding.ItemWishlistBinding

class WishlistAdapter(
    private val onStatusClick: (AstroObject) -> Unit,
    private val onEditClick: (AstroObject) -> Unit,
    private val onAlertClick: (AstroObject) -> Unit,
    private val onDeleteClick: (AstroObject) -> Unit
) : ListAdapter<AstroObject, WishlistAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val b = ItemWishlistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(b)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    inner class ViewHolder(private val b: ItemWishlistBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(obj: AstroObject) {
            b.textWishObjectName.text = obj.name
            b.textWishFilter.text = obj.mainFilter.ifEmpty { "Filtro no definido" }

            
// --- NUEVOS INDICADORES VISUALES ---
            b.indicatorM1.text = obj.visibilityMonth1.ifEmpty { "—" }
            b.indicatorM2.text = obj.visibilityMonth2.ifEmpty { "—" }
            b.indicatorM3.text = obj.visibilityMonth3.ifEmpty { "—" }
            b.indicatorM4.text = obj.visibilityMonth4.ifEmpty { "—" }
            // Ocultamos el texto antiguo para que no estorbe
            b.textWishVisibility.visibility = android.view.View.GONE

            
            // Visibilidad
            //b.textWishVisibility.text = buildString {
                //if (obj.visibilityMonth1.isNotEmpty() && obj.visibilityMonth1 != "—") append("M1:${obj.visibilityMonth1} ")
                //if (obj.visibilityMonth2.isNotEmpty() && obj.visibilityMonth2 != "—") append("M2:${obj.visibilityMonth2} ")
                //if (obj.visibilityMonth3.isNotEmpty() && obj.visibilityMonth3 != "—") append("M3:${obj.visibilityMonth3} ")
                //if (obj.visibilityMonth4.isNotEmpty() && obj.visibilityMonth4 != "—") append("M4:${obj.visibilityMonth4}")
            //}.trim().ifEmpty { "Sin visibilidad definida" }

            // Referencia de subs
            val refParts = mutableListOf<String>()
            if (obj.refLproSubs > 0) refParts.add("L-Pro: ${obj.refLproSubs}×${obj.refLproExpSec}s")
            if (obj.refHaSubs > 0) refParts.add("Hα: ${obj.refHaSubs}×${obj.refHaExpSec}s")
            if (obj.refOiiiSubs > 0) refParts.add("OIII: ${obj.refOiiiSubs}×${obj.refOiiiExpSec}s")
            if (obj.refSiiSubs > 0) refParts.add("SII: ${obj.refSiiSubs}×${obj.refSiiExpSec}s")
            if (obj.refLextSubs > 0) refParts.add("L-Ext: ${obj.refLextSubs}×${obj.refLextExpSec}s")
            if (obj.refCustom1Subs > 0) refParts.add("C1: ${obj.refCustom1Subs}×${obj.refCustom1ExpSec}s")
            if (obj.refCustom2Subs > 0) refParts.add("C2: ${obj.refCustom2Subs}×${obj.refCustom2ExpSec}s")

            if (refParts.isNotEmpty()) {
                b.textWishRef.visibility = android.view.View.VISIBLE
                b.textWishRef.text = "Ref: ${refParts.joinToString(" · ")}  →  ${obj.refTotalTime}"
            } else {
                b.textWishRef.visibility = android.view.View.GONE
            }

            // Estado
            val (bgColor, fgColor) = when (obj.status) {
                "Completado" -> R.color.status_done_bg to R.color.status_done_fg
                "En curso" -> R.color.status_progress_bg to R.color.status_progress_fg
                else -> R.color.status_pending_bg to R.color.status_pending_fg
            }
            b.chipStatus.text = obj.status
            b.chipStatus.setChipBackgroundColorResource(bgColor)
            b.chipStatus.setTextColor(b.root.context.getColor(fgColor))
            b.chipStatus.setOnClickListener { onStatusClick(obj) }

            b.buttonEdit.setOnClickListener { onEditClick(obj) }
            b.buttonAlert.isSelected = obj.alertEnabled
            b.buttonAlert.text = if (obj.alertEnabled) "★ Alerta ON" else "☆ Alerta"
            b.buttonAlert.setOnClickListener { onAlertClick(obj) }
            b.buttonDelete.setOnClickListener { onDeleteClick(obj) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<AstroObject>() {
        override fun areItemsTheSame(a: AstroObject, b: AstroObject) = a.id == b.id
        override fun areContentsTheSame(a: AstroObject, b: AstroObject) = a == b
    }
}
