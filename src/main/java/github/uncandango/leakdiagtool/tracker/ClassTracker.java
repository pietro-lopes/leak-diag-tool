package github.uncandango.leakdiagtool.tracker;

import github.uncandango.leakdiagtool.LeakDiagTool;
import net.minecraft.Util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public enum ClassTracker {
    LOADED,
    LEAKING;

    private static long lastTimeRanGC = 0;
    private final Map<Class<?>, Set<IdentityWeakReference<Object>>> map;

    ClassTracker() {
        this.map = new ConcurrentHashMap<>();
    }

    public void add(Class<?> clazz, Object object) {
        map.computeIfPresent(clazz, (key, value) -> {
            value.add(new IdentityWeakReference<>(object));
            return value;
        });
        map.computeIfAbsent(clazz, (key) -> {
            var newSet = new HashSet<IdentityWeakReference<Object>>();
            newSet.add(new IdentityWeakReference<>(object));
            return newSet;
        });
    }

    public int size(Class<?> clazz) {
        return size(clazz, false);
    }

    public int size(Class<?> clazz, boolean cleanUp) {
        if (cleanUp && (Util.getMillis() - lastTimeRanGC) > 5000) {
            System.gc();
            lastTimeRanGC = Util.getMillis();
            LeakDiagTool.LOGGER.debug("Running GC...");
        }
        var set = map.get(clazz);
        if (set == null) return 0;
        set.removeIf(value -> value.get() == null);
        return set.size();
    }

    public boolean contains(Object object) {
        return contains(object.getClass(), object);
    }

    public boolean contains(Class<?> clazz, Object object) {
        var set = map.get(clazz);
        if (set == null) return false;
        return set.contains(new IdentityWeakReference<>(object));
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        map.keySet().forEach(clazz -> {
            sb.append("Class: ").append(clazz.getSimpleName()).append("\n");
            sb.append("Objects: ").append(this.size(clazz, true)).append("\n");
        });
        return sb.toString();
    }

    public Map<Class<?>, Integer> getSummary() {
        var result = new HashMap<Class<?>, Integer>();
        map.forEach((key, value) -> result.put(key, this.size(key, true)));
        return result;
    }

    public void add(LeakDiagTool.ValidClass type, Object object) {
        type.getClasses().stream()
                .filter(clazz -> clazz.isAssignableFrom(object.getClass()))
                .forEach(clazz -> add(clazz,object));
    }

}