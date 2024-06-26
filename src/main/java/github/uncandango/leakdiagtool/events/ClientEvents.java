package github.uncandango.leakdiagtool.events;

import github.uncandango.leakdiagtool.LeakDiagTool;
import github.uncandango.leakdiagtool.Scheduler;
import github.uncandango.leakdiagtool.commands.LDTCommands;
import github.uncandango.leakdiagtool.tracker.ClassTracker;
import github.uncandango.leakdiagtool.tracker.ObjectTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = LeakDiagTool.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
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
            var toast = new SystemToast(SystemToast.SystemToastIds.TUTORIAL_HINT, toastTitle, toastBody);
            var toastWarning = new SystemToast(SystemToast.SystemToastIds.TUTORIAL_HINT, toastTitle, toastWarn);
            var toastFinished = new SystemToast(SystemToast.SystemToastIds.TUTORIAL_HINT, toastTitle, toastDone);
            Minecraft.getInstance().getToasts().addToast(toast);
            Scheduler.INSTANCE.schedule("heap_dump_toast_warn", () -> Minecraft.getInstance().getToasts().addToast(toastWarning), 2L, LogicalSide.CLIENT);
            var delayed = CompletableFuture.delayedExecutor(3L, TimeUnit.SECONDS);
            CompletableFuture.runAsync(LDTCommands::dumpHeap, delayed)
                    .thenRun(() -> Minecraft.getInstance().getToasts().addToast(toastFinished));
        }
    }

    @SubscribeEvent
    public static void clientPlayerClone(ClientPlayerNetworkEvent.Clone event){
        ObjectTracker.add(ClassTracker.LEAKING, ObjectTracker.ValidClass.PLAYER, event.getOldPlayer());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBackToTitleScreen(MainTitleScreenOpeningEvent event) {
        for (var tracker : ClassTracker.values()){
            tracker.cleanNulls();
        }
    }

    @SubscribeEvent
    public static void registerClientCommands(RegisterClientCommandsEvent event) {
        LDTCommands.registerClientCommands(event.getDispatcher(), event.getBuildContext());
    }
}
