package com.nuvoco.facades;

import com.nuvoco.facades.data.SSOLoginData;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;

public interface SSOLoginFacade {
    /**
     *
     * @param uid
     * @return
     * @throws UnknownIdentifierException
     */
    SSOLoginData verifyUser(String uid) throws UnknownIdentifierException;

}
