package com.astrolog.app.ui.wishlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.astrolog.app.R
import com.astrolog.app.data.entity.AstroObject
import com.astrolog.app.data.entity.Season
import com.astrolog.app.databinding.ItemWishlistBinding

class WishlistAdapter(
    private val onStatusClick: (AstroObject) -> Unit,
    private val onEditClick: (AstroObject) -> Unit,
    private val onAlertClick: (AstroObject) -> Unit,
    private val onDeleteClick: (AstroObject) -> Unit
) : ListAdapter<AstroObject, WishlistAdapter.ViewHolder>(DiffCallback()) {

    // Almacén de temporadas para traducir IDs a nombres de meses 
    private var seasonsList: List<Season> = emptyList()

    fun setSeasons(seasons: List<Season>) {
        this.seasonsList = seasons
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val b = ItemWishlistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(b)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    inner class ViewHolder(private val b: ItemWishlistBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(obj: AstroObject) {
            b.textWishObjectName.text = obj.name
            b.textWishFilter.text = obj.mainFilter.ifEmpty { "Filtro no definido" }

            // Buscamos si existe la temporada para este objeto
            val mySeason = seasonsList.find { it.id == obj.seasonId }

            // Actualizamos los indicadores de visibilidad (estrellas/rayas) 
            // Nota: Se asume que indicatorM1 etc. existen en tu XML
            b.indicatorM1.text = if (obj.visibilityMonth1.isNullOrBlank()) "—" else obj.visibilityMonth1
            b.indicatorM2.text = if (obj.visibilityMonth2.isNullOrBlank()) "—" else obj.visibilityMonth2
            b.indicatorM3.text = if (obj.visibilityMonth3.isNullOrBlank()) "—" else obj.visibilityMonth3
            b.indicatorM4.text = if (obj.visibilityMonth4.isNullOrBlank()) "—" else obj.visibilityMonth4

            // Ocultamos el campo antiguo para evitar confusión
            b.textWishVisibility.visibility = android.view.View.GONE

            // Lógica de referencias (subs y tiempos) 
            val refParts = mutableListOf<String>()
            if (obj.refLproSubs > 0) refParts.add("L-Pro: ${obj.refLproSubs}×${obj.refLproExpSec}s")
            if (obj.refHaSubs > 0) refParts.add("Hα: ${obj.refHaSubs}×${obj.refHaExpSec}s")
            if (obj.refOiiiSubs > 0) refParts.add("OIII: ${obj.refOiiiSubs}×${obj.refOiiiExpSec}s")

            if (refParts.isNotEmpty()) {
                b.textWishRef.visibility = android.view.View.VISIBLE
                b.textWishRef.text = "Ref: ${refParts.joinToString(" · ")}  →  ${obj.refTotalTime}"
            } else {
                b.textWishRef.visibility = android.view.View.GONE
            }

            // Estado y Colores
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
