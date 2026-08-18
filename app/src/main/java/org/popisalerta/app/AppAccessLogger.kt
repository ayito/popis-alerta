package org.popisalerta.app

import org.popisalerta.app.data.AccessRepository

class AppAccessLogger(
    private val accessRepository: AccessRepository,
) {
    suspend fun logAppOpen() {
        accessRepository.logAccess(APP_OPEN_TRIGGER_SOURCE)
    }

    suspend fun logAppResume() {
        accessRepository.logAccess(APP_RESUME_TRIGGER_SOURCE)
    }

    companion object {
        const val APP_OPEN_TRIGGER_SOURCE = "APP_OPEN"
        const val APP_RESUME_TRIGGER_SOURCE = "APP_RESUME"
    }
}
