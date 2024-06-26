package github.uncandango.leakdiagtool.tracker;

import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static github.uncandango.leakdiagtool.tracker.Generation.EvolutionEvent.*;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber
public class GenerationEvolver {

    static void bumpGeneration(Generation.EvolutionEvent trigger){
        for (ObjectTracker.ValidClass type : ObjectTracker.ValidClass.values()) {
            type.bumpGeneration(trigger);
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event){
        bumpGeneration(LEVEL_UNLOAD);
    }

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event){
        bumpGeneration(LEVEL_LOAD);
    }

    @SubscribeEvent
    public static void onWorldLoad(ServerAboutToStartEvent event){
        bumpGeneration(WORLD_LOAD);
    }

    @SubscribeEvent
    public static void onWorldUnload(ServerStoppingEvent event){
        bumpGeneration(WORLD_UNLOAD);
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event){
        bumpGeneration(CHUNK_LOAD);
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event){
        bumpGeneration(CHUNK_UNLOAD);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onReloadResourcesHigh(AddReloadListenerEvent event){
        ClassTracker.LOADED.transferTo(ClassTracker.LEAKING, ObjectTracker.ValidClass.INGREDIENT);
//        ClassTracker.LOADED.transferTo(ClassTracker.LEAKING, ObjectTracker.ValidClass.ITEMSTACK);
        bumpGeneration(RELOAD_RESOURCES);
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event){
        bumpGeneration(PLAYER_LOGIN);
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event){
        bumpGeneration(PLAYER_LOGOUT);
    }
}
