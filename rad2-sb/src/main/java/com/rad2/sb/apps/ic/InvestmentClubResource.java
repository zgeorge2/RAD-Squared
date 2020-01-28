package com.rad2.sb.apps.ic;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rad2.apps.ic.ctrl.InvestmentClubController;
import com.rad2.sb.res.BaseResource;
import org.springframework.web.bind.annotation.*;

/**
 * The InvestmentClubResource provides REST resources for use by external entities. This class, thus, provides
 * the REST API entry point into the Investment Club Application.
 */
@RestController
@RequestMapping("/vap/ic")
public class InvestmentClubResource extends BaseResource<InvestmentClubController> {
    @RequestMapping()
    public String defaultMethod() {
        return "DEFAULT HANDLING";
    }

    @RequestMapping("*")
    public String fallbackMethod() {
        return "fallback method";
    }

    @PostMapping("/bulk")
    public PrintOut addClubsAndMembers(@RequestBody InvestmentClubController.BulkInvClubsDTO ics) {
        logger.info(String.format("POST /vap/ic/bulk"));
        this.getC().addInvestmentClubsAndMembers(ics);
        return new PrintOut(String.format("Create banks: /vap/ic/bulk"));
    }

    @PostMapping("/advice/bulk")
    public PrintOut addBulkAdvice(@RequestBody InvestmentClubController.BulkAdviceDTO advList) {
        logger.info(String.format("POST /vap/ic/advice/bulk"));
        this.getC().provideAdviceInBulk(advList);
        return new PrintOut(String.format("Create banks: /vap/ic/advice/bulk"));
    }

    @PostMapping("/{icName}/selectFromAdvice")
    public PrintOut selectFromOfferedAdvice(@PathVariable("icName") String icName) {
        logger.info(String.format("POST /vap/ic/{%s}/selectFromAdvice", icName));
        this.getC().selectFromOfferedAdvice(icName);
        return new PrintOut(String.format("Select From Advice/vap/ic/{%s}/selectFromAdvice", icName));
    }

    @PutMapping("/{icName}")
    public PrintOut addClub(@PathVariable("icName") String icName) {
        logger.info(String.format("POST /vap/ic/{%s}", icName));
        this.getC().addInvestmentClub(icName);
        return new PrintOut(String.format("Create Investment Club: /vap/ic/{%s}", icName));
    }

    @PutMapping("/{cName}/member/{mName}")
    public PrintOut addMember(@PathVariable("cName") String cName,
                              @PathVariable("mName") String mName) {
        logger.info(String.format("POST /vap/ic/{%s}/member/{%s}", cName, mName));
        this.getC().addMember(cName, mName);
        return new PrintOut(String.format("Create Member: /vap/ic/{%s}/member/{%s}", cName, mName));
    }

    /**
     * Classes below are used for Jackson based JSON->Obj-JSON transforms in some of the above resources
     */
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
}