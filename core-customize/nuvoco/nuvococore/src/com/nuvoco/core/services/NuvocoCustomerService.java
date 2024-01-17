package com.nuvoco.core.services;

import com.nuvoco.core.model.NuvocoCustomerModel;
import com.nuvoco.core.model.NuvocoUserModel;
import de.hybris.platform.catalog.model.CatalogUnawareMediaModel;
import de.hybris.platform.commercefacades.user.data.AddressData;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface NuvocoCustomerService {

    NuvocoCustomerModel getCustomerForUid(final String uid);

    String saveProfilePicture(MultipartFile file);

    List<AddressData> filterAddressByLpSource(List<AddressData> filteredAddressList);

    void triggerShipToPartyAddress(String addressId);

    List<NuvocoCustomerModel> getDealersList(final NuvocoUserModel user);

    CatalogUnawareMediaModel createMediaFromFile(final String uid, final String documentType, final MultipartFile file );
}
