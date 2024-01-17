package com.nuvoco.facades;

import com.nuvoco.facades.prosdealer.data.DealerListData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface NuvocoCustomerFacade {


    String setProfilePicture(MultipartFile file);

    List<AddressData> filterAddressBookData(final List<AddressData> addressData, final String retailerUid);

    List<AddressData> filterAddressByLpSource(List<AddressData> filteredAddressList);

    DealerListData getDealersForCurrentUser();
}
