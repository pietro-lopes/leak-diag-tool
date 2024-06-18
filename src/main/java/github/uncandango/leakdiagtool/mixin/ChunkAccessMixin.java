package github.uncandango.leakdiagtool.mixin;

import github.uncandango.leakdiagtool.LeakDiagTool;
import github.uncandango.leakdiagtool.tracker.ClassTracker;
import net.minecraft.core.Registry;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkAccess.class)
public class ChunkAccessMixin {

    @Inject(method = "<init>", at = @At("TAIL"))
    void ldt$grabChunkInstance(ChunkPos arg, UpgradeData arg2, LevelHeightAccessor arg3, Registry arg4, long l, LevelChunkSection[] args, BlendingData arg5, CallbackInfo ci) {
        LeakDiagTool.ValidClass.CHUNK.getClasses().forEach(clazz -> {
            if (clazz.isAssignableFrom(this.getClass())) ClassTracker.LOADED.add(clazz, this);
        });
    }
}
