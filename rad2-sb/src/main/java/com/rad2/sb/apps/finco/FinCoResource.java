/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.sb.apps.finco;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rad2.apps.finco.akka.FCAccountWorker;
import com.rad2.apps.finco.akka.FCData;
import com.rad2.apps.finco.akka.FinCoWorker;
import com.rad2.apps.finco.ctrl.FinCoController;
import com.rad2.sb.res.BaseResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * The FinCoResource provides REST resources for use by external entities. This
 * class, thus, provides the REST API entry point into the Financial Company
 * Application. Fin is a rewrite of the Banking Application using pure NSC (Nano
 * Service Components) only
 */
@RestController
@RequestMapping("/fin")
public class FinCoResource extends BaseResource<FinCoController> {
    @PostMapping("")
    public PrintOut addFinCo(@RequestBody FCData.FinCoList dto) {
        logger.info("POST /fin");
        this.getC().addFinCoList(dto);
        return new PrintOut(String.format("Add dto: [%s]", dto));
    }

    @GetMapping("/{fcn}/getAllBranches")
    public DeferredResult<ResponseEntity<String>> getAllBranches(@PathVariable("fcn") String fcn) {
        logger.info(String.format("GET /fin/{%s}/getAllBranches", fcn));
        return createRequest(req -> req.putArg(FinCoWorker.GetAllAccountHolders.FINCO_NAME_KEY, fcn),
                req -> getC().getAllBranches(req));
    }

    @GetMapping("/{fcn}/getAllAccountHolders")
    public DeferredResult<ResponseEntity<String>> getAllAccountHolders(@PathVariable("fcn") String fcn) {
        logger.info(String.format("GET /fin/{%s}/getAllAccountHolders", fcn));
        return createRequest(req -> req.putArg(FinCoWorker.GetAllAccountHolders.FINCO_NAME_KEY, fcn),
                req -> getC().getAllAccountHolders(req));
    }

    @GetMapping("/{fcn}/getAllAccounts")
    public DeferredResult<ResponseEntity<String>> getAllAccounts(@PathVariable("fcn") String fcn) {
        logger.info(String.format("GET /fin/{%s}/getAllAccounts", fcn));
        return createRequest(req -> req.putArg(FinCoWorker.GetAllAccounts.FINCO_NAME_KEY, fcn),
                req -> getC().getAllAccounts(req));
    }

    @PostMapping("/doTransfers")
    public DeferredResult<ResponseEntity<String>> doTransfers(@RequestBody FCData.FCTransfers dto) {
        logger.info("POST /fin/doTransfers");
        return createRequest(req -> req.putArg(FCAccountWorker.DoTransfers.FC_TRANSFERS_KEY, dto),
                req -> getC().doTransfers(req));
    }

    @PostMapping("/addAccountHolders")
    public DeferredResult<ResponseEntity<String>> addAccountHolders(@RequestBody FCData.FinCoByIdList dto) {
        logger.info("POST /fin/addAccountHolders");
        return createRequest(req -> req.putArg(FinCoWorker.AddAccountHolders.FC_BY_ID_LIST_KEY, dto),
                req -> getC().addAccountHolders(req));
    }

    @PostMapping("/{fcn}/accrueInterest")
    public DeferredResult<ResponseEntity<String>> accrueInterest(@PathVariable("fcn") String fcn) {
        logger.info(String.format("GET /fin/{%s}/accrueInterest", fcn));
        return createRequest(req -> req.putArg(FCAccountWorker.AccrueInterest.FINCO_NAME_KEY, fcn),
                req -> getC().accrueInterest(req));
    }

    /**
     * Helper class for a generic return value for a request
     */
    public static class PrintOut {
        private final String value;

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
