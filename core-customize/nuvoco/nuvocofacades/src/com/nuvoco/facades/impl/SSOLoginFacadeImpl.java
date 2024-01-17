package com.nuvoco.facades.impl;

import com.nuvoco.core.services.SSOLoginService;
import com.nuvoco.facades.SSOLoginFacade;
import com.nuvoco.facades.data.SSOLoginData;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import org.springframework.beans.factory.annotation.Autowired;

public class SSOLoginFacadeImpl implements SSOLoginFacade {

    @Autowired
    SSOLoginService ssoLoginService;

    /**
     * @param uid
     * @return
     * @throws UnknownIdentifierException
     */
    @Override
    public SSOLoginData verifyUser(String uid) throws UnknownIdentifierException {
        return ssoLoginService.validateUser(uid);
    }


}
