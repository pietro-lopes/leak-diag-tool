package github.uncandango.leakdiagtool.events;

import github.uncandango.leakdiagtool.LeakDiagTool;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = LeakDiagTool.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MainTitleScreenOpeningEvent extends Event {

    private static final AtomicBoolean lock = new AtomicBoolean(true);
    private static Class<?> mainScreenClass = null;

    @SubscribeEvent
    public static void onOpenScreen(ScreenEvent.Opening event) {
        if (event.getNewScreen() instanceof TitleScreen && event.getCurrentScreen() instanceof GenericDirtMessageScreen oldscreen){
            if (oldscreen.getTitle().getContents() instanceof TranslatableContents contents) {
                if (contents.getKey().equals("menu.savingLevel")) lock.set(false);
            }
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
                MinecraftForge.EVENT_BUS.post(new MainTitleScreenOpeningEvent(event.getScreen()));
            }
        }
    }

    private final Screen screen;
    public MainTitleScreenOpeningEvent(Screen screen){
        this.screen = screen;
    }

    public Screen getScreen() {
        return screen;
    }
}
