package com.rad2.ignite.common;

import com.rad2.akka.common.RegistryStateDTO;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

abstract public class DModel {
    @QuerySqlField(index = true)
    private String key;
    @QuerySqlField(index = true)
    private String parentKey;
    @QuerySqlField
    private String name;
    @QuerySqlField
    private String actorPath;

    public DModel(RegistryStateDTO dto) {
        this.key = dto.getKey();
        this.parentKey = dto.getParentKey();
        this.name = dto.getName();
        this.actorPath = dto.getActorPath();
    }

    /**
     * Convert the RegistryState (model) as held in Ignite Registry INTO a DTO needed for constructing this
     * RegistryState
     *
     * @return
     */
    abstract public <K extends RegistryStateDTO> K toRegistryStateDTO();

    /**
     * Some RegistryStates have associated Actors. Return the class of the actor observer if applicable.
     * Override where applicable. If the registry state has no corresponding Actor, then leave this
     * unimplemented in the child class.
     *
     * @return
     */
    protected Class getActorClass() {
        return null;
    }

    /**
     * Get the path of the Actor associated with this registry state.
     *
     * @return
     */
    public String getActorPath() {
        return this.actorPath;
    }

    /**
     * EVEN if an actor class is specified and an actorPath is available, this method helps with additional
     * filtering capability on whether to reincarnate an Actor. By default, this method returns true. However,
     * a subclass of DModel can choose to control whether a particular Model instance needs to be reincarnated
     * into an Actor, based on the state stored in the Registry.
     *
     * @return true if the Actor should be reincarnated, false otherwise. Default is true
     */
    protected boolean shouldReincarnateActor() {
        return true;
    }

    public String getKey() {
        return key;
    }

    public String getParentKey() {
        return this.parentKey;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(String.format("REG STATE[%s] [KEY:%s] [ACTOR PATH:%s]\n",
            this.getClass().getSimpleName(), this.getKey(), this.getActorPath()));
        return sb.toString();
    }
}
