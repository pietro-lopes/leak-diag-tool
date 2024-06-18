package github.uncandango.leakdiagtool.events;

import github.uncandango.leakdiagtool.LeakDiagTool;
import github.uncandango.leakdiagtool.Scheduler;
import github.uncandango.leakdiagtool.commands.LDTCommands;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = LeakDiagTool.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class ClientEvents {

    public static boolean heapDumpScheduledOnClientExit = false;

    public static AtomicBoolean lock = new AtomicBoolean(true);
    public static Class<?> mainScreenClass = null;

    @SubscribeEvent
    public static void onOpenScreen(ScreenEvent.Opening event) {
        if (event.getNewScreen() instanceof TitleScreen) {
            lock.set(false);
            mainScreenClass = event.getNewScreen().getClass();
        }
    }

    private static boolean tryCheckInstanceOf(Screen screen) {
        if (mainScreenClass == null) return false;
        return mainScreenClass.isAssignableFrom(screen.getClass());
    }

    private static boolean tryCheckTranslationKey(Screen screen) {
        if (screen.getTitle().getContents() instanceof TranslatableContents contents) {
            if (contents.getKey().equals("narrator.screen.title")) {
                mainScreenClass = screen.getClass();
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        if (!lock.get()) {
            if (tryCheckInstanceOf(event.getScreen()) || tryCheckTranslationKey(event.getScreen())) {
                lock.set(true);
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
                    Scheduler.INSTANCE.schedule("heap_dump_now", LDTCommands::dumpHeap, 5L, LogicalSide.CLIENT);
                    Runnable isDoneChecker = () -> {
                        if (Scheduler.INSTANCE.isTaskDone("heap_dump_now")) {
                            Minecraft.getInstance().getToasts().addToast(toastFinished);
                            Scheduler.INSTANCE.cancelTask("checker");
                        }
                    };
                    var checkerTask = Scheduler.INSTANCE.getExecutor().scheduleAtFixedRate(isDoneChecker, 1, 1, TimeUnit.SECONDS);
                    Scheduler.INSTANCE.addCustom("checker", checkerTask);
                }
            }
        }
    }


}
