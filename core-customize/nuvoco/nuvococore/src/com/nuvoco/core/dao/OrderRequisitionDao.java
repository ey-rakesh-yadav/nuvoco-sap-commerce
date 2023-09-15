package com.nuvoco.core.dao;

import com.nuvoco.core.model.NuvocoCustomerModel;

import java.util.List;

public interface OrderRequisitionDao {

    List<List<Object>> getSalsdMTDforRetailer(List<NuvocoCustomerModel> toCustomerList, String startDate, String endDate);

}
