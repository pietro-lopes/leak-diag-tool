package github.uncandango.leakdiagtool.tracker;

import java.lang.ref.WeakReference;

public class IdentityWeakReference<T> extends WeakReference<T> {
    public IdentityWeakReference(T referent) {
        super(referent);
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WeakReference<?> weak) {
            return this.refersTo((T) weak.get());
        }
        return this.refersTo((T) obj);
    }

    @Override
    public int hashCode() {
        var referent = this.get();
        return referent != null ? referent.hashCode() : 0;
    }
}