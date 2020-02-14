package com.rad2.ctrl;

import com.rad2.akka.common.AkkaActorSystemUtility;
import com.rad2.common.utils.PrintUtils;
import com.rad2.ctrl.deps.IFakeControllerDependency;
import com.rad2.ctrl.deps.IJobRef;
import com.rad2.ctrl.deps.JobRefFactory;
import com.rad2.ctrl.deps.UUIDGenerator;
import com.rad2.ignite.common.RegistryManager;
import com.rad2.ignite.common.UsesRegistryManager;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Name subclasses using the regex patten shown in this class. e.g. FooController, BarController. This will
 * then allow for automatic wiring with the corresponding Resource Endpoint. e.g. FooResource, BarResource,
 * respectively.
 */
abstract public class BaseController implements ControllerDependencyListProvider, UsesRegistryManager {
    private static final Pattern classNamePattern = Pattern.compile("^([a-zA-Z0-9]+)(Controller)");
    private RegistryManager rm; // the Apache Ignite Registries
    private Map<String, ControllerDependency> depMap;
    private HashSet<Class<? extends ControllerDependency>> commonDependencies;

    public BaseController() {
        this.commonDependencies = this.createDependencySet();
    }

    public final String getTypePrefix() {
        Matcher m = classNamePattern.matcher(this.getClass().getSimpleName());
        return m.find() ? m.group(1) : null;
    }

    public <T extends ControllerDependency> void initialize(RegistryManager rm, List<T> deps) {
        this.rm = rm;
        this.depMap = deps.stream()
                .filter(this::isADependency)
                .collect(Collectors.toMap(d -> d.getType(), Function.identity()));
        this.depMap.entrySet().forEach(entry -> {
            PrintUtils.printToActor("*** Initializing Controller [%s] w/ Dep [%s] ***",
                    this.getClass().getSimpleName(), entry.getValue());
        });
    }

    protected AkkaActorSystemUtility getAU() {
        return this.getRM().getAU();
    }

    public RegistryManager getRM() {
        return rm;
    }

    public IJobRef createJobRef() {
        return getJobRefFactory().create();
    }

    private JobRefFactory getJobRefFactory() {
        return this.getDep(JobRefFactory.class);
    }

    private IFakeControllerDependency getFakeControllerDependency() {
        return this.getDep(IFakeControllerDependency.class);
    }

    protected final <T extends ControllerDependency> T getDep(Class depClass) {
        T ret = (T) this.depMap.get(depClass.getSimpleName()); // try by simply name, the way it was stored
        if (ret == null) { // if simple name did not work, scan for assignability
            ret = (T) this.depMap.values().stream()
                    .filter(dep -> depClass.isAssignableFrom(dep.getClass()))
                    .findFirst().get();
        }
        return ret;
    }

    /**
     * Initialize a HashSet of common dependencies and sub class specific dependencies.
     *
     * @return
     */
    private HashSet<Class<? extends ControllerDependency>> createDependencySet() {
        HashSet<Class<? extends ControllerDependency>> ret = new HashSet<>();
        ret.add(UUIDGenerator.class);
        ret.add(JobRefFactory.class);
        this.getDependenciesList().forEach(ret::add);
        return ret;
    }

    /**
     * Verify if the instance of dependency passed as the argument is indeed a dependency of this class or sub
     * class using the Dependency HashSet
     *
     * @return
     */
    private <T extends ControllerDependency> boolean isADependency(T depInstance) {
        return this.commonDependencies.stream()
                .anyMatch(clz -> clz.isAssignableFrom(depInstance.getClass()));
    }
}
