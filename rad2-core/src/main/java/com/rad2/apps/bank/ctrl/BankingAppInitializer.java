package com.rad2.apps.bank.ctrl;

import com.rad2.apps.bank.akka.BankingCentral;
import com.rad2.ctrl.ControllerDependency;
import com.rad2.ignite.common.RegistryManager;

/**
 * Used to initialize an intial set of actors needed by the Bank App
 */
public class BankingAppInitializer implements ControllerDependency {
    public BankingAppInitializer(RegistryManager rm) {
        rm.getAU().add(() -> BankingCentral.props(rm), BankingCentral.BANKING_CENTRAL_NAME);
    }
}

