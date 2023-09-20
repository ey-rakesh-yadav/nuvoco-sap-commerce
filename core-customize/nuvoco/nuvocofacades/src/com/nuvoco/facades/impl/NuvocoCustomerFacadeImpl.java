package com.nuvoco.facades.impl;

import com.nuvoco.core.model.NuvocoCustomerModel;
import com.nuvoco.core.model.NuvocoUserModel;
import com.nuvoco.core.services.NuvocoCustomerService;
import com.nuvoco.facades.NuvocoCustomerFacade;
import com.nuvoco.facades.prosdealer.data.DealerListData;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class NuvocoCustomerFacadeImpl implements NuvocoCustomerFacade {

    private static final String NOT_SCL_USER_MESSAGE = "Current user is not an Nuvoco user";

    @Autowired
    NuvocoCustomerService nuvocoCustomerService;

    @Autowired
    private Converter<B2BCustomerModel, CustomerData> dealerBasicConverter;

    @Autowired
    UserService userService;
    /**
     * @param file
     * @return
     */
    @Override
    public String setProfilePicture(MultipartFile file) {
        return nuvocoCustomerService.saveProfilePicture(file);
    }

    /**
     * @param addressData
     * @param retailerUid
     * @return
     */
    @Override
    public List<AddressData> filterAddressBookData(List<AddressData> addressData, String retailerUid) {
       return addressData.stream().filter(addr -> retailerUid!=null && retailerUid.equals(addr.getRetailerUid())).collect(Collectors.toList());

    }

    /**
     * @param filteredAddressList
     * @return
     */
    @Override
    public List<AddressData> filterAddressByLpSource(List<AddressData> filteredAddressList) {
        return nuvocoCustomerService.filterAddressByLpSource(filteredAddressList);
    }

    /**
     * @return
     */
    @Override
    public DealerListData getDealersForCurrentUser() {
        DealerListData dealerListData = new DealerListData();
        List<NuvocoCustomerModel> dealersList = new ArrayList<>();
        final UserModel currentUser = userService.getCurrentUser();

        if (currentUser instanceof NuvocoUserModel) {
            final NuvocoUserModel user = (NuvocoUserModel) currentUser;
            dealersList = nuvocoCustomerService.getDealersList(user);
        } else {
            throw new ModelNotFoundException(NOT_SCL_USER_MESSAGE);
        }

        List<CustomerData> dealerData = Optional.of(dealersList.stream()
                .map(b2BCustomer -> dealerBasicConverter
                        .convert(b2BCustomer)).collect(Collectors.toList())).get();
        dealerListData.setDealers(dealerData);
        return dealerListData;
    }
}
