package com.rad2.ctrl.deps;

public class JobRef implements IJobRef {
    private String name; // the name of the job
    private String parentKey; // made unique, typically by the local system name

    public JobRef(IJobRef ijr) {
        this(ijr.getParentKey(), ijr.getName());
    }

    public JobRef(String parentKey, String name) {
        this.parentKey = parentKey;
        this.name = name;
    }

    @Override
    public String getParentKey() {
        return this.parentKey;
    }

    @Override
    public String getName() {
        return name;
    }

}
