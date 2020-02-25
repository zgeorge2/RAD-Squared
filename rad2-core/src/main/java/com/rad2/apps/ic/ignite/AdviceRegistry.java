/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.apps.ic.ignite;

import com.rad2.akka.common.RegistryStateDTO;
import com.rad2.ignite.common.BaseModelRegistry;
import com.rad2.ignite.common.DModel;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

/**
 * The Advice Registry relates Members to Term Deposits. Hence it is maintained in the registry hierarchy as a
 * child of the InvestmentClubRegistry.
 */
public class AdviceRegistry extends BaseModelRegistry<AdviceRegistry.DAdviceModel> {
    @Override
    protected Class getModelClass() {
        return DAdviceModel.class;
    }

    @Override
    public Class getParentRegistryClass() {
        return InvestmentClubRegistry.class;
    }

    public static class DAdviceModel extends DModel {
        @QuerySqlField
        private String fromMember;
        @QuerySqlField
        private String toMember;
        @QuerySqlField
        private String termDeposit;
        @QuerySqlField
        private String adviceDetails;

        public DAdviceModel(AdviceRegistryDTO dto) {
            super(dto);
            this.fromMember = dto.getFromMember();
            this.toMember = dto.getToMember();
            this.termDeposit = dto.getTermDeposit();
            this.adviceDetails = dto.getAdviceDetails();
        }

        @Override
        public RegistryStateDTO toRegistryStateDTO() {
            return new AdviceRegistryDTO(this);
        }

        public String getFromMember() {
            return fromMember;
        }

        public String getToMember() {
            return toMember;
        }

        public String getTermDeposit() {
            return termDeposit;
        }

        public String getAdviceDetails() {
            return adviceDetails;
        }
    }

    public static class AdviceRegistryDTO extends RegistryStateDTO {
        public static final String ATTR_OFFERED_FROM_MEMBER_KEY = "OFFERED_FROM_MEMBER_KEY";
        public static final String ATTR_OFFERED_TO_MEMBER_KEY = "OFFERED_TO_MEMBER_KEY";
        public static final String ATTR_TD_KEY = "TD_KEY";
        public static final String ATTR_ADVICE_DETAILS_KEY = "ADVICE_DETAILS_KEY ";

        public AdviceRegistryDTO(String parentKey, String name, String fromMember,
                                 String toMember, String termDeposit, String adviceDetails) {
            super(AdviceRegistry.class, parentKey, name);
            this.putAttr(ATTR_OFFERED_FROM_MEMBER_KEY, fromMember);
            this.putAttr(ATTR_OFFERED_TO_MEMBER_KEY, toMember);
            this.putAttr(ATTR_TD_KEY, termDeposit);
            this.putAttr(ATTR_ADVICE_DETAILS_KEY, adviceDetails);
        }

        public AdviceRegistryDTO(DAdviceModel model) {
            super(AdviceRegistry.class, model);
            this.putAttr(ATTR_OFFERED_FROM_MEMBER_KEY, model.getFromMember());
            this.putAttr(ATTR_OFFERED_TO_MEMBER_KEY, model.getToMember());
            this.putAttr(ATTR_TD_KEY, model.getTermDeposit());
            this.putAttr(ATTR_ADVICE_DETAILS_KEY, model.getAdviceDetails());
        }

        @Override
        public DModel toModel() {
            return new DAdviceModel(this);
        }

        public String getFromMember() {
            return (String) this.getAttr(ATTR_OFFERED_FROM_MEMBER_KEY);
        }

        public String getToMember() {
            return (String) this.getAttr(ATTR_OFFERED_TO_MEMBER_KEY);
        }

        public String getTermDeposit() {
            return (String) this.getAttr(ATTR_TD_KEY);
        }

        public String getAdviceDetails() {
            return (String) this.getAttr(ATTR_ADVICE_DETAILS_KEY);
        }
    }
}
