package com.nuvoco.core.services.impl;

import com.nuvoco.core.constants.NuvocoCoreConstants;
import com.nuvoco.core.dao.TerritoryManagementDao;
import com.nuvoco.core.model.NuvocoCustomerModel;
import com.nuvoco.core.model.SubAreaMasterModel;
import com.nuvoco.core.services.TerritoryManagementService;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TerritoryManagementServiceImpl implements TerritoryManagementService {

    @Autowired
    UserService userService;

    @Autowired
    BaseSiteService baseSiteService;

    @Autowired
    TerritoryManagementDao territoryManagementDao;

    /**
     * @return
     */
    @Override
    public List<NuvocoCustomerModel> getRetailerListForDealer() {

        NuvocoCustomerModel customerModel=(NuvocoCustomerModel) userService.getCurrentUser();
        BaseSiteModel site = baseSiteService.getCurrentBaseSite();
        return territoryManagementDao.getRetailerListForDealer(customerModel,site);

    }

    /**
     * @param customerId
     * @return
     */
    @Override
    public List<String> getAllSubAreaForCustomer(String customerId) {
        BaseSiteModel site = baseSiteService.getCurrentBaseSite();
        return territoryManagementDao.getAllSubAreaForCustomer(userService.getUserForUID(customerId),site);

    }

    /**
     * @param searchPageData
     * @param filter
     * @return
     */
    @Override
    public SearchPageData<NuvocoCustomerModel> getDealerListForRetailerPagination(SearchPageData searchPageData, String filter) {
        NuvocoCustomerModel currentUser=(NuvocoCustomerModel) userService.getCurrentUser();
        BaseSiteModel currentSite = baseSiteService.getCurrentBaseSite();
        return territoryManagementDao.getDealerListForRetailerPagination(searchPageData,currentUser,currentSite,filter);

    }

    /**
     * @param searchPageData
     * @param networkType
     * @param isNew
     * @param filter
     * @return
     */
    @Override
    public SearchPageData<NuvocoCustomerModel> getRetailerListForDealerPagination(SearchPageData searchPageData, String networkType, boolean isNew, String filter) {
        NuvocoCustomerModel nuvocoCustomerModel=(NuvocoCustomerModel) userService.getCurrentUser();
        BaseSiteModel site = baseSiteService.getCurrentBaseSite();
        return territoryManagementDao.getRetailerListForDealerPagination(searchPageData, nuvocoCustomerModel, site, networkType, isNew, filter);

    }

    /**
     * @param searchPageData
     * @param counterType
     * @param networkType
     * @param isNew
     * @param filter
     * @param influencerType
     * @param dealerCategory
     * @return
     */
    @Override
    public SearchPageData<NuvocoCustomerModel> getCustomerByTerritoriesAndCounterType(SearchPageData searchPageData, String counterType, String networkType, boolean isNew, String filter, String influencerType, String dealerCategory) {
        return getCustomerByTerritoriesAndCounterType(searchPageData, getTerritoriesForSO(), counterType, networkType, isNew, filter, influencerType, dealerCategory);
    }

    @Override
    public SearchPageData<NuvocoCustomerModel> getCustomerByTerritoriesAndCounterType(SearchPageData searchPageData, List<SubAreaMasterModel> subAreaMaster, String counterType, String networkType, boolean isNew, String filter, String influencerType, String dealerCategory) {
        return territoryManagementDao.getCustomerByTerritoriesAndCounterType(searchPageData, subAreaMaster, counterType, networkType, isNew, filter, influencerType, dealerCategory);
    }
    /**
     * @return
     */
    @Override
    public Integer getDealerCountForRetailer() {
        NuvocoCustomerModel currentUser = (NuvocoCustomerModel) userService.getCurrentUser();
        BaseSiteModel currentSite = baseSiteService.getCurrentBaseSite();
        return territoryManagementDao.getDealerCountForRetailer(currentUser,currentSite);
    }

    /**
     * @return
     */
    @Override
    public Integer getRetailerCountForDealer() {
        NuvocoCustomerModel currentUser = (NuvocoCustomerModel) userService.getCurrentUser();
        BaseSiteModel currentSite = baseSiteService.getCurrentBaseSite();
        return territoryManagementDao.getRetailerCountForDealer(currentUser,currentSite);
    }

    /**
     * @return
     */
    @Override
    public List<NuvocoCustomerModel> getDealersForSubArea() {
        List<SubAreaMasterModel> list = new ArrayList<SubAreaMasterModel>();
        UserModel user = userService.getCurrentUser();
      /*  if(user instanceof SclUserModel) {
            list = getTerritoriesForSO();
        }*/
        if(user instanceof NuvocoCustomerModel){
            list = getTerritoriesForCustomer((NuvocoCustomerModel)user);
        }
        List<NuvocoCustomerModel> dealerList = getAllCustomerForSubArea(list).stream().filter(d -> (d.getGroups()).contains(userService.getUserGroupForUID(NuvocoCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))).collect(Collectors.toList());
        return dealerList!=null && !dealerList.isEmpty() ? dealerList : Collections.emptyList() ;

    }

    /**
     * @return
     */
    @Override
    public List<NuvocoCustomerModel> getRetailersForSubArea() {
        List<NuvocoCustomerModel> resultList = getAllCustomerForSubArea(getTerritoriesForSO()).stream().filter(r->(r.getGroups()).contains(userService.getUserGroupForUID(NuvocoCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))).collect(Collectors.toList());
        return resultList;
    }

    /**
     * @param customer
     * @return
     */
    @Override
    public List<SubAreaMasterModel> getTerritoriesForCustomer(NuvocoCustomerModel customer) {
        return territoryManagementDao.getTerritoriesForCustomer(customer);
    }

    @Override
    public List<NuvocoCustomerModel> getAllCustomerForSubArea(List<SubAreaMasterModel> subAreas) {
        List<NuvocoCustomerModel> customerList = new ArrayList<NuvocoCustomerModel>();
        if(subAreas!=null && !subAreas.isEmpty()) {
            customerList = territoryManagementDao.getAllCustomerForTerritories(subAreas);
        }
        return customerList;
    }

    /**
     * @return
     */
    @Override
    public List<SubAreaMasterModel> getTerritoriesForSO() {
        return territoryManagementDao.getTerritoriesForSO();
    }
}
