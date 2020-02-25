/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.apps.ic.akka;

import akka.actor.Props;
import com.rad2.akka.common.BaseActorWithRegState;
import com.rad2.akka.common.RegistryStateDTO;
import com.rad2.apps.ic.ignite.AdviceRegistry;
import com.rad2.apps.ic.ignite.TermDepositRegistry;
import com.rad2.common.utils.PrintUtils;
import com.rad2.ignite.common.RegistryManager;

import java.util.function.Function;

public class TermDeposit extends BaseActorWithRegState {
    private TermDeposit(RegistryManager rm, RegistryStateDTO dto) {
        super(rm, dto);
    }

    static public Props props(RegistryManager rm, String parentKey, String name, int principal,
                              int termInYears, int expectedAmount) {
        RegistryStateDTO dto = new TermDepositRegistry.TermDepositRegistryDTO(parentKey, name, principal,
            termInYears, expectedAmount, 0, null);
        return Props.create(TermDeposit.class, rm, dto);
    }

    private AdviceRegistry getAdvReg() {
        return this.getReg().reg(AdviceRegistry.class);
    }

    private TermDepositRegistry getTDReg() {
        return this.getReg().reg(TermDepositRegistry.class);
    }

    @Override
    public Receive createReceive() {
        return super.createReceive()
            .orElse(receiveBuilder()
                .match(SelectAdvice.class, this::selectAdvice)
                .match(CheckAdvice.class, this::checkAdvice)
                .build());
    }

    private void selectAdvice(SelectAdvice arg) {
        final SelectAdviceResult selAdv = new SelectAdviceResult(arg.forMember, this.getName());
        // function is applied to each query result
        Function<AdviceRegistry.DAdviceModel, Boolean> advSelectorFunc =
            (AdviceRegistry.DAdviceModel adv) -> {
                selAdv.selectAdvice(adv.getName());
                return true;
            };
        // apply the query to get the selected advice for the member with the given td.
        this.getAdvReg().applyToFiltered(advSelectorFunc, "select_one_advice_for_member_with_td",
            arg.forMember, this.getName());
        PrintUtils.printToActor(selAdv.toString());
        // finally set the selected advice into the TD
        this.getTDReg().selectAdvice(this.getRegId(), selAdv.selectedAdvice);
    }

    private void checkAdvice(CheckAdvice arg) {
    }

    /**
     * Classes used for receive method above.
     */
    static public class CheckAdvice {
    }

    static public class SelectAdvice {
        String forMember;

        public SelectAdvice(String forMember) {
            this.forMember = forMember;
        }
    }

    static public class SelectAdviceResult {
        String forMember;
        String forTD;
        String selectedAdvice;

        public SelectAdviceResult(String forMember, String forTD) {
            this.forMember = forMember;
            this.forTD = forTD;
            this.selectedAdvice = null;
        }

        public void selectAdvice(String advice) {
            this.selectedAdvice = advice;
        }

        public String toString() {
            return String.format("Selected adv=%s for td=%s of %s", this.selectedAdvice, this.forTD,
                this.forMember);
        }
    }
}

