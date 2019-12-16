package com.vmware.dw.bank;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.typesafe.config.Config;
import com.vmware.akka.common.BankController;
import com.vmware.akka.common.CoreBankDTO;
import com.vmware.akka.common.CoreBanksDTO;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The BankResource provides REST resources for use by external entities. This class, thus, provides the REST
 * API entry point into the Banking Application.
 */
@Path("/system")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMIN")
@PermitAll
public class BankResource {
    private BankController controller;

    void initialize(Config config) {
        this.controller = new BankController(config);
    }

    @POST
    @Path("/greeting/{pathvar}/{formvar}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Greeting greet(@PathParam("pathvar") String pathvar,
                          @PathParam("formvar") String formvar,
                          BanksDTO banks) {
        return new Greeting(this.controller.greeting(pathvar, formvar, banks.toCoreBanksDTO()));
    }

    @POST
    @Path("/{systemName}/bank")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public PrintOut addLocalBankAndAccountHolders(@PathParam("systemName") String systemName,
                                                  BanksDTO banks) {
        this.controller.addLocalBankAndAccountHolders(systemName, banks.toCoreBanksDTO());
        final String value = String.format("Create banks: /[%s]/[%s]", systemName, banks);
        return new PrintOut(value);
    }

    @GET
    @Path("/{systemName}/bank")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public PrintOut printAllLocalBanks(@PathParam("systemName") String systemName) {
        this.controller.printAccountStatementsOfAllBanks(systemName);
        final String value = String.format("Print statements of all local banks in system /[%s]", systemName);
        return new PrintOut(value);
    }

    @GET
    @Path("/{systemName}/bank/{bankName}")
    @Timed
    public PrintOut printLocalBank(@PathParam("systemName") String systemName,
                                   @PathParam("bankName") String bankName) {
        this.controller.printAccountStatementOfBank(systemName, bankName);
        return new PrintOut(String.format("Account Statement request sent to : /[%s]/[%s]", systemName,
            bankName));
    }

    @PUT
    @Path("/{systemName}/bank/{bankName}/accountHolder")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public PrintOut addAccountHolder(@PathParam("systemName") String systemName,
                                     @PathParam("bankName") String bankName,
                                     @FormParam("name") String name) {
        this.controller.addAccountHolder(systemName, bankName, name);
        final String value = String.format("Create accountHolder: /[%s]/[%s]/[%s]", systemName, bankName,
            name);
        return new PrintOut(value);
    }

    @POST
    @Path("/{systemName}/bank/accrueInterest")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public PrintOut accrueInterestInAllAccounts(@PathParam("systemName") String systemName) {
        this.controller.accrueInterestInAllAccounts(systemName);
        String value = String.format("Accrue Interest in accounts of: /[%s]", systemName);
        return new PrintOut(value);
    }

    @PUT
    @Path("/{systemName}/bank/{bankName}/accountHolder/{accountHolder}/transfer")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public PrintOut addIntraAccountTransaction(@PathParam("systemName") String systemName,
                                               @PathParam("bankName") String bankName,
                                               @PathParam("accountHolder") String fromAccountHolder,
                                               @FormParam("fromAccount") String fromAccount,
                                               @FormParam("toAccount") String toAccount,
                                               @FormParam("amount") int amount) {
        this.controller.intraAccountHolderMoneyTransfer(systemName, bankName,
            fromAccountHolder, fromAccount, toAccount, amount);
        final String value = String.format("MT: From {" +
                "/[%s]/[%s]/[%s]/[%s] -> [%s] of (Rs. [%d])",
            systemName, bankName, fromAccountHolder, fromAccount, toAccount, amount);
        return new PrintOut(value);
    }

    @PUT
    @Path("/{systemName}/bank/{bankName}/accountHolder/{accountHolder}/transferRemote")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public PrintOut addInterAccountTransaction(@PathParam("systemName") String systemName,
                                               @PathParam("bankName") String bankName,
                                               @PathParam("accountHolder") String accountHolder,
                                               @FormParam("toSystemName") String toSystemName,
                                               @FormParam("toBankName") String toBankName,
                                               @FormParam("toAccountHolder") String toAccountHolder,
                                               @FormParam("amount") int amount) {
        this.controller.interAccountHolderMoneyTransfer(systemName, bankName, accountHolder,
            toSystemName, toBankName, toAccountHolder, amount);
        final String value = String.format("\"Remote MT: From /[%s]/[%s]/[%s]/[NRO] -> " +
                "/[%s]/[%s]/[%s]/[NRO] of (Rs. [%d])",
            systemName, bankName, accountHolder, toSystemName, toBankName, toAccountHolder, amount);
        return new PrintOut(value);
    }

    @GET
    @Path("/{systemName}/broadcast")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public PrintOut broadcastMessage(@PathParam("systemName") String systemName,
                                     @QueryParam("message") String message) {
        return new PrintOut(this.controller.broadcastMessage(systemName, message));
    }

    /**
     * Classes below are used for Jackson based JSON->Obj-JSON transforms in some of the above resources
     */
    public static class Greeting {
        private final String content;

        public Greeting(String content) {
            this.content = content;
        }

        public String getContent() {
            return content;
        }
    }

    public static class PrintOut {
        private String value;

        public PrintOut(String value) {
            this.value = value;
        }

        @JsonProperty
        public String getValue() {
            return value;
        }
    }

    public static class BanksDTO {
        private List<BankDTO> banks;

        public BanksDTO() {
            this.banks = new ArrayList<>();
        }

        @JsonProperty
        public List<BankDTO> getBanks() {
            return banks;
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("{[");
            this.getBanks().forEach(x -> sb.append(x).append(","));
            sb.append("]}");
            return sb.toString();
        }

        public CoreBanksDTO toCoreBanksDTO() {
            return new CoreBanksDTO(getBanks().
                stream().map(x -> x.toCoreBankDTO())
                .collect(Collectors.toList()));
        }

        public static class BankDTO {
            private String name;
            private List<String> accountHolders;

            public BankDTO() {
                this.accountHolders = new ArrayList<>();
            }

            @JsonProperty
            public String getName() {
                return name;
            }

            @JsonProperty
            public List<String> getAccountHolders() {
                return accountHolders;
            }

            public String toString() {
                StringBuffer sb = new StringBuffer();
                sb.append("{name: ").append(this.getName()).append(", accountHolders:[");
                this.getAccountHolders().forEach(x -> sb.append(x).append(","));
                sb.append("]");
                return sb.toString();
            }

            public CoreBankDTO toCoreBankDTO() {
                return new CoreBankDTO(this.getName(), this.getAccountHolders());
            }
        }
    }
}