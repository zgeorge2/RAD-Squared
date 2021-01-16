/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.sb.apps.finco;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rad2.apps.finco.akka.FinCoWorker;
import com.rad2.apps.finco.ctrl.FinCoController;
import com.rad2.sb.res.BaseResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * The FinCoResource provides REST resources for use by external
 * entities. This class, thus, provides the REST API entry point into the
 * Financial Company Application. Fin is a rewrite of the Banking Application
 * using pure NSC (Nano Service Components) only
 */
@RestController
@RequestMapping("/fin")
public class FinCoResource extends BaseResource<FinCoController> {
    @PostMapping("")
    public PrintOut addFinCo(@RequestBody FinCoController.FinCoListDTO dto) {
        logger.info("POST /fin");
        this.getC().addFinCo(dto);
        return new PrintOut(String.format("Add dto: /[%s]", dto));
    }

    @GetMapping("/{fcn}/getAllBranches")
    public DeferredResult<ResponseEntity<String>> getAllBranches(@PathVariable("fcn") String fcn) {
        logger.info(String.format("GET /fin/{%s}/getAllBranches", fcn));
        return createRequest(req -> req.putArg(FinCoWorker.GetAllAccountHolders.FINCO_NAME_KEY, fcn),
                req -> getC().getAllBranches(req));
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
