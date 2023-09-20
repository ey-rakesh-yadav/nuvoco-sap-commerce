package com.nuvoco.core.dao;

import com.nuvoco.core.model.NuvocoCustomerModel;
import de.hybris.platform.core.model.user.AddressModel;

public interface NuvocoUserDao {

    AddressModel getAddressByPk(String pk);

    AddressModel getDealerAddressByRetailerPk(String retailerAddressPk, NuvocoCustomerModel customer);

}
