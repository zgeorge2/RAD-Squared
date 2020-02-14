package com.rad2.ctrl.deps;

/**
 * Any reference to a particular Job in the JobTrackerRegistry is made using an IJobRef, which consists of
 * the name of the Job and its parentkey. Together they are to uniquely identify the job in the cluster.
 * Note that the usual parentKey + "/" + name represents the Jobs regId in teh registry too.
 */
public interface IJobRef {
    /**
     * the parent key to use for the job tracker in the JobTrackerRegistry
     *
     * @return
     */
    String getParentKey();

    /**
     * The to use in the JobTrackerRegistry. The combination of the parent key and name should ensure
     * uniqueness of this entry in the JobTrackerRegistry
     *
     * @return
     */
    String getName();

    default String regId() {
        return getParentKey() + "/" + this.getName();
    }
}
