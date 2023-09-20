package com.nuvoco.core.dao;

import de.hybris.platform.core.model.user.AddressModel;

public interface DepotOperationDao {

    AddressModel findDepotAddressByPk(String pk);
}
