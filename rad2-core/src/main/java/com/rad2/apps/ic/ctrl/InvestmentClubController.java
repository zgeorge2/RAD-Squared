package com.rad2.apps.ic.ctrl;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rad2.apps.ic.akka.InvestmentClub;
import com.rad2.ctrl.BaseController;

import java.util.ArrayList;
import java.util.List;

/**
 * The InvestmentClubController acts as the central brain of the application, it receives instructions from
 * the InvestmentClubResource and uses the AkkaActorSystemUtility and the Apache Ignite Registry to perform
 * those instructions. The Akka Actor System performs the asynchronous, distributed, concurrent programming.
 * All shared state across Actors are held within the Ignite registry and is available to all Actors on any
 * Node in the cluster.
 */
public class InvestmentClubController extends BaseController {
    public void addInvestmentClubsAndMembers(BulkInvClubsDTO dto) {
        dto.getIcs().forEach(ic -> {
            this.addInvestmentClub(ic.getName());
            ic.getMembers().forEach(mName -> {
                this.addMember(ic.getName(), mName);
            });
            ic.getTermDeposits().forEach(td -> {
                this.setupTermDeposit(ic.getName(), td.getName(), td.getPrincipal(), td.getTermInYears(),
                    td.getExpectedAmount(), td.getMember());
            });
        });
    }

    public void provideAdviceInBulk(BulkAdviceDTO dto) {
        dto.getAdviceList().forEach(adv -> {
            this.provideAdvice(adv.getIcName(), adv.getFromMember(), adv.getToMember(),
                adv.getTermDeposit(), adv.getAdviceDetails());
        });
    }

    public void addInvestmentClub(String nm) {
        getAU().add(() -> InvestmentClub.props(getRM(), getAU().getLocalSystemName(), nm), nm);
    }

    public void addMember(String icName, String memberName) {
        getIC(icName).tell(new InvestmentClub.CreateMember(memberName), ActorRef.noSender());
    }

    public void provideAdvice(String icName, String fromMember, String toMember, String termDeposit,
                              String adviceDetails) {
        getIC(icName).tell(new InvestmentClub.ProvideAdvice(fromMember, toMember, termDeposit,
            adviceDetails), ActorRef.noSender());
    }

    public void selectFromOfferedAdvice(String icName) {
        getIC(icName).tell(new InvestmentClub.SelectAdviceForEachMember(getAU().getLocalSystemName()),
            ActorRef.noSender());
    }

    private void setupTermDeposit(String icName, String tdName, int pr, int yrs, int exp, String mem) {
        getIC(icName).tell(new InvestmentClub.SetupTermDepositForMember(tdName, pr, yrs, exp, mem),
            ActorRef.noSender());
    }

    private ActorSelection getIC(String icName) {
        return getAU().getActorNamed(icName);
    }

    /**
     * DTO classes for controller methods
     */
    public static class BulkInvClubsDTO {
        private List<InvClubDTO> ics;

        public BulkInvClubsDTO() {
            this.ics = new ArrayList<>();
        }

        @JsonProperty
        public List<InvClubDTO> getIcs() {
            return ics;
        }

        public static class InvClubDTO {
            private String name;
            private List<String> members;
            private List<TermDepDTO> termDeposits;

            public InvClubDTO() {
                this.members = new ArrayList<>();
                this.termDeposits = new ArrayList<>();
            }

            @JsonProperty
            public List<TermDepDTO> getTermDeposits() {
                return termDeposits;
            }

            @JsonProperty
            public String getName() {
                return name;
            }

            @JsonProperty
            public List<String> getMembers() {
                return members;
            }
        }

        public static class TermDepDTO {
            private String name; // must be unique across all TDs
            private int principal;
            private int termInYears;
            private int expectedAmount;
            private String member;

            @JsonProperty
            public String getName() {
                return name;
            }

            @JsonProperty
            public int getPrincipal() {
                return principal;
            }

            @JsonProperty
            public int getTermInYears() {
                return termInYears;
            }

            @JsonProperty
            public int getExpectedAmount() {
                return expectedAmount;
            }

            @JsonProperty
            public String getMember() {
                return member;
            }
        }
    }

    public static class BulkAdviceDTO {
        private List<ProvideAdviceDTO> adviceList;

        public BulkAdviceDTO() {
            this.adviceList = new ArrayList<>();
        }

        @JsonProperty
        public List<ProvideAdviceDTO> getAdviceList() {
            return adviceList;
        }

        public static class ProvideAdviceDTO {
            private String icName;
            private String fromMember;
            private String toMember;
            private String termDeposit;
            private String adviceDetails;

            @JsonProperty
            public String getIcName() {
                return icName;
            }

            @JsonProperty
            public String getFromMember() {
                return fromMember;
            }

            @JsonProperty
            public String getToMember() {
                return toMember;
            }

            @JsonProperty
            public String getTermDeposit() {
                return termDeposit;
            }

            @JsonProperty
            public String getAdviceDetails() {
                return adviceDetails;
            }
        }
    }
}
