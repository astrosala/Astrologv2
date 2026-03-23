package com.astrolog.app.ui.newsession

import android.os.Bundle

// Generado por Safe Args — stub manual para referencia
// El plugin navigation-safe-args lo genera automáticamente al compilar
data class NewSessionFragmentArgs(val sessionId: Long = -1L) {
    companion object {
        @JvmStatic
        fun fromBundle(bundle: Bundle): NewSessionFragmentArgs {
            return NewSessionFragmentArgs(bundle.getLong("sessionId", -1L))
        }
    }
}
