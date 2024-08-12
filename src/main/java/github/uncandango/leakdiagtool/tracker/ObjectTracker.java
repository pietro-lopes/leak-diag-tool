package github.uncandango.leakdiagtool.tracker;

import github.uncandango.leakdiagtool.Utils;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import net.neoforged.neoforge.common.util.Lazy;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static github.uncandango.leakdiagtool.tracker.Generation.EvolutionEvent.*;

public class ObjectTracker {

    private final Generation generation;
    private final IdentityWeakReference<Object> object;
    @Nullable
    private StackTraceElement[] stackTraceElements;

    private ObjectTracker(Object obj, ValidClass type) {
        this.object = new IdentityWeakReference<>(obj);
        this.generation = type.getCurrentGeneration();
        if (type.logStackTrace()) this.stackTraceElements = Thread.currentThread().getStackTrace();
    }

    public static ObjectTracker of(Object obj, ValidClass type) {
        return new ObjectTracker(obj, type);
    }

    public static void add(ClassTracker tracker, ValidClass type, Object obj) {
        tracker.add(type, obj);
    }

    public Object unwrap() {
        return object.get();
    }

    public Generation getGeneration() {
        return generation;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ObjectTracker tracker) {
            return object.equals(tracker.object);
        }
        return object.equals(obj);
    }

    public boolean isEmpty() {
        if (this.unwrap() instanceof ItemStack stack) {
            return stack.isEmpty();
        }
        return this.unwrap() == null;
    }

    @Override
    public int hashCode() {
        return object.hashCode();
    }

    public enum ValidClass {
        CHUNK(Set.of(CHUNK_LOAD, CHUNK_UNLOAD), true, "net.minecraft.world.level.chunk.ChunkAccess"),
        LEVEL(Set.of(LEVEL_LOAD, LEVEL_UNLOAD), true, "net.minecraft.client.multiplayer.ClientLevel", "net.minecraft.server.level.ServerLevel"),
        SERVER(Set.of(WORLD_LOAD, WORLD_UNLOAD), true, "net.minecraft.server.dedicated.DedicatedServer", "net.minecraft.client.server.IntegratedServer", "net.minecraft.gametest.framework.GameTestServer"),
        PLAYER(Set.of(PLAYER_LOGOUT, PLAYER_LOGIN), true, "net.minecraft.client.player.AbstractClientPlayer", "net.minecraft.server.level.ServerPlayer"),
        INGREDIENT(Set.of(RELOAD_RESOURCES), false, "net.minecraft.world.item.crafting.Ingredient"),
        ITEMSTACK(Set.of(RELOAD_RESOURCES), false, "net.minecraft.world.item.ItemStack");

        private final Lazy<Set<Class<?>>> classes;
        private final boolean stackTrace;
        private final Set<Generation.EvolutionEvent> events;
        private Generation generation;

        ValidClass(Set<Generation.EvolutionEvent> events, boolean stackTrace, String... classNames) {
            this.classes = Lazy.of(() -> Stream.of(classNames).map(Utils::safeGetClass).filter(Objects::nonNull).collect(Collectors.toSet()));
            this.generation = new Generation();
            this.stackTrace = stackTrace;
            this.events = events;
        }

        public static ValidClass getByKey(Class<?> clazz) {
            return Arrays.stream(ValidClass.values()).filter(type -> type.getClasses().contains(clazz)).findFirst().orElse(null);
        }

        public synchronized void bumpGeneration(Generation.EvolutionEvent trigger) {
            if (events.contains(trigger)) {
                generation = generation.bump(trigger);
            }
        }

        public synchronized Generation getCurrentGeneration() {
            return generation;
        }

        public boolean logStackTrace() {
            return stackTrace;
        }

        public Set<Generation.EvolutionEvent> getEvents() {
            return events;
        }

        public Set<Class<?>> getClasses() {
            return classes.get();
        }
    }
}
