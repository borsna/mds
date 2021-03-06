package org.datacite.mds.validation.constraints.impl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.datacite.mds.domain.Datacentre;
import org.datacite.mds.util.ValidationUtils;
import org.datacite.mds.validation.constraints.MatchSymbolPrefix;

public class MatchSymbolPrefixValidator implements ConstraintValidator<MatchSymbolPrefix, Datacentre> {
    String defaultMessage;

    public void initialize(MatchSymbolPrefix constraintAnnotation) {
        defaultMessage = constraintAnnotation.message();
    }

    public boolean isValid(Datacentre datacentre, ConstraintValidatorContext context) {
        String datacentreSymbol = datacentre.getSymbol();
        String allocatorSymbol = datacentre.getAllocator().getSymbol();
        
        ValidationUtils.addConstraintViolation(context, defaultMessage, "symbol");

        return datacentreSymbol.startsWith(allocatorSymbol + ".");
    }

}
