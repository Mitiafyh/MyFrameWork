package Utils;

import java.util.HashMap;
import java.util.Map;

public class ModelAndView {

    private String viewName;
    private Map<String, Object> attribut = new HashMap<>();

    public ModelAndView() {
    }

    public ModelAndView(String viewname) {
        this.viewName = viewname;
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public Map<String, Object> getAttribut() {
        return attribut;
    }

    public void setAttribut(Map<String, Object> data) {
        this.attribut = data;
    }

    public void addItem(String key, Object value) {
        this.attribut.put(key, value);
    }
}
