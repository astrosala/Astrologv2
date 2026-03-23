package com.astrolog.app.ui.sessions

import androidx.navigation.ActionOnlyNavDirections
import com.astrolog.app.R

// Generado por Safe Args — incluido manualmente para referencia del IDE
// En el proyecto real se genera automáticamente con el plugin navigation-safe-args-gradle-plugin
// Añade en build.gradle (root): classpath "androidx.navigation:navigation-safe-args-gradle-plugin:2.7.6"
// Añade en app/build.gradle: id 'androidx.navigation.safeargs.kotlin'

class SessionsFragmentDirections {
    companion object {
        fun actionSessionsToNewSession(sessionId: Long = -1L): androidx.navigation.NavDirections {
            return object : androidx.navigation.NavDirections {
                override val actionId = R.id.action_sessions_to_newSession
                override val arguments = android.os.Bundle().apply {
                    putLong("sessionId", sessionId)
                }
            }
        }
    }
}
