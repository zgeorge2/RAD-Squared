package com.vmware.vap.service.control;

/**
 * Type of VAP Actors running in the system. Each of the supported messages type is mapped to one these actors. The
 * corresponding actor will handle the message arrived on Lemans.
 */
public enum VapActorType {
    /**
     * Master Actor
     */
    MASTER ("vap-master"),

    /**
     * Fallback actor that continue to exists till all other actors are implemented
     */
    FALLBACK ("fallback"),

    /**
     *  1. Agent Install
     *  2. Agent Uninstall
     *  3. Callback Message Handling
     *      a. Agent Install progress
     *      b. Agent Uninstall progress
     */
    BOOTSTRAP ("bootstrap"),

    /**
     * 1. Plugin Activation
     * 2. Plugin Deactivation
     * 3. Callback Message Handling
     *     a. Plugin activation messages
     *     b. Plugin deactivation messages
     *     c. Configured plugins
     */
    PLUGIN ("plugin"),

    /**
     * 1. Agent Start
     * 2. Agent Stop
     * 3. Content Upgrade in VMs
     * 4. Upgrade Agents
     * 3. Callback Message Handling
     *     a. Agent start
     *     b. Agent Stop
     *     c. Content Upgrade messages
     *     d. Upgrade Agents messages
     *     e. MEPS Health
     */
    AGENT_MANAGEMENT ("agent_management"),

    /**
     * 1. VAP OVA - Content Upgrade
     * 2. VAP OVA - Health
     */
    RDC_MANAGEMENT ("rdc_management"),

    /**
     * 1. Callback Message Handling
     *    a. Processes
     */
    PROCESSES ("processes"),

    /**
     * Actor for handling GET APIs
     */
    RETRIEVER("retriever"),

    /**
     * 1. Validate Cloud Account
     */
    CLOUD_ACCOUNT("cloud_account");

    private String actorType;

    VapActorType(String actorType) {
        this.actorType = actorType;
    }

    public String getActorType() {
        return actorType;
    }

    public String getActorName() {
        return actorType;
    }
}
