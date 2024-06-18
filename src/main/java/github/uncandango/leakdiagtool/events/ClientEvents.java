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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = LeakDiagTool.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEvents {

    public static boolean heapDumpScheduledOnClientExit = false;

    public static AtomicBoolean lock = new AtomicBoolean(true);
    public static Class<?> mainScreenClass = null;

    @SubscribeEvent
    public static void onOpenScreen(ScreenEvent.Opening event) {
        if (event.getNewScreen() instanceof TitleScreen){
            lock.set(false);
            mainScreenClass = event.getNewScreen().getClass();
        }
    }

    private static boolean tryCheckInstanceOf(Screen screen) {
        if (mainScreenClass == null) return false;
        return mainScreenClass.isAssignableFrom(screen.getClass());
    }

    private static boolean tryCheckTranslationKey(Screen screen) {
        if (screen.getTitle().getContents() instanceof TranslatableContents contents){
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
            if (tryCheckInstanceOf(event.getScreen()) || tryCheckTranslationKey(event.getScreen())){
                    lock.set(true);
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
                        Scheduler.INSTANCE.schedule("heap_dump_toast_warn", () -> Minecraft.getInstance().getToasts().addToast(toastWarning),2L, LogicalSide.CLIENT);
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
