package com.rad2.sb.app;

import com.rad2.akka.common.SystemProperties;
import com.rad2.apps.adm.ctrl.AdmAppInitializer;
import com.rad2.ctrl.deps.JobRefFactory;
import com.rad2.apps.bank.ctrl.BankingAppInitializer;
import com.rad2.apps.nfv.ctrl.NFVAppInitializer;
import com.rad2.apps.nfv.ctrl.ThirdPartyInitializer;
import com.rad2.common.utils.PrintUtils;
import com.rad2.ctrl.BaseController;
import com.rad2.ctrl.ControllerDependency;
import com.rad2.ctrl.deps.UUIDGenerator;
import com.rad2.ctrl.deps.YetAnotherFakeControllerDependency;
import com.rad2.ignite.common.BaseModelRegistry;
import com.rad2.ignite.common.DModel;
import com.rad2.ignite.common.RegistryManager;
import com.rad2.sb.res.BaseResource;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import kamon.Kamon;
import kamon.influxdb.InfluxDBReporter;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextListener;

import java.util.List;

/**
 * Declare all third party objects in this Component. This includes beans that need to be created from the
 * rad2-core module.
 */
@Component
public class SBExternalComponents {
    private static final String pkgPref = "com.rad2";
    private static final String OBSERVABILITY_PLATFORM_SEND_METRICS = "observability.sendMetrics";

    /**
     * Metrics Management of this application - Zipkin, Kamino, etc.
     */
    @Bean
    public Boolean setupObservability(SystemProperties sysProps) {
        // Ensure that sending metrics to Observability Platform is turned on
        boolean shouldSentMetrics = Boolean.parseBoolean(sysProps.get(OBSERVABILITY_PLATFORM_SEND_METRICS));
        if (shouldSentMetrics) {
            PrintUtils.printToActor("*** Will send metrics to Observability Platform");
            Kamon.addReporter(new InfluxDBReporter(Kamon.config()));
        } else {
            PrintUtils.printToActor("*** Not sending Metrics Observability Platform");
        }
        return shouldSentMetrics;
    }

    @Bean
    public SystemProperties getSystemProperties(ApplicationArguments args) {
        final String nodeSpecificConfigName = args.getOptionValues("akka.conf").get(0);
        Config nodeSpecificConfig = ConfigFactory.load(nodeSpecificConfigName);
        final String commonApplicationConfigName = "application";
        Config commonApplicationConfig = ConfigFactory.load(commonApplicationConfigName);
        return new SystemProperties(nodeSpecificConfig.withFallback(commonApplicationConfig));
    }

    @Bean
    public RegistryManager getRegistryManager(SystemProperties sysProps,
                                              SBList<BaseModelRegistry<? extends DModel>> registryList) {
        return new RegistryManager(sysProps, registryList.get());
    }

    /**
     * Since Registry instances  are NOT Spring boot components and are obtained from an external module, they
     * are obtained by scanning packages as below.
     *
     * @return
     */
    @Bean
    SBList<BaseModelRegistry<? extends DModel>> getRegs() {
        List<BaseModelRegistry<? extends DModel>> regs =
            new SBComponentScanner<BaseModelRegistry<? extends DModel>>(BaseModelRegistry.class, pkgPref)
                .createInstances();
        return new SBList<>(regs);
    }

    /**
     * Since Controllers are NOT Spring boot components and are obtained from an external module, the are
     * obtained by scanning packages as below.
     *
     * @return
     */
    @Bean
    SBList<BaseController> getControllers(RegistryManager rm,
                                          SBList<? extends ControllerDependency> ctrlDepsList) {
        List<BaseController> ctrlrs = new SBComponentScanner<BaseController>(BaseController.class, pkgPref)
            .createInstances();
        // initialize controllers with rm and dependencies
        ctrlrs.forEach(c -> c.initialize(rm, ctrlDepsList.get()));
        return new SBList<>(ctrlrs);
    }

    /**
     * The SBList argument is autowired in the above bean and then used here.
     *
     * @return
     */
    @Bean
    SBMap<BaseController> getControllersMap(SBList<BaseController> controllerList) {
        return new SBMap<>(controllerList, (c) -> c.getTypePrefix());
    }

    /**
     * Since BaseResources subclasses are RestControllers, they are automatically wired into the List
     * argument.
     *
     * @return
     */
    @Bean
    SBList<BaseResource> getResources(List<BaseResource> resList, SBMap<BaseController> controllersMap) {
        // initialize resource end points with the controller having the same prefix Type
        resList.forEach(res -> res.initialize(controllersMap.get(res.getTypePrefix())));
        return new SBList<>(resList);
    }

    /**
     * All ControllerDependency implementers are autowired into this list.
     */
    @Bean
    <T extends ControllerDependency> SBList<T> getControllerDependencies(RegistryManager rm,
                                                                         List<? extends ControllerDependency>
                                                                             controllerDepBeans) {
        SBList<T> ret =
            new SBList<>(new SBComponentScanner<T>(ControllerDependency.class, pkgPref).createInstances());
        controllerDepBeans.forEach(b -> ret.add((T) b));
        return ret;
    }

    @Bean
    ControllerDependency createYetAnotherFakeControllerDependency(RegistryManager rm) {
        return new YetAnotherFakeControllerDependency(rm);
    }

    @Bean
    ControllerDependency createJobRefFactory(RegistryManager rm) {
        return new JobRefFactory(rm);
    }

    @Bean
    ControllerDependency createUUIDGenerator(RegistryManager rm) {
        return new UUIDGenerator(rm);
    }

    @Bean
    ControllerDependency createBankingAppInitializer(RegistryManager rm) {
        return new BankingAppInitializer(rm);
    }

    @Bean
    ControllerDependency createAdmAppInitializer(RegistryManager rm) {
        return new AdmAppInitializer(rm);
    }

    @Bean
    ControllerDependency createNFVAppInitializer(RegistryManager rm) {
        return new NFVAppInitializer(rm);
    }

    @Bean
    ControllerDependency createThirdPartyInitializer(RegistryManager rm) {
        return new ThirdPartyInitializer(rm);
    }

    @Bean
    public RequestContextListener requestContextListener() {
        return new RequestContextListener();
    }
}