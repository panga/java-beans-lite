package lite.beans;

public interface PropertyEditor {

    Object getValue();

    void setValue(Object value);

    String getAsText();

    void setAsText(String text) throws IllegalArgumentException;

    void addPropertyChangeListener(PropertyChangeListener listener);

    void removePropertyChangeListener(PropertyChangeListener listener);

}