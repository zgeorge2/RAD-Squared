package com.rad2.ignite.common;

import com.rad2.akka.common.RegistryStateDTO;

/**
 * An instance of the implementing class needs access to other Registry's which it can get through the
 * RegistryManager.
 */
public interface UsesRegistryManager {
    /**
     * Implementor must return a RegistryManager
     *
     * @return
     */
    RegistryManager getRM();

    /**
     * Implentor uses the RegistryManager to retrieve another registry by its classname.
     *
     * @param kClass the class of the other registry held in the RegistryManager
     * @param <K>    the parameterized type.
     * @return
     */
    default <K extends BaseModelRegistry> K reg(Class kClass) {
        return (K) getRM().get(kClass);
    }


    default <K extends BaseModelRegistry> K reg(RegistryStateDTO dto) {
        return reg(dto.getRegistryClassForState());
    }
}

