package com.vmware.apps.ic.akka;

import akka.actor.ActorRef;
import akka.actor.Props;
import com.vmware.akka.common.BaseActorWithRegState;
import com.vmware.akka.common.RegistryStateDTO;
import com.vmware.apps.ic.ignite.AdviceRegistry;
import com.vmware.apps.ic.ignite.InvestmentClubRegistry;
import com.vmware.apps.ic.ignite.MemberRegistry;
import com.vmware.common.serialization.IAkkaSerializable;
import com.vmware.common.utils.PrintUtils;
import com.vmware.ignite.common.RegistryManager;

import java.util.ArrayList;
import java.util.List;

public class InvestmentClub extends BaseActorWithRegState {
    private InvestmentClub(RegistryManager rm, RegistryStateDTO dto) {
        super(rm, dto);
    }

    static public Props props(RegistryManager rm, String parentKey, String name) {
        RegistryStateDTO dto = new InvestmentClubRegistry.InvestmentClubRegistryDTO(parentKey, name);
        return Props.create(InvestmentClub.class, rm, dto);
    }

    private AdviceRegistry getAdvReg() {
        return this.getReg().reg(AdviceRegistry.class);
    }

    @Override
    public Receive createReceive() {
        return super.createReceive()
            .orElse(receiveBuilder()
                .match(CreateMember.class, this::createMember)
                .match(ComputeScores.class, this::computeScores)
                .match(SetupTermDepositForMember.class, this::setupTermDepositForMember)
                .match(SelectAdviceForEachMember.class, this::selectAdviceForEachMember)
                .match(ProvideAdvice.class, this::provideAdvice)
                .build());
    }

    private void createMember(CreateMember arg) {
        String nm = arg.name;
        this.add(() -> Member.props(this.getRM(), this.getRegId(), nm), nm);
    }

    private void computeScores(ComputeScores arg) {
    }

    private void setupTermDepositForMember(SetupTermDepositForMember arg) {
        ActorRef mem = this.context().child(arg.member).get();
        mem.tell(arg, self());
    }

    private void selectAdviceForEachMember(SelectAdviceForEachMember arg) {
        PrintUtils.printToActor("Message:SelectAdviceForEachMember from system:[%s]", arg.fromSystem);
        getAllMemberNames().forEach(mem -> {
            this.context().child(mem).get().tell(new Member.SelectFromOfferedAdvice(), self());
        });
    }

    private List<String> getAllMemberNames() {
        MemberRegistry reg = reg(MemberRegistry.class);
        List<String> ret = new ArrayList<>();
        reg.applyToChildrenOfParent(this.getRegId(), m -> ret.add(m.getName()));
        return ret;
    }

    private String provideAdvice(ProvideAdvice arg) {
        String advName = "ADV_" + this.getAdvReg().generateNewId();
        return this.getAdvReg().add(new AdviceRegistry.AdviceRegistryDTO(this.getRegId(), advName,
            arg.fromMember, arg.toMember, arg.termDeposit, arg.adviceDetails));
    }

    /**
     * Classes used for receive method above.
     */
    static public class CreateMember {
        String name;

        public CreateMember(String name) {
            this.name = name;
        }
    }

    static public class SetupTermDepositForMember extends Member.SetupTermDeposit {
        String member;

        public SetupTermDepositForMember(String name, int principal, int termInYears, int expectedAmount,
                                         String member) {
            super(name, principal, termInYears, expectedAmount);
            this.member = member;
        }
    }

    static public class ProvideAdvice {
        String fromMember; // provider of the advice
        String toMember; // the member who owns the TD for which advice is being given
        String termDeposit; // the deposit that will receive the advice
        String adviceDetails; // the plan - just a string for now

        public ProvideAdvice(String fromMember, String toMember, String termDeposit, String adviceDetails) {
            this.fromMember = fromMember;
            this.toMember = toMember;
            this.termDeposit = termDeposit;
            this.adviceDetails = adviceDetails;
        }
    }

    static public class SelectAdviceForEachMember implements IAkkaSerializable {
        String fromSystem; // the system from which this message was received

        public SelectAdviceForEachMember() {
            this("UNKNOWN_SENDER");
        }

        public SelectAdviceForEachMember(String fromSystem) {
            this.fromSystem = fromSystem;
        }
    }

    static public class ComputeScores {
    }
}

