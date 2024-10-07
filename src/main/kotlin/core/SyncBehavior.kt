package pikapack.core

import pikapack.plan.SyncPlan
import java.nio.file.Path

interface SyncBehavior {
    fun refresh(plan: SyncPlan)
    fun restore(plan: SyncPlan)
}
