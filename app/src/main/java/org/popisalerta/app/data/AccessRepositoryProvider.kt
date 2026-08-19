package org.popisalerta.app.data

import android.content.Context
import org.popisalerta.app.data.local.AccessDatabase

object AccessRepositoryProvider {
    @Volatile
    var factory: ((Context) -> AccessRepository)? = null

    fun create(context: Context): AccessRepository = factory?.invoke(context)
        ?: DefaultAccessRepository(
            AccessDatabase.getInstance(context.applicationContext).accessDao()
        )

    fun reset() {
        factory = null
    }
}
