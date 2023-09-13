package com.nuvoco.core.dao;

import com.nuvoco.core.model.DealerDriverDetailsModel;
import com.nuvoco.core.model.NuvocoCustomerModel;

import java.util.List;

public interface DealerDriverDetailsDao {

    DealerDriverDetailsModel findDriverDetailsByContactNumber(final String contactNumber);

    List<DealerDriverDetailsModel> findDriverDetailsForDealer(final NuvocoCustomerModel dealer);
}
