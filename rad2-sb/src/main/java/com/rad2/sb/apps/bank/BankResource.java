package com.rad2.sb.apps.bank;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rad2.apps.bank.ctrl.BankController;
import com.rad2.sb.res.BaseResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.Callable;

/**
 * The BankResource provides REST resources for use by external entities. This class, thus, provides the REST
 * API entry point into the Banking Application.
 */
@RestController
@RequestMapping("/vap/bank")
public class BankResource extends BaseResource<BankController> {
    @RequestMapping("*")
    public String fallbackMethod() {
        return "fallback method";
    }

    @GetMapping("/fib/{senderRewards}/{numLoops}")
    public PrintOut initiateFibonacci(@PathVariable("senderRewards") int senderRewards,
                                      @PathVariable("numLoops") int numLoops) {
        this.getC().initiateFibonacci(senderRewards, numLoops);
        return new PrintOut("Kicked off infinite Fibonacci sequence [%d] times", numLoops);
    }

    @RequestMapping(value = "/greeting/{pathvar}/{formvar}", method = RequestMethod.POST)
    public PrintOut greet(@PathVariable("pathvar") String pathvar,
                          @PathVariable("formvar") String formvar,
                          @RequestBody BankController.BanksDTO banks) {
        logger.info(String.format("GET /greeting/{%s}/{%s}", pathvar, formvar));
        return new PrintOut(this.getC().greeting(pathvar, formvar, banks));
    }

    @PostMapping("")
    public PrintOut addLocalBankAndAccountHolders(@RequestBody BankController.BanksDTO banks) {
        logger.info("POST /bank");
        this.getC().addLocalBankAndAccountHolders(banks);
        return new PrintOut(String.format("Create banks: /[%s]", banks));
    }

    @GetMapping("")
    public PrintOut printAccountStatementsOfLocalBanks() {
        logger.info("GET /bank");
        this.getC().printAccountStatementsOfLocalBanks();
        return new PrintOut("PrintAccountStatementsOfLocalBanks");
    }

    @PostMapping("printAccountStatementByCount")
    public PrintOut printAccountStatementByCount(@RequestBody BankController.PrintAccountStatementByCountDTO requiredStatements) {
        logger.info("GET /printAccountStatementByCount");
        this.getC().printAccountStatementsOfLocalBanksByCount(requiredStatements);
        return new PrintOut("printAccountStatementByCount");
    }

    @GetMapping("/{bn}")
    public PrintOut printAccountStatementOfBank(@PathVariable("bn") String bn) {
        logger.info(String.format("GET /bank/{%s}", bn));
        this.getC().printAccountStatementOfBank(bn);
        return new PrintOut(String.format("PrintAccountStatementOfBank : /[%s]", bn));
    }

    @GetMapping("/{bn}/printBankBySelection")
    public PrintOut printBankBySelection(@PathVariable("bn") String bn) {
        logger.info(String.format("GET /bank/{%s}/printBankBySelection", bn));
        this.getC().printBankBySelection(bn);
        return new PrintOut(String.format("PrintBankBySelection", bn));
    }

    @PutMapping("/{bn}/accountHolder/{ah}")
    public PrintOut addAccountHolder(@PathVariable("bn") String bn,
                                     @PathVariable("ah") String ah) {
        logger.info(String.format("PUT /bank/{%s}/accountHolder/{%s}", bn, ah));
        this.getC().addAccountHolder(bn, ah);
        return new PrintOut(String.format("Create fromAH: /[%s]/[%s]", bn, ah));
    }

    @PostMapping("/accrueInterest")
    public PrintOut accrueInterestInAllAccounts() {
        logger.info("POST /bank/accrueInterest");
        this.getC().accrueMonthlyBankInterestInLocalBanks();
        return new PrintOut("Accrue Interest in accounts");
    }

    @GetMapping("/printAccountsList")
    public PrintOut printAllAccountNamesInAllBanks() {
        logger.info("POST /printAccountsList");
        this.getC().printAllAccountNamesInAllBanks();
        return new PrintOut("Print all account names");
    }

    @GetMapping("/getAllAccountHolders")
    public Callable<ResponseEntity<PrintOut>> getAllAccountHoldersOfAllBanks() {
        logger.info("GET getAllAccountHoldersOfAllBanks");
        return () -> ResponseEntity.ok(new PrintOut(this.getC().getAllAccountHoldersOfAllBanks()));
    }

    @PostMapping("/doAdd")
    public PrintOut doAdd(@RequestBody BankController.PrintAccountStatementByCountDTO requiredStatements) {
        logger.info("POST /doAdd");
        this.getC().doAdd(requiredStatements);
        return new PrintOut("Adding a lot in all account holders");
    }

    @GetMapping("/{bn}/getAllAccountHolders")
    public PrintOut getAllAccountHoldersFromBank(@PathVariable("bn") String bn) {
        logger.info(String.format("GET /bank/{%s}/getAllAccountHoldersOfBank", bn));
        String ret = this.getC().getAllAccountHoldersFromBank(bn);
        return new PrintOut(String.format("Account Holders : %s", ret));
    }

    @PutMapping("/{bn}/accountHolder/{ah}/account/{ac}/transferTo/{toAC}/{amt}")
    public PrintOut addIntraAccountTransaction(@PathVariable("bn") String bn,
                                               @PathVariable("ah") String ah,
                                               @PathVariable("ac") String ac,
                                               @PathVariable("toAC") String toAC,
                                               @PathVariable("amt") int amt) {
        logger.info(String.format("PUT /bank/{%s}/accountHolder/{%s}/account/{%s}/transferTo/{%s}/{%s}",
            bn, ah, ac, toAC, amt));
        this.getC().intraAccountHolderMoneyTransfer(bn, ah, ac, toAC, amt);
        return new PrintOut(String.format("MT: From {/[%s]/[%s]/[%s] -> [%s] of (Rs. [%d])",
            bn, ah, ac, toAC, amt));
    }

    @PutMapping("/{bn}/accountHolder/{ah}/transferRemote/{toSN}/{toBN}/{toAH}/{amt}")
    public PrintOut addInterAccountTransaction(@PathVariable("bn") String bn,
                                               @PathVariable("ah") String ah,
                                               @PathVariable("toSN") String toSN,
                                               @PathVariable("toBN") String toBN,
                                               @PathVariable("toAH") String toAH,
                                               @PathVariable("amt") int amt) {
        logger.info(String.format("PUT /bank/{%s}/accountHolder/{%s}/transferRemote/{%s}/{%s}/{%s}/{%s}",
            bn, ah, toSN, toBN, toAH, amt));
        this.getC().interAccountHolderMoneyTransfer(bn, ah, toSN, toBN, toAH, amt);
        return new PrintOut(String.format("\"Remote MT: From /[%s]/[%s]/[NRO] -> " +
            "/[%s]/[%s]/[%s]/[NRO] of (Rs. [%d])", bn, ah, toSN, toBN, toAH, amt));
    }

    @GetMapping("/broadcast/{message}")
    public PrintOut broadcastMessage(@PathVariable("message") String message) {
        logger.info(String.format("GET /broadcast/{%s}", message));
        return new PrintOut(this.getC().broadcastMessage(message));
    }

    /**
     * Classes below are used for Jackson based JSON->Obj-JSON transforms in some of the above resources
     */
    public static class PrintOut {
        private String value;

        public PrintOut(String value) {
            this.value = value;
        }

        public PrintOut(String format, Object... args) {
            this.value = String.format(format, args);
        }

        @JsonProperty
        public String getValue() {
            return value;
        }
    }
}