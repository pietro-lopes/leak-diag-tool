package github.uncandango.leakdiagtool;

import com.mojang.logging.LogUtils;
import github.uncandango.leakdiagtool.events.CommonEvents;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(LeakDiagTool.MOD_ID)
public final class LeakDiagTool {
    public static final String MOD_ID = "leakdiagtool";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static boolean debugMode = false;

    public LeakDiagTool() {
        final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        final IEventBus eventBus = MinecraftForge.EVENT_BUS;
        eventBus.addListener(Scheduler.INSTANCE::onShutdown);
        if (debugMode) {
            eventBus.addListener(CommonEvents::levelTick);
        }
    }
}
