package github.uncandango.leakdiagtool.mixin;

import github.uncandango.leakdiagtool.tracker.ClassTracker;
import github.uncandango.leakdiagtool.tracker.ObjectTracker;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(Level.class)
public class LevelMixin {

    @Inject(method = "<init>", at = @At("TAIL"))
    void ldt$grabLevelInstance(WritableLevelData arg, ResourceKey<Level> arg2, RegistryAccess arg3, Holder<DimensionType> arg4, Supplier<ProfilerFiller> supplier, boolean bl, boolean bl2, long l, int i, CallbackInfo ci) {
        ObjectTracker.add(ClassTracker.LOADED, ObjectTracker.ValidClass.LEVEL, this);
    }
}
