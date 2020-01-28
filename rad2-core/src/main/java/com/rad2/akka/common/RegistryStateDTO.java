package com.rad2.akka.common;

import com.rad2.ignite.common.DModel;

import java.util.HashMap;
import java.util.Map;

/**
 * Basic DTO to construct Actors that also have state stored in a corresponding registry.
 */
abstract public class RegistryStateDTO {
    public static final String ATTR_REGISTRATION_ID_KEY = "REGISTRATION_ID_KEY";
    public static final String ATTR_ACTOR_PATH_KEY = "ACTOR_PATH_KEY";
    private static final String ATTR_PARENT_KEY = "PARENT_KEY";
    private static final String ATTR_NAME_KEY = "NAME_KEY";
    private static final String ATTR_REGISTRY_CLASS_FOR_STATE_KEY = "REGISTRY_CLASS_KEY";
    private Map<String, Object> entries;

    public RegistryStateDTO(Class registryClassForState, String parentKey, String name) {
        this.entries = new HashMap<>();
        this.putAttr(ATTR_PARENT_KEY, parentKey == null ? this.getRootParentKey() : parentKey);
        this.putAttr(ATTR_NAME_KEY, name);
        this.putAttr(ATTR_REGISTRY_CLASS_FOR_STATE_KEY, registryClassForState);
    }

    public RegistryStateDTO(Class registryClassForState, DModel model) {
        this(registryClassForState, model.getParentKey(), model.getName());
        this.putAttr(ATTR_ACTOR_PATH_KEY, model.getActorPath());
    }

    abstract public <K extends DModel> K toModel();

    /**
     * construct a key using a parentKey and the name that acts as pkey for the registry entry.
     *
     * @return
     */
    public String getKey() {
        return String.format("%s/%s", getParentKey(), getName());
    }

    public final Class getRegistryClassForState() {
        return (Class) this.getAttr(ATTR_REGISTRY_CLASS_FOR_STATE_KEY);
    }

    private final String getRootParentKey() {
        return "/"; // root parent key is "/" for all registry items
    }

    public String getParentKey() {
        return (String) this.getAttr(ATTR_PARENT_KEY);
    }

    public String getName() {
        return (String) this.getAttr(ATTR_NAME_KEY);
    }

    public String getRegId() {
        return (String) this.getAttr(ATTR_REGISTRATION_ID_KEY);
    }

    public String getActorPath() {
        return (String) this.getAttr(ATTR_ACTOR_PATH_KEY);
    }

    public final RegistryStateDTO putAttr(String key, Object value) {
        this.entries.put(key, value);
        return this;
    }

    public final Object getAttr(String key) {
        return this.entries.get(key);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(String.format("REG STATE DTO[%s]", this.getClass().getSimpleName())).append("\n");
        entries.forEach((k, v) -> sb.append(String.format("\t[%s]:", k)).append(String.format("[%s]\n", v)));
        return sb.toString();
    }
}

