package com.nuvoco.core.dao;

import com.nuvoco.core.model.*;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.user.UserModel;

import java.util.Date;
import java.util.List;

public interface DJPVisitDao {



    ObjectiveModel findOjectiveById(String objectiveId);

    List<DJPRouteScoreMasterModel> findAllRouteForPlannedDate(DJPRunMasterModel djpRun);
    Integer getVisitCountMTD(NuvocoCustomerModel nuvocoCustomer, int month, int year);

    List<List<Object>> getCounterSharesForDealerOrRetailer(String userId, int month, int year);

    Double getDealerOutstandingAmount(String customer);

    Double getDealerCreditLimit(String customer);

    Date getLastLiftingDateForDealer(String customerNo, BaseSiteModel brand);

    List<List<Double>> getOutstandingBucketsForDealer(String customer);

    List<VisitMasterModel> getPlannedVisitForToday(UserModel user, String plannedDate);


    Double getLastLiftingQuantityForDealer(String customerNo, BaseSiteModel brand, Date maxDate);
}


