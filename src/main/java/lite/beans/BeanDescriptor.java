package lite.beans;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

public class BeanDescriptor extends FeatureDescriptor {

    private Reference<Class<?>> _beanClass;

    public BeanDescriptor(Class<?> beanClass) {
        setName(beanClass.getSimpleName());
        _beanClass = new WeakReference<Class<?>>(beanClass);
    }

    public Class<?> getBeanClass() {
        return _beanClass.get();
    }
}
