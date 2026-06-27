package Utils;

import java.lang.reflect.Method;

public class Mapping {

    private Object controllerInstance;
    private Method methode;

    public Mapping(Object instance,Method methode){
        this.controllerInstance = instance;        
         this.methode  = methode;
    }

    public Object getControllerInstance() {
        return controllerInstance;
    }

    public void setControllerInstance(Object c) {
        this.controllerInstance = c;
    }

    public Method getMethode() {
        return methode;
    }

    public void setMethode(Method m) {
        this.methode = m;
    }
}
