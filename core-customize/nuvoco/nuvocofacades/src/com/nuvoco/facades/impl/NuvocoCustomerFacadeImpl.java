package com.nuvoco.facades.impl;

import com.nuvoco.core.services.NuvocoCustomerService;
import com.nuvoco.facades.NuvocoCustomerFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

public class NuvocoCustomerFacadeImpl implements NuvocoCustomerFacade {

    @Autowired
    NuvocoCustomerService nuvocoCustomerService;
    /**
     * @param file
     * @return
     */
    @Override
    public String setProfilePicture(MultipartFile file) {
        return nuvocoCustomerService.saveProfilePicture(file);
    }
}
