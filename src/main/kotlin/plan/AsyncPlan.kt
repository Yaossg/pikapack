package pikapack.plan

import pikapack.util.EventBus
import pikapack.util.Options
import java.nio.file.FileSystems
import java.nio.file.StandardWatchEventKinds
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class AsyncPlan(val options: Options, val syncPlan: SyncPlan) {
    private val eventBus: EventBus = EventBus()
    private val scheduler = Executors.newScheduledThreadPool(1)
    private val watchService = FileSystems.getDefault().newWatchService()
    private val executor = Executors.newSingleThreadExecutor()

    init {
        // Initialize the scheduled task
        if (syncPlan.options.schedule != -1) {
            scheduler.scheduleAtFixedRate({
                eventBus.submit(syncPlan)
            }, 0, syncPlan.options.schedule.toLong(), TimeUnit.MINUTES)
        }

        // Initialize and start the watch service
        if (options.watch) {
            syncPlan.options.src.register(
                watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_DELETE
            )

            Thread {
                try {
                    while (!Thread.currentThread().isInterrupted) {
                        val key = watchService.take()
                        for (event in key.pollEvents()) {
                            val kind = event.kind()
                            if (kind == StandardWatchEventKinds.OVERFLOW) continue

                            // Submit the sync plan when a file event occurs
                            eventBus.submit(syncPlan)
                        }
                        key.reset()
                    }
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }.start()
        }


        // Start the executor thread
        executor.submit {
            try {
                while (!Thread.currentThread().isInterrupted) {
                    eventBus.poll()
                    Thread.sleep(1000)
                }
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
    }

    fun shutdown() {
        executor.shutdown()
        scheduler.shutdown()
        watchService.close()
    }
}