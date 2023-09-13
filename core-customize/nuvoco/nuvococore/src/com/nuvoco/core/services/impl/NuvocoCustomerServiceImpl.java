package com.nuvoco.core.services.impl;

import com.nuvoco.core.model.NuvocoCustomerModel;
import com.nuvoco.core.services.NuvocoCustomerService;
import de.hybris.platform.servicelayer.exceptions.ClassMismatchException;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.log4j.Logger;

public class NuvocoCustomerServiceImpl implements NuvocoCustomerService {

    private static final Logger LOG = Logger.getLogger(NuvocoCustomerServiceImpl.class);

    private UserService userService;
    /**
     * @param uid
     * @return
     */
    @Override
    public NuvocoCustomerModel getCustomerForUid(String uid) {
        NuvocoCustomerModel nuvocoCustomer = null;
        try {
            nuvocoCustomer = getUserService().getUserForUID(uid, NuvocoCustomerModel.class);
        }
        catch (final UnknownIdentifierException | ClassMismatchException e) {
            LOG.error("Failed to get Nuvoco Customer with uid : "+uid);
            LOG.error("Exception is : "+e.getMessage());
            throw new ModelNotFoundException("Failed to get Nuvoco Customer with uid : "+uid);
        }
        return nuvocoCustomer;
    }



    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
