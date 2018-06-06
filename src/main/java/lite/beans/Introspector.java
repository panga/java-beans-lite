package lite.beans;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Subset of java.beans.Introspector for inspecting bean property descriptors.
 */
public final class Introspector {

    private final static PropertyDescriptor[] EMPTY_PROPERTIES = new PropertyDescriptor[0];

    private static final int DEFAULT_CAPACITY = 128;

    private static Map<String, GenericBeanInfo> BEAN_CACHE = new ConcurrentHashMap<>(DEFAULT_CAPACITY);

    /**
     * Introspect on a Java Bean and learn about all its properties, exposed methods, and events.
     * <p>
     * If the BeanInfo class for a Java Bean has been previously Introspected then the BeanInfo class is retrieved from
     * the BeanInfo cache.
     *
     * @param beanClass The bean class to be analyzed.
     * @return A BeanInfo object describing the target bean.
     * @throws IntrospectionException if an exception occurs during introspection.
     */
    public static BeanInfo getBeanInfo(Class<?> beanClass) throws IntrospectionException {
        return getBeanInfo(beanClass, null);
    }

    /**
     * Introspect on a Java bean and learn all about its properties, exposed methods, below a given "stop" point.
     * <p>
     * If the BeanInfo class for a Java Bean has been previously Introspected based on the same arguments, then the
     * BeanInfo class is retrieved from the BeanInfo cache.
     *
     * @param beanClass The bean class to be analyzed.
     * @param stopClass The baseclass at which to stop the analysis. Any methods/properties/events in the stopClass or
     * in its baseclasses will be ignored in the analysis.
     * @return A BeanInfo object describing the target bean.
     * @throws IntrospectionException if an exception occurs during introspection.
     */
    public static BeanInfo getBeanInfo(Class<?> beanClass, Class<?> stopClass) throws IntrospectionException {
        String beanInfoSignature = _beanInfoSignature(beanClass, stopClass);
        GenericBeanInfo beanInfo = BEAN_CACHE.get(beanInfoSignature);
        if (beanInfo == null) {
            beanInfo = _construct(beanClass, stopClass);
            BEAN_CACHE.put(beanInfoSignature, beanInfo);
        }
        return beanInfo;
    }

    private static String _beanInfoSignature(Class<?> beanClass, Class<?> stopClass) {
        return (beanClass != null ? beanClass.getName() : "") + "," + (stopClass != null ? stopClass.getName() : "");
    }

    /**
     * Utility method to take a string and convert it to normal Java variable name capitalization. This normally means
     * converting the first character from upper case to lower case, but in the (unusual) special case when there is
     * more than one character and both the first and second characters are upper case, we leave it alone.
     * <p>
     * Thus "FooBah" becomes "fooBah" and "X" becomes "x", but "URL" stays as "URL".
     *
     * @param name The string to be decapitalized.
     * @return The decapitalized version of the string.
     */
    public static String decapitalize(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        if (name.length() > 1 && Character.isUpperCase(name.charAt(1)) && Character.isUpperCase(name.charAt(0))) {
            return name;
        }
        char[] chars = name.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }

    private static GenericBeanInfo _construct(Class<?> beanType, Class<?> stopType) {
        Map<String, PropertyDescriptor> propsByName = new TreeMap<>();

        _introspect(beanType, stopType, propsByName);

        BeanDescriptor beanDescriptor = new BeanDescriptor(beanType);

        PropertyDescriptor[] propertyDescriptors;
        if (propsByName.isEmpty()) {
            propertyDescriptors = EMPTY_PROPERTIES;
        } else {
            propertyDescriptors = propsByName.values().toArray(new PropertyDescriptor[propsByName.size()]);
        }

        return new GenericBeanInfo(beanDescriptor, propertyDescriptors);
    }

    private static void _introspect(Class<?> currType, Class<?> stopType, Map<String, PropertyDescriptor> props) {
        if (currType == null) {
            return;
        }

        if (stopType != null && currType == stopType) {
            return;
        }

        _introspect(currType.getSuperclass(), stopType, props);

        for (Method m : currType.getDeclaredMethods()) {
            final int flags = m.getModifiers();
            if (Modifier.isStatic(flags) || m.isSynthetic() || m.isBridge()) {
                continue;
            }

            Class<?> argTypes[] = m.getParameterTypes();
            if (argTypes.length == 0) {
                if (!Modifier.isPublic(flags)) {
                    continue;
                }

                Class<?> resultType = m.getReturnType();
                if (resultType == Void.class) {
                    continue;
                }

                String name = m.getName();
                if (name.startsWith("get")) {
                    if (resultType == void.class) {
                        continue;
                    }

                    if (name.length() > 3) {
                        name = decapitalize(name.substring(3));
                        _prop(props, name).setReadMethod(m);
                    }
                } else if (name.startsWith("is")) {
                    if (resultType == void.class) {
                        continue;
                    }

                    if (name.length() > 2) {
                        name = decapitalize(name.substring(2));
                        _prop(props, name).setReadMethod(m);
                    }
                }
            } else if (argTypes.length == 1) {
                if (!Modifier.isPublic(flags)) {
                    continue;
                }

                String name = m.getName();
                if (!name.startsWith("set") || name.length() == 3) {
                    continue;
                }

                name = decapitalize(name.substring(3));
                _prop(props, name).setWriteMethod(m);
            }
        }
    }

    private static PropertyDescriptor _prop(Map<String, PropertyDescriptor> props, String name) {
        PropertyDescriptor prop = props.get(name);
        if (prop == null) {
            prop = new PropertyDescriptor(name);
            props.put(name, prop);
        }
        return prop;
    }
}
