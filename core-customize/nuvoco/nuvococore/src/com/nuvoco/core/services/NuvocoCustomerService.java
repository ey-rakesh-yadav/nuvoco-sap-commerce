package com.nuvoco.core.services;

import com.nuvoco.core.model.NuvocoCustomerModel;

public interface NuvocoCustomerService {

    NuvocoCustomerModel getCustomerForUid(final String uid);
}
