/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.apps.ic.akka;

import akka.actor.Props;
import com.rad2.akka.common.BaseActorWithRegState;
import com.rad2.akka.common.RegistryStateDTO;
import com.rad2.apps.ic.ignite.AdviceRegistry;
import com.rad2.apps.ic.ignite.MemberRegistry;
import com.rad2.apps.ic.ignite.TermDepositRegistry;
import com.rad2.ignite.common.RegistryManager;

import java.util.function.Function;

public class Member extends BaseActorWithRegState {
    private Member(RegistryManager rm, RegistryStateDTO dto) {
        super(rm, dto);
    }

    static public Props props(RegistryManager rm, String parentKey, String name) {
        RegistryStateDTO dto = new MemberRegistry.MemberRegistryDTO(parentKey, name);
        return Props.create(Member.class, rm, dto);
    }

    private TermDepositRegistry getTDReg() {
        return this.getReg().reg(TermDepositRegistry.class);
    }

    private AdviceRegistry getAdvReg() {
        return this.getReg().reg(AdviceRegistry.class);
    }

    @Override
    public Receive createReceive() {
        return super.createReceive()
            .orElse(receiveBuilder()
                .match(SetupTermDeposit.class, this::setupTermDeposit)
                .match(SelectFromOfferedAdvice.class, this::selectFromOfferedAdvice)
                .build());
    }

    private void setupTermDeposit(SetupTermDeposit arg) {
        String nm = arg.name;
        int amount = arg.principal;
        int termInYears = arg.termInYears;
        int expectedAmount = arg.expectedAmount;
        this.add(() -> TermDeposit.props(this.getRM(), this.getRegId(), nm, amount, termInYears,
            expectedAmount), nm);
    }

    private void selectFromOfferedAdvice(SelectFromOfferedAdvice arg) {
        String memberName = this.getName();
        Function<TermDepositRegistry.DTermDepositModel, Boolean> advSelectorFunc =
            (TermDepositRegistry.DTermDepositModel td) -> {
                // Tell each TD to select from advice given
                this.context().child(td.getName()).get().tell(new TermDeposit.SelectAdvice(memberName),
                    self());
                return true;
            };
        // for each TD owned by the member, select advice from what's offered
        this.getTDReg().applyToChildrenOfParent(this.getRegId(), advSelectorFunc);
    }

    /**
     * Classes used for receive method above.
     */
    static public class SetupTermDeposit {
        // since TDs are represented by Actors, this name needs to be unique across actors
        String name; // a creative name representing the goal of this TD.
        int principal;
        int termInYears;
        int expectedAmount;

        public SetupTermDeposit(String name, int principal, int termInYears, int expectedAmount) {
            this.name = name;
            this.principal = principal;
            this.termInYears = termInYears;
            this.expectedAmount = expectedAmount;
        }
    }

    static public class SelectFromOfferedAdvice {
    }
}

