package com.vmware.common.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.*;
import java.util.Set;

public class DTOValildator {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    public static DTOValildator INSTANCE = new DTOValildator();

    private DTOValildator() {

    }

    public boolean isValid(Object entity, final String agentId) {
        ValidatorFactory validatorFactory =
            Validation.byDefaultProvider().configure().constraintValidatorFactory(new ConstraintValidatorFactory() {
                @Override
                public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
                    if (HasAgentId.class.isAssignableFrom(key)) {
                        try {
                            HasAgentId validator = (HasAgentId) key.newInstance();
                            validator.setAgentId(agentId);
                            return (T) validator;
                        } catch (InstantiationException | IllegalAccessException e) {
                            logger.error("Not able to create the instance of HasAgentId type. {}"
                                , e.getMessage(), e);
                        }
                    }
                    try {
                        return key.newInstance();
                    } catch (InstantiationException | IllegalAccessException e) {
                        logger.error(e.getMessage(), e);
                    }
                    return null;
                }

                @Override
                public void releaseInstance(ConstraintValidator<?, ?> constraintValidator) {

                }
            }).buildValidatorFactory();

        Validator validator = validatorFactory.getValidator();
        Set<ConstraintViolation<Object>> violations = validator.validate(entity);
        boolean valid = true;
        if (violations.size() > 0) {
            valid = false;
            for (ConstraintViolation<Object> violation : violations) {
                logger.error("Validation error : '{}' -> '{}' -> '{}'",
                    violation.getRootBeanClass().getName(), violation.getPropertyPath(),
                    violation.getMessage());
            }
        }
        return valid;
    }
}
