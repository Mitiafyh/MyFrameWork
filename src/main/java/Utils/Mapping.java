package Utils;

import java.lang.reflect.Method;

public class Mapping {

    private Class<?> controllerInstance;
    private Method methode;

    public Mapping(Class<?> instance, Method methode) {
        this.controllerInstance = instance;
        this.methode = methode;
    }

    public Class<?> getControllerInstance() {
        return controllerInstance;
    }

    public void setControllerInstance(Class<?> c) {
        this.controllerInstance = c;
    }

    public Method getMethode() {
        return methode;
    }

    public void setMethode(Method m) {
        this.methode = m;
    }
}
