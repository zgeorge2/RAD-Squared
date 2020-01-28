package com.rad2.ctrl;

import java.util.ArrayList;
import java.util.List;

/**
 * Controllers must implement this interface and may choose to override the default implementation to provide
 * a concrete set of Classes that are its dependencies. If none are provided, no dependencies are wired in.
 */
public interface ControllerDependencyListProvider {
    default List<Class> getDependenciesList() {
        return new ArrayList<>(); // return an empty list as default
    }
}

