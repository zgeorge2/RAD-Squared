package com.rad2.examples.gen1;

public class SimpleObject extends DObject {
    private String simplePlace;

    public SimpleObject(String name, String place, String simplePlace) {
        super(name, place);
        this.simplePlace = simplePlace;
    }

    public String getSimplePlace() {
        return simplePlace;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(String.format("SIMPLE OBJ STATE[%s] [NAME:%s] [PLACE:%s] [SIMPLE PLACE: %s]\n",
            this.getClass().getSimpleName(), this.getName(), this.getPlace(), this.getSimplePlace()));
        return sb.toString();
    }
}
