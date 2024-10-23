package pikapack.core

import pikapack.plan.SyncPlan

interface SyncBehavior {
    fun refresh(plan: SyncPlan)
    fun restore(plan: SyncPlan)
}
