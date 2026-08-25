package org.popisalerta.app

class BathroomVisitCooldown(private val cooldownMs: Long) {

    fun canCreateVisit(nowMs: Long, lastVisitStartedAtMs: Long?): Boolean {
        if (lastVisitStartedAtMs == null) {
            return true
        }

        return nowMs - lastVisitStartedAtMs >= cooldownMs
    }
}
