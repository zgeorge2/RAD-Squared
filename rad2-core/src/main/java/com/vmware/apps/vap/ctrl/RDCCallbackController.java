package com.vmware.apps.vap.ctrl;

import com.vmware.vap.service.control.VapActorType;

public class RDCCallbackController extends BaseVAPController {

    @Override
    protected VapActorType getActorType() {
        return VapActorType.FALLBACK;
    }

}
