package com.rad2.apps.ic.ignite;

import com.rad2.akka.common.RegistryStateDTO;
import com.rad2.apps.ic.akka.TermDeposit;
import com.rad2.ignite.common.BaseModelRegistry;
import com.rad2.ignite.common.DModel;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

public class TermDepositRegistry extends BaseModelRegistry<TermDepositRegistry.DTermDepositModel> {
    @Override
    protected Class getModelClass() {
        return DTermDepositModel.class;
    }

    @Override
    public Class getParentRegistryClass() {
        return MemberRegistry.class;
    }

    private AdviceRegistry getAdvReg() {
        return this.reg(AdviceRegistry.class);
    }

    public String selectAdvice(String key, String selectedAdvice) {
        return this.apply(key, td -> td.switchSelectedAdvice(selectedAdvice));
    }

    /**
     * MODEL CLASSES & DTOs
     */
    public static class DTermDepositModel extends DModel {
        @QuerySqlField
        private int principal;
        @QuerySqlField
        private int termInYears;
        @QuerySqlField
        private int expectedAmount;
        @QuerySqlField
        private int actualAmount; // at end of term
        @QuerySqlField
        private String selectedAdvice; // the advice selected to get the expected amount

        public DTermDepositModel(TermDepositRegistryDTO dto) {
            super(dto);
            this.principal = dto.getPrincipal();
            this.expectedAmount = dto.getExpectedAmount();
            this.termInYears = dto.getTermInYears();
            this.actualAmount = dto.getActualAmount();
            this.selectedAdvice = dto.getSelectedAdvice();
        }

        @Override
        public RegistryStateDTO toRegistryStateDTO() {
            return new TermDepositRegistryDTO(this);
        }

        @Override
        public Class getActorClass() {
            return TermDeposit.class;
        }

        public int getPrincipal() {
            return principal;
        }

        public int getTermInYears() {
            return termInYears;
        }

        public int getExpectedAmount() {
            return expectedAmount;
        }

        public int getActualAmount() {
            return actualAmount;
        }

        public String getSelectedAdvice() {
            return this.selectedAdvice;
        }

        public String switchSelectedAdvice(String newAdvice) {
            this.selectedAdvice = newAdvice;
            return this.selectedAdvice;
        }
    }

    public static class TermDepositRegistryDTO extends RegistryStateDTO {
        public static final String ATTR_PRINCIPAL_KEY = "PRINCIPAL_KEY";
        public static final String ATTR_EXP_AMOUNT_KEY = "EXP_AMOUNT_KEY";
        public static final String ATTR_TERM_IN_YEARS_KEY = "TERM_IN_YEARS_KEY";
        public static final String ATTR_ACTUAL_AMOUNT_KEY = "ACTUAL_AMOUNT_KEY";
        public static final String ATTR_SELECTED_ADVICE_KEY = "SELECTED_ADVICE_KEY";

        public TermDepositRegistryDTO(String parentKey, String name, int principal, int termInYears,
                                      int expectedAmount, int actualAmount, String selectedAdvice) {
            super(TermDepositRegistry.class, parentKey, name);
            this.putAttr(ATTR_PRINCIPAL_KEY, principal);
            this.putAttr(ATTR_EXP_AMOUNT_KEY, expectedAmount);
            this.putAttr(ATTR_TERM_IN_YEARS_KEY, termInYears);
            this.putAttr(ATTR_ACTUAL_AMOUNT_KEY, actualAmount);
            this.putAttr(ATTR_SELECTED_ADVICE_KEY, selectedAdvice);
        }

        public TermDepositRegistryDTO(DTermDepositModel model) {
            super(TermDepositRegistry.class, model);
            this.putAttr(ATTR_PRINCIPAL_KEY, model.getPrincipal());
            this.putAttr(ATTR_EXP_AMOUNT_KEY, model.getExpectedAmount());
            this.putAttr(ATTR_TERM_IN_YEARS_KEY, model.getTermInYears());
            this.putAttr(ATTR_ACTUAL_AMOUNT_KEY, model.getActualAmount());
            this.putAttr(ATTR_SELECTED_ADVICE_KEY, model.getSelectedAdvice());
        }

        @Override
        public DModel toModel() {
            return new DTermDepositModel(this);
        }

        public int getPrincipal() {
            return (int) this.getAttr(ATTR_PRINCIPAL_KEY);
        }

        public int getExpectedAmount() {
            return (int) this.getAttr(ATTR_EXP_AMOUNT_KEY);
        }

        public int getTermInYears() {
            return (int) this.getAttr(ATTR_TERM_IN_YEARS_KEY);
        }

        public int getActualAmount() {
            return (int) this.getAttr(ATTR_ACTUAL_AMOUNT_KEY);
        }

        public String getSelectedAdvice() {
            return (String) this.getAttr(ATTR_SELECTED_ADVICE_KEY);
        }
    }
}
