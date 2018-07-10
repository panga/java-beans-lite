package lite.beans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

abstract class ChangeListenerMap<L extends EventListener> {

    private Map<String, L[]> map;

    protected abstract L[] newArray(int length);

    protected abstract L newProxy(String name, L listener);

    public final synchronized void add(String name, L listener) {
        if (this.map == null) {
            this.map = new HashMap<>();
        }
        L[] array = this.map.get(name);
        int size = (array != null)
            ? array.length
            : 0;

        L[] clone = newArray(size + 1);
        clone[size] = listener;
        if (array != null) {
            System.arraycopy(array, 0, clone, 0, size);
        }
        this.map.put(name, clone);
    }

    public final synchronized void remove(String name, L listener) {
        if (this.map != null) {
            L[] array = this.map.get(name);
            if (array != null) {
                for (int i = 0; i < array.length; i++) {
                    if (listener.equals(array[i])) {
                        int size = array.length - 1;
                        if (size > 0) {
                            L[] clone = newArray(size);
                            System.arraycopy(array, 0, clone, 0, i);
                            System.arraycopy(array, i + 1, clone, i, size - i);
                            this.map.put(name, clone);
                        } else {
                            this.map.remove(name);
                            if (this.map.isEmpty()) {
                                this.map = null;
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    public final synchronized L[] get(String name) {
        return (this.map != null)
            ? this.map.get(name)
            : null;
    }

    public final void set(String name, L[] listeners) {
        if (listeners != null) {
            if (this.map == null) {
                this.map = new HashMap<>();
            }
            this.map.put(name, listeners);
        } else if (this.map != null) {
            this.map.remove(name);
            if (this.map.isEmpty()) {
                this.map = null;
            }
        }
    }

    public final synchronized L[] getListeners() {
        if (this.map == null) {
            return newArray(0);
        }
        List<L> list = new ArrayList<>();

        L[] listeners = this.map.get(null);
        if (listeners != null) {
            for (L listener : listeners) {
                list.add(listener);
            }
        }
        for (Entry<String, L[]> entry : this.map.entrySet()) {
            String name = entry.getKey();
            if (name != null) {
                for (L listener : entry.getValue()) {
                    list.add(newProxy(name, listener));
                }
            }
        }
        return list.toArray(newArray(list.size()));
    }

    public final L[] getListeners(String name) {
        if (name != null) {
            L[] listeners = get(name);
            if (listeners != null) {
                return listeners.clone();
            }
        }
        return newArray(0);
    }

    public final synchronized boolean hasListeners(String name) {
        if (this.map == null) {
            return false;
        }
        L[] array = this.map.get(null);
        return (array != null) || ((name != null) && (null != this.map.get(name)));
    }

    public final Set<Entry<String, L[]>> getEntries() {
        return (this.map != null)
            ? this.map.entrySet()
            : Collections.<Entry<String, L[]>>emptySet();
    }

    public abstract L extract(L listener);
}
