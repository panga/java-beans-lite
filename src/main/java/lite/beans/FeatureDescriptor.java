package lite.beans;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map.Entry;

public class FeatureDescriptor {

    private static final String TRANSIENT = "transient";

    private String _name;
    private String _displayName;
    private boolean _expert;
    private boolean _hidden;
    private boolean _preferred;
    private String _shortDescription;
    private Hashtable<String, Object> _table;

    static void appendTo(StringBuilder sb, String name, Object value) {
        if (value != null) {
            sb.append("; ").append(name).append("=").append(value);
        }
    }

    static void appendTo(StringBuilder sb, String name, boolean value) {
        if (value) {
            sb.append("; ").append(name);
        }
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public String getDisplayName() {
        if (_displayName == null) {
            return getName();
        }
        return _displayName;
    }

    public void setDisplayName(String displayName) {
        _displayName = displayName;
    }

    public boolean isExpert() {
        return _expert;
    }

    public void setExpert(boolean expert) {
        _expert = expert;
    }

    public boolean isHidden() {
        return _hidden;
    }

    public void setHidden(boolean hidden) {
        _hidden = hidden;
    }

    public boolean isPreferred() {
        return _preferred;
    }

    public void setPreferred(boolean preferred) {
        _preferred = preferred;
    }

    public String getShortDescription() {
        if (_shortDescription == null) {
            return getDisplayName();
        }
        return _shortDescription;
    }

    public void setShortDescription(String text) {
        _shortDescription = text;
    }

    public void setValue(String attributeName, Object value) {
        getTable().put(attributeName, value);
    }

    public Object getValue(String attributeName) {
        return (_table != null)
            ? _table.get(attributeName)
            : null;
    }

    public Enumeration<String> attributeNames() {
        return getTable().keys();
    }

    private void addTable(Hashtable<String, Object> table) {
        if ((table != null) && !table.isEmpty()) {
            getTable().putAll(table);
        }
    }

    private Hashtable<String, Object> getTable() {
        if (_table == null) {
            _table = new Hashtable<>();
        }
        return _table;
    }

    boolean isTransient() {
        Object value = getValue(TRANSIENT);
        return (value instanceof Boolean)
            ? (Boolean) value
            : false;
    }

    void setTransient(Transient annotation) {
        if ((annotation != null) && (null == getValue(TRANSIENT))) {
            setValue(TRANSIENT, annotation.value());
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getName());
        sb.append("[name=").append(_name);
        appendTo(sb, "displayName", _displayName);
        appendTo(sb, "shortDescription", _shortDescription);
        appendTo(sb, "preferred", _preferred);
        appendTo(sb, "hidden", _hidden);
        appendTo(sb, "expert", _expert);
        if ((_table != null) && !_table.isEmpty()) {
            sb.append("; values={");
            for (Entry<String, Object> entry : _table.entrySet()) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("; ");
            }
            sb.setLength(sb.length() - 2);
            sb.append("}");
        }
        appendTo(sb);
        return sb.append("]").toString();
    }

    void appendTo(StringBuilder sb) {
    }
}
