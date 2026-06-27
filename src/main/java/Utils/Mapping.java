package Utils;

import java.lang.reflect.Method;

public class Mapping {

    private Class<?> classController;
    private Method methode;

    public Mapping(Class<?> classe,Method methode){
        this.classController = classe;        
         this.methode  = methode;
    }

    public Class<?> getClassController() {
        return classController;
    }

    public void setClassController(Class<?> c) {
        this.classController = c;
    }

    public Method getMethode() {
        return methode;
    }

    public void setMethode(Method m) {
        this.methode = m;
    }
}
