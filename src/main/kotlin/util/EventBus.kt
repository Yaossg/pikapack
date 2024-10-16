package pikapack.util

import pikapack.plan.SyncPlan
import java.util.LinkedList
import java.util.Queue
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class EventBus {
    val queue: Queue<SyncPlan> = LinkedList<SyncPlan>()
    val lock = ReentrantLock()

    fun submit(plan: SyncPlan) = lock.withLock {
        queue.add(plan)
    }

    fun poll() = lock.withLock {
        queue.poll()?.execute()
    }
}