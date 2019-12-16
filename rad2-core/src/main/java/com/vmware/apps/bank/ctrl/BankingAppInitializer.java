package com.vmware.apps.bank.ctrl;

import com.vmware.apps.bank.akka.BankingCentral;
import com.vmware.ctrl.ControllerDependency;
import com.vmware.ignite.common.RegistryManager;

/**
 * Used to initialize an intial set of actors needed by the Bank App
 */
public class BankingAppInitializer implements ControllerDependency {
    public BankingAppInitializer(RegistryManager rm) {
        rm.getAU().add(() -> BankingCentral.props(rm), BankingCentral.BANKING_CENTRAL_NAME);
    }
}

