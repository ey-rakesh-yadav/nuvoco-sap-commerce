package com.nuvoco.facades;

import org.springframework.web.multipart.MultipartFile;

public interface NuvocoCustomerFacade {


    String setProfilePicture(MultipartFile file);
}
