package com.nuvoco.core.services;

import com.nuvoco.core.model.*;
import com.nuvoco.facades.data.DealerSummaryData;
import com.nuvoco.facades.data.MonthlySalesData;

import java.util.Collection;
import java.util.List;

public interface DJPVisitService {


    List<ObjectiveModel> getAllObjective();

    List<TruckModelMasterModel> findAllTrucks();

    public NuvocoCustomerModel getProductMixForDealerSummary(String counterVisitId);

    public DealerSummaryData getDealerSummary(String counterVisitId);

    List<MonthlySalesData> getLastSixMonthSalesForDealer(String counterVisitId);

    List<RouteMasterModel> getRouteMasterList(String plannedDate, String subAreaMasterPk, List<RouteMasterModel> recommendedRoute);

    Collection<DJPRouteScoreMasterModel> getDJPRouteScores(String plannedDate,
                                                           List<DJPRouteScoreMasterModel> recommendedRoute, String district, String taluka);


    void getMonthlySalesForDealer(List<MonthlySalesData> dataList, NuvocoCustomerModel customer);
}
