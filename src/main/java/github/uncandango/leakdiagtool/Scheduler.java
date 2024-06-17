package github.uncandango.leakdiagtool;

import net.minecraft.client.Minecraft;
import net.minecraft.server.TickTask;
import net.neoforged.common.util.LogicalSidedProvider;
import net.neoforged.event.server.ServerStoppedEvent;
import net.neoforged.fml.LogicalSide;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.*;

public enum Scheduler {
    INSTANCE;

    private final Map<String, Future<?>> scheduledTasks = new ConcurrentHashMap<>();
    private ScheduledExecutorService executor = null;

    public ScheduledExecutorService getExecutor() {
        if (executor == null) {
            executor = Executors.newSingleThreadScheduledExecutor();
        }
        return executor;
    }

    public void onShutdown(ServerStoppedEvent event) {
        if (executor != null) executor.shutdown();
    }

    @Nullable
    public Future<?> getTask(String id) {
        return scheduledTasks.getOrDefault(id, null);
    }

    private Future<?> createTask(Runnable task, Long seconds, LogicalSide side) {
        return getExecutor().schedule(
                () -> LogicalSidedProvider.WORKQUEUE.get(side)
                        .execute(task), seconds, TimeUnit.SECONDS);
    }

    public void addCustom(String id, Future<?> future){
        scheduledTasks.putIfAbsent(id, future);
    }

    public void schedule(String taskId, Runnable task, Long seconds, LogicalSide side){
        var future = createTask(task, seconds, side);
        cancelTask(taskId);
        scheduledTasks.putIfAbsent(taskId, future);
    }

    public boolean isTaskDone(String id) {
        var task = getTask(id);
        if (task == null) {
            LeakDiagTool.LOGGER.error(
                    "Task Id does not exists.",
                    new IllegalArgumentException("Task does not exists!"));
            return true;
        }
        return task.isDone();
    }

    public boolean cancelTask(String id) {
        var removedTask = scheduledTasks.remove(id);
        if (removedTask != null) {
            return removedTask.cancel(true);
        }
        return false;
    }
}
