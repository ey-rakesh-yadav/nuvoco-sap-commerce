/*
 * Copyright (c) 2020 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.nuvoco.controllers;

import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.webservicescommons.errors.exceptions.WebserviceValidationException;
import de.hybris.platform.webservicescommons.mapping.FieldSetLevelHelper;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import javax.annotation.Resource;

public class NuvocooccController
{
	// implement the controller here


    protected static final String FULL_FIELD_SET = FieldSetLevelHelper.FULL_LEVEL;
    @Resource(name = "commerceWebServicesCartFacade2")
    protected CartFacade commerceCartFacade;


    protected void validate(final Object object, final String objectName, final Validator validator)
    {
        final Errors errors = new BeanPropertyBindingResult(object, objectName);
        validator.validate(object, errors);
        if (errors.hasErrors())
        {
            throw new WebserviceValidationException(errors);
        }
    }
}
