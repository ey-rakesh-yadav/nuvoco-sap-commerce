package com.nuvoco.core.services;

import com.nuvoco.facades.data.SSOLoginData;

public interface SSOLoginService {
    /**
     *
     * @param uid
     * @return ssoLoginData
     */
    SSOLoginData validateUser(String uid);

}
