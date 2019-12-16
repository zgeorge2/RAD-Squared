package com.vmware.common.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CloudAccountValidator implements ConstraintValidator<ValidCloudAccount, String>,
    HasAgentId {
    private String agentId;

    @Override
    public boolean isValid(String cloudAccountId, ConstraintValidatorContext constraintValidatorContext) {
        //TODO : Get cloud Account ID and compare
        System.out.println("Cloud Account ID : " + cloudAccountId);
        return true;
    }

    @Override
    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }
}
