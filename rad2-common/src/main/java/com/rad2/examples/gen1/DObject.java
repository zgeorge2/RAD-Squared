package com.rad2.examples.gen1;

public class DObject {
    String name;
    String place;

    public DObject(String name, String place) {
        this.name = name;
        this.place = place;
    }

    public String getName() {
        return name;
    }

    public String getPlace() {
        return place;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(String.format("OBJ STATE[%s] [NAME:%s] [PLACE:%s]\n",
            this.getClass().getSimpleName(), this.getName(), this.getPlace()));
        return sb.toString();
    }
}
