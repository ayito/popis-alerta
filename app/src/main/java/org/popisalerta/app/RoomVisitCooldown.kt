package org.popisalerta.app

class RoomVisitCooldown(private val cooldownMs: Long) {

    fun canCreateVisit(nowMs: Long, lastVisitStartedAtMs: Long?): Boolean {
        if (lastVisitStartedAtMs == null) {
            return true
        }

        return nowMs - lastVisitStartedAtMs >= cooldownMs
    }
}
