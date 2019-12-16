package com.vmware.apps.vap.ctrl;

import com.vmware.ctrl.deps.AnotherFakeControllerDependency;
import com.vmware.ctrl.BaseController;
import com.vmware.ctrl.deps.YetAnotherFakeControllerDependency;

import java.util.ArrayList;
import java.util.List;

public class VAPController extends BaseController {
    public String greeting(String pathvar, String formvar) {
        return String.format("Hello PathVar=[%s] and FormVar=[%s]:[%s]", pathvar, formvar);
    }

    @Override
    public List<Class> getDependenciesList() {
        List<Class> ret = new ArrayList<>();
        ret.add(AnotherFakeControllerDependency.class);
        ret.add(YetAnotherFakeControllerDependency.class);
        return ret;
    }
}
