package com.rad2.sb.res;

import com.rad2.common.utils.PrintUtils;
import com.rad2.ctrl.BaseController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BaseResource<K extends BaseController> {
    protected static final Logger logger = LoggerFactory.getLogger(BaseResource.class);
    private static final Pattern classNamePattern = Pattern.compile("^([a-zA-Z0-9]+)(Resource)");
    private K controller;

    public BaseResource() {
        PrintUtils.printToActor("*** Creating  instance of %s ***", this.getClass());
    }

    public void initialize(K controller) {
        PrintUtils.printToActor("*** Initializing [%s] with controller [%s]  ***", this.getClass(),
            controller);
        this.controller = controller;
    }

    protected K getC() {
        return controller;
    }

    public final String getTypePrefix() {
        Matcher m = classNamePattern.matcher(this.getClass().getSimpleName());
        return m.find() ? m.group(1) : null;
    }
}
