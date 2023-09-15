package com.nuvoco.core.services;

import com.nuvoco.core.model.NuvocoCustomerModel;
import de.hybris.platform.catalog.model.CatalogUnawareMediaModel;
import org.springframework.web.multipart.MultipartFile;

public interface NuvocoCustomerService {

    NuvocoCustomerModel getCustomerForUid(final String uid);

    String saveProfilePicture(MultipartFile file);

    CatalogUnawareMediaModel createMediaFromFile(final String uid, final String documentType, final MultipartFile file );
}
