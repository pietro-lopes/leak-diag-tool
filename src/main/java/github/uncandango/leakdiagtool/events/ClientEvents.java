package github.uncandango.leakdiagtool.events;

import github.uncandango.leakdiagtool.LeakDiagTool;
import github.uncandango.leakdiagtool.Scheduler;
import github.uncandango.leakdiagtool.commands.LDTCommands;
import github.uncandango.leakdiagtool.tracker.ClassTracker;
import github.uncandango.leakdiagtool.tracker.ObjectTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = LeakDiagTool.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class ClientEvents {

    public static boolean heapDumpScheduledOnClientExit = false;


    @SubscribeEvent
    public static void onScreenRender(MainTitleScreenOpeningEvent event) {
        if (heapDumpScheduledOnClientExit) {
            heapDumpScheduledOnClientExit = false;
            var toastTitle = Component.literal("Scheduled Heap Dump");
            var toastBody = Component.literal("Heap dump running in 5 seconds...");
            var toastWarn = Component.literal("It may take a few minutes to complete!");
            var toastDone = Component.literal("Heap dump is finished!");
            var toast = new SystemToast(SystemToast.SystemToastId.PERIODIC_NOTIFICATION, toastTitle, toastBody);
            var toastWarning = new SystemToast(SystemToast.SystemToastId.PERIODIC_NOTIFICATION, toastTitle, toastWarn);
            var toastFinished = new SystemToast(SystemToast.SystemToastId.PERIODIC_NOTIFICATION, toastTitle, toastDone);
            Minecraft.getInstance().getToasts().addToast(toast);
            Scheduler.INSTANCE.schedule("heap_dump_toast_warn", () -> Minecraft.getInstance().getToasts().addToast(toastWarning), 2L, LogicalSide.CLIENT);
            var delayed = CompletableFuture.delayedExecutor(3L, TimeUnit.SECONDS);
            CompletableFuture.runAsync(LDTCommands::dumpHeap, delayed)
                    .thenRun(() -> Minecraft.getInstance().getToasts().addToast(toastFinished));
        }
    }

    @SubscribeEvent
    public static void clientPlayerClone(ClientPlayerNetworkEvent.Clone event) {
        ObjectTracker.add(ClassTracker.LEAKING, ObjectTracker.ValidClass.PLAYER, event.getOldPlayer());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBackToTitleScreen(MainTitleScreenOpeningEvent event) {
        for (var tracker : ClassTracker.values()) {
            tracker.cleanNulls();
        }
    }

    @SubscribeEvent
    public static void registerClientCommands(RegisterClientCommandsEvent event) {
        LDTCommands.registerClientCommands(event.getDispatcher(), event.getBuildContext());
    }
}
