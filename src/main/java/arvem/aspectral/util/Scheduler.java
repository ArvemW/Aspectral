package arvem.aspectral.util;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Simple scheduler for delayed tasks.
 * Executes tasks after a specified number of ticks.
 */
public class Scheduler {

    private final ConcurrentLinkedQueue<ScheduledTask> tasks = new ConcurrentLinkedQueue<>();

    /**
     * Schedule a task to run after a delay.
     *
     * @param task The task to run
     * @param delayTicks Number of ticks to wait
     */
    public void schedule(Runnable task, int delayTicks) {
        tasks.add(new ScheduledTask(task, delayTicks));
    }

    /**
     * Schedule a repeating task.
     *
     * @param task The task to run
     * @param delayTicks Initial delay in ticks
     * @param periodTicks Ticks between executions
     * @return A handle to cancel the task
     */
    public ScheduledTask scheduleRepeating(Runnable task, int delayTicks, int periodTicks) {
        ScheduledTask scheduled = new ScheduledTask(task, delayTicks, periodTicks);
        tasks.add(scheduled);
        return scheduled;
    }

    /**
     * Called every tick to process scheduled tasks.
     */
    public void tick() {
        Iterator<ScheduledTask> iterator = tasks.iterator();
        while (iterator.hasNext()) {
            ScheduledTask task = iterator.next();

            if (task.isCancelled()) {
                iterator.remove();
                continue;
            }

            task.ticksRemaining--;

            if (task.ticksRemaining <= 0) {
                try {
                    task.task.run();
                } catch (Exception e) {
                    // Log but don't crash
                    e.printStackTrace();
                }

                if (task.period > 0) {
                    // Repeating task - reset timer
                    task.ticksRemaining = task.period;
                } else {
                    // One-shot task - remove
                    iterator.remove();
                }
            }
        }
    }

    /**
     * Represents a scheduled task.
     */
    public static class ScheduledTask {
        private final Runnable task;
        private int ticksRemaining;
        private final int period;
        private boolean cancelled = false;

        public ScheduledTask(Runnable task, int delay) {
            this(task, delay, 0);
        }

        public ScheduledTask(Runnable task, int delay, int period) {
            this.task = task;
            this.ticksRemaining = delay;
            this.period = period;
        }

        public void cancel() {
            this.cancelled = true;
        }

        public boolean isCancelled() {
            return cancelled;
        }
    }
}
