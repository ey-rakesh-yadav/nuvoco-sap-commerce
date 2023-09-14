package com.nuvoco.core.services.impl;

import com.nuvoco.core.constants.NuvocoCoreConstants;
import com.nuvoco.core.services.SSOLoginService;
import com.nuvoco.facades.data.SSOLoginData;
import de.hybris.platform.core.model.security.PrincipalGroupModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;
import java.util.Set;

public class SSOLoginServiceImpl implements SSOLoginService {

    private static final Logger LOGGER = Logger.getLogger(SSOLoginServiceImpl.class);

    @Autowired
    UserService userService;

    /**
     * @param uid
     * @return ssoLoginData
     */
    @Override
    public SSOLoginData validateUser(String uid) {
        SSOLoginData data = new SSOLoginData();

        try {
            UserModel user = userService.getUserForUID(uid);

            if (Objects.nonNull(user)) {
                data.setIsUserPresent(Boolean.TRUE);

                Set<PrincipalGroupModel> userGroupSet = user.getGroups();

                if (userGroupSet.contains(userService.getUserGroupForUID(NuvocoCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
                    data.setUserGroup(NuvocoCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID);
                } else if (userGroupSet.contains(userService.getUserGroupForUID(NuvocoCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
                    data.setUserGroup(NuvocoCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID);
                }
            } else {
                LOGGER.info(String.format("User not found with given id %s",uid));
                throw new UnknownIdentifierException("User Not Found!");
            }

        } catch (Exception e) {
            LOGGER.debug(String.format("exception occurred in validate user", e.getMessage()));
            data.setIsUserPresent(Boolean.FALSE);
            return data;
        }
        return data;
    }


}
