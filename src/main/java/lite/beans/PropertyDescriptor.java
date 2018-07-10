package lite.beans;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Objects;

public class PropertyDescriptor extends FeatureDescriptor {

    boolean _constrained;
    boolean _bound;
    private Method _readMethod;
    private Method _writeMethod;
    private Class<?> _propertyEditorClass;

    public PropertyDescriptor(String propertyName, Class<?> beanClass) throws IntrospectionException {
        this(propertyName, beanClass, "get" + Introspector.initialUpperCase(propertyName),
            "set" + Introspector.initialUpperCase(propertyName));
    }

    public PropertyDescriptor(String propertyName, Method readMethod, Method writeMethod)
        throws IntrospectionException {
        setName(propertyName);
        _readMethod = readMethod;
        _writeMethod = writeMethod;
    }

    public PropertyDescriptor(String propertyName, Class<?> beanClass, String readMethodName, String writeMethodName)
        throws IntrospectionException {
        setName(propertyName);
        if (readMethodName != null) {
            setReadMethod(beanClass, readMethodName);
        }
        if (writeMethodName != null) {
            setWriteMethod(beanClass, writeMethodName);
        }
    }

    PropertyDescriptor(String propertyName) {
        setName(propertyName);
    }

    void setReadMethod(Class<?> beanClass, String getterName)
        throws IntrospectionException {
        try {
            Method readMethod = beanClass.getMethod(getterName, new Class[]{});
            setReadMethod(readMethod);
        } catch (Exception e) {
            throw new IntrospectionException();
        }
    }

    void setWriteMethod(Class<?> beanClass, String setterName)
        throws IntrospectionException {
        Method writeMethod = null;
        try {
            if (_readMethod != null) {
                writeMethod = beanClass.getMethod(setterName,
                    new Class[]{_readMethod.getReturnType()});
            } else {
                Class<?> clazz = beanClass;
                Method[] methods = null;
                while (clazz != null && writeMethod == null) {
                    methods = clazz.getDeclaredMethods();
                    for (Method method : methods) {
                        if (setterName.equals(method.getName())) {
                            if (method.getParameterTypes().length == 1) {
                                writeMethod = method;
                                break;
                            }
                        }
                    }
                    clazz = clazz.getSuperclass();
                }
            }
        } catch (Exception e) {
            throw new IntrospectionException();
        }
        if (writeMethod == null) {
            throw new IntrospectionException();
        }
        setWriteMethod(writeMethod);
    }

    public Class<?> getPropertyType() {
        Class<?> result = null;
        if (_readMethod != null) {
            result = _readMethod.getReturnType();
        } else if (_writeMethod != null) {
            Class<?>[] parameterTypes = _writeMethod.getParameterTypes();
            result = parameterTypes[0];
        }
        return result;
    }

    public Method getReadMethod() {
        return _readMethod;
    }

    public void setReadMethod(Method readMethod) {
        this._readMethod = readMethod;
    }

    public Method getWriteMethod() {
        return _writeMethod;
    }

    public void setWriteMethod(Method writeMethod) {
        this._writeMethod = writeMethod;
    }

    public Class<?> getPropertyEditorClass() {
        return _propertyEditorClass;
    }

    public void setPropertyEditorClass(Class<?> propertyEditorClass) {
        _propertyEditorClass = propertyEditorClass;
    }

    public boolean isConstrained() {
        return _constrained;
    }

    public void setConstrained(boolean constrained) {
        _constrained = constrained;
    }

    public boolean isBound() {
        return _bound;
    }

    public void setBound(boolean bound) {
        _bound = bound;
    }

    public PropertyEditor createPropertyEditor(Object bean) {
        PropertyEditor editor;
        if (_propertyEditorClass == null) {
            return null;
        }
        if (!PropertyEditor.class.isAssignableFrom(_propertyEditorClass)) {
            throw new ClassCastException();
        }
        try {
            Constructor<?> constr;
            try {
                constr = _propertyEditorClass.getConstructor(Object.class);
                editor = (PropertyEditor) constr.newInstance(bean);
            } catch (NoSuchMethodException e) {
                constr = _propertyEditorClass.getConstructor();
                editor = (PropertyEditor) constr.newInstance();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return editor;
    }

    @Override
    public boolean equals(Object object) {
        boolean result = object instanceof PropertyDescriptor;
        if (result) {
            PropertyDescriptor pd = (PropertyDescriptor) object;
            boolean gettersAreEqual = (this._readMethod == null)
                && (pd.getReadMethod() == null) || (this._readMethod != null)
                && (this._readMethod.equals(pd.getReadMethod()));
            boolean settersAreEqual = (this._writeMethod == null)
                && (pd.getWriteMethod() == null) || (this._writeMethod != null)
                && (this._writeMethod.equals(pd.getWriteMethod()));
            boolean propertyTypesAreEqual = this.getPropertyType() == pd
                .getPropertyType();
            boolean propertyEditorClassesAreEqual = this
                .getPropertyEditorClass() == pd.getPropertyEditorClass();
            boolean boundPropertyAreEqual = this.isBound() == pd.isBound();
            boolean constrainedPropertyAreEqual = this.isConstrained() == pd
                .isConstrained();
            result = gettersAreEqual && settersAreEqual
                && propertyTypesAreEqual && propertyEditorClassesAreEqual
                && boundPropertyAreEqual && constrainedPropertyAreEqual;
        }
        return result;
    }

    @Override
    public int hashCode() {
        return Objects.hash(_readMethod, _writeMethod, _propertyEditorClass, _bound, _constrained);
    }
}
