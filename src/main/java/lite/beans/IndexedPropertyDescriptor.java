package lite.beans;

import java.lang.reflect.Method;
import java.util.Objects;

public class IndexedPropertyDescriptor extends PropertyDescriptor {

    private Class<?> indexedPropertyType;

    private Method indexedGetter;

    private Method indexedSetter;

    public IndexedPropertyDescriptor(String propertyName, Class<?> beanClass,
        String getterName, String setterName, String indexedGetterName,
        String indexedSetterName) throws IntrospectionException {
        super(propertyName, beanClass, getterName, setterName);
        setIndexedByName(beanClass, indexedGetterName, indexedSetterName);
    }

    public IndexedPropertyDescriptor(String propertyName, Method getter,
        Method setter, Method indexedGetter, Method indexedSetter)
        throws IntrospectionException {
        super(propertyName, getter, setter);
        if (indexedGetter != null) {
            internalSetIndexedReadMethod(indexedGetter);
            internalSetIndexedWriteMethod(indexedSetter, true);
        } else {
            internalSetIndexedWriteMethod(indexedSetter, true);
            internalSetIndexedReadMethod(indexedGetter);
        }

        if (!isCompatible()) {
            throw new IntrospectionException();
        }
    }

    public IndexedPropertyDescriptor(String propertyName, Class<?> beanClass)
        throws IntrospectionException {
        super(propertyName, beanClass);
        setIndexedByName(beanClass, "get"
            .concat(Introspector.initialUpperCase(propertyName)), "set"
            .concat(Introspector.initialUpperCase(propertyName)));
    }

    private void setIndexedByName(Class<?> beanClass, String indexedGetterName,
        String indexedSetterName) throws IntrospectionException {

        String theIndexedGetterName = indexedGetterName;
        if (theIndexedGetterName == null) {
            if (indexedSetterName != null) {
                setIndexedWriteMethod(beanClass, indexedSetterName);
            }
        } else {
            if (theIndexedGetterName.length() == 0) {
                theIndexedGetterName = "get" + getName();
            }
            setIndexedReadMethod(beanClass, theIndexedGetterName);
            if (indexedSetterName != null) {
                setIndexedWriteMethod(beanClass, indexedSetterName,
                    indexedPropertyType);
            }
        }

        if (!isCompatible()) {
            throw new IntrospectionException();
        }
    }

    private boolean isCompatible() {
        Class<?> propertyType = getPropertyType();

        if (propertyType == null) {
            return true;
        }
        Class<?> componentTypeOfProperty = propertyType.getComponentType();
        if (componentTypeOfProperty == null) {
            return false;
        }
        if (indexedPropertyType == null) {
            return false;
        }

        return componentTypeOfProperty.getName().equals(
            indexedPropertyType.getName());
    }

    public Method getIndexedWriteMethod() {
        return indexedSetter;
    }

    public void setIndexedWriteMethod(Method indexedSetter)
        throws IntrospectionException {
        this.internalSetIndexedWriteMethod(indexedSetter, false);
    }

    public Method getIndexedReadMethod() {
        return indexedGetter;
    }

    public void setIndexedReadMethod(Method indexedGetter)
        throws IntrospectionException {
        this.internalSetIndexedReadMethod(indexedGetter);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof IndexedPropertyDescriptor)) {
            return false;
        }

        IndexedPropertyDescriptor other = (IndexedPropertyDescriptor) obj;

        return (super.equals(other)
            && (indexedPropertyType == null ? other.indexedPropertyType == null
            : indexedPropertyType.equals(other.indexedPropertyType))
            && (indexedGetter == null ? other.indexedGetter == null
            : indexedGetter.equals(other.indexedGetter)) && (indexedSetter == null ? other.indexedSetter == null
            : indexedSetter.equals(other.indexedSetter)));
    }

    @Override
    public int hashCode() {
        return Objects.hash(indexedPropertyType, indexedGetter, indexedSetter);
    }

    public Class<?> getIndexedPropertyType() {
        return indexedPropertyType;
    }

    private void setIndexedReadMethod(Class<?> beanClass, String indexedGetterName)
        throws IntrospectionException {
        Method getter;
        try {
            getter = beanClass.getMethod(indexedGetterName,
                new Class[]{Integer.TYPE});
        } catch (NoSuchMethodException | SecurityException exception) {
            throw new IntrospectionException();
        }
        internalSetIndexedReadMethod(getter);
    }

    private void internalSetIndexedReadMethod(Method indexGetter)
        throws IntrospectionException {

        if (indexGetter == null) {
            if (indexedSetter == null) {
                if (getPropertyType() != null) {
                    throw new IntrospectionException();
                }
                indexedPropertyType = null;
            }
            this.indexedGetter = null;
            return;
        }

        if ((indexGetter.getParameterTypes().length != 1)
            || (indexGetter.getParameterTypes()[0] != Integer.TYPE)) {
            throw new IntrospectionException();
        }
        Class<?> indexedReadType = indexGetter.getReturnType();
        if (indexedReadType == Void.TYPE) {
            throw new IntrospectionException();
        } else if (indexedSetter != null
            && indexGetter.getReturnType() != indexedSetter
            .getParameterTypes()[1]) {
            throw new IntrospectionException();
        }

        if (this.indexedGetter == null) {
            indexedPropertyType = indexedReadType;
        } else {
            if (indexedPropertyType != indexedReadType) {
                throw new IntrospectionException();
            }
        }

        this.indexedGetter = indexGetter;
    }

    private void setIndexedWriteMethod(Class<?> beanClass, String indexedSetterName)
        throws IntrospectionException {
        Method setter = null;
        try {
            setter = beanClass.getMethod(indexedSetterName, new Class[]{
                Integer.TYPE, getPropertyType().getComponentType()});
        } catch (SecurityException | NoSuchMethodException e) {
            throw new IntrospectionException();
        }
        internalSetIndexedWriteMethod(setter, true);
    }

    private void setIndexedWriteMethod(Class<?> beanClass,
        String indexedSetterName, Class<?> argType)
        throws IntrospectionException {
        try {
            Method setter = beanClass.getMethod(indexedSetterName, new Class[]{Integer.TYPE, argType});
            internalSetIndexedWriteMethod(setter, true);
        } catch (NoSuchMethodException | SecurityException exception) {
            throw new IntrospectionException();
        }
    }

    private void internalSetIndexedWriteMethod(Method indexSetter,
        boolean initialize) throws IntrospectionException {
        if (indexSetter == null) {
            if (indexedGetter == null) {
                if (getPropertyType() != null) {
                    throw new IntrospectionException();
                }
                indexedPropertyType = null;
            }
            this.indexedSetter = null;
            return;
        }

        Class<?>[] indexedSetterArgs = indexSetter.getParameterTypes();
        if (indexedSetterArgs.length != 2) {
            throw new IntrospectionException();
        }
        if (indexedSetterArgs[0] != Integer.TYPE) {
            throw new IntrospectionException();
        }

        Class<?> indexedWriteType = indexedSetterArgs[1];
        if (initialize && indexedGetter == null) {
            indexedPropertyType = indexedWriteType;
        } else {
            if (indexedPropertyType != indexedWriteType) {
                throw new IntrospectionException();
            }
        }

        this.indexedSetter = indexSetter;
    }
}