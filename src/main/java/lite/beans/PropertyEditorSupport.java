package lite.beans;

import java.util.Vector;

public class PropertyEditorSupport implements PropertyEditor {

    private Object value;
    private Object source;
    private Vector<PropertyChangeListener> listeners;

    public PropertyEditorSupport() {
        setSource(this);
    }

    public PropertyEditorSupport(Object source) {
        if (source == null) {
            throw new NullPointerException();
        }
        setSource(source);
    }

    public Object getSource() {
        return source;
    }

    public void setSource(Object source) {
        this.source = source;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
        firePropertyChange();
    }

    public String getAsText() {
        return (this.value != null)
            ? this.value.toString()
            : null;
    }

    public void setAsText(String text) throws IllegalArgumentException {
        if (value instanceof String) {
            setValue(text);
            return;
        }
        throw new IllegalArgumentException(text);
    }

    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        if (listeners == null) {
            listeners = new Vector<>();
        }
        listeners.addElement(listener);
    }

    public synchronized void removePropertyChangeListener(
        PropertyChangeListener listener) {
        if (listeners == null) {
            return;
        }
        listeners.removeElement(listener);
    }

    public void firePropertyChange() {
        Vector<PropertyChangeListener> targets;
        synchronized (this) {
            if (listeners == null) {
                return;
            }
            targets = unsafeClone(listeners);
        }

        PropertyChangeEvent evt = new PropertyChangeEvent(source, null, null, null);

        for (int i = 0; i < targets.size(); i++) {
            PropertyChangeListener target = targets.elementAt(i);
            target.propertyChange(evt);
        }
    }

    private <T> Vector<T> unsafeClone(Vector<T> v) {
        return (Vector<T>) v.clone();
    }
}