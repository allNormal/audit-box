package org.semsys.entity;

import java.util.HashMap;
import java.util.Map;

public class ProvoClass {

    private String name;
    private Map<String, String> values = new HashMap<>();

    /**
     * getting the name of a provo class
     * @return string name
     */
    public String getName() {
        return name;
    }

    /**
     * getting all data properties in a provo class
     * @return all data properties and its key in form of a map
     */
    public Map<String, String> getValues() {
        return values;
    }

    /**
     * setting the name of a provo class
     * @param name of a provo class
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * add a data properties into a map
     * @param category data properties key
     * @param value data properties value
     */
    public void addValue(String category, String value) {
        this.values.put(category, value);
    }
}
