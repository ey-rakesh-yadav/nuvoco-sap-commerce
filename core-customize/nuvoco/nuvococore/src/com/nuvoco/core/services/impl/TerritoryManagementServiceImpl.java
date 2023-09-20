package com.nuvoco.core.services.impl;

import com.nuvoco.core.constants.NuvocoCoreConstants;
import com.nuvoco.core.dao.TerritoryManagementDao;
import com.nuvoco.core.enums.CounterType;
import com.nuvoco.core.model.CustDepotMasterModel;
import com.nuvoco.core.model.NuvocoCustomerModel;
import com.nuvoco.core.model.NuvocoUserModel;
import com.nuvoco.core.model.SubAreaMasterModel;
import com.nuvoco.core.services.TerritoryManagementService;
import com.nuvoco.facades.data.FilterTalukaData;
import com.nuvoco.facades.data.RequestCustomerData;
import de.hybris.platform.b2b.model.B2BCustomerModel;
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
     * @param requestCustomerData
     * @return
     */
    @Override
    public List<NuvocoCustomerModel> getCustomerforUser(RequestCustomerData requestCustomerData) {
        B2BCustomerModel currentUser=(B2BCustomerModel) userService.getCurrentUser();
        List<NuvocoCustomerModel> customerList = null;
        if(currentUser instanceof NuvocoUserModel ||
                (((NuvocoCustomerModel) currentUser).getCounterType()==null) ||
                (( !((NuvocoCustomerModel) currentUser).getCounterType().equals(CounterType.SP)))){
            List<SubAreaMasterModel> subAreaMasterList = new ArrayList<SubAreaMasterModel>();
            if(StringUtils.isNotBlank(requestCustomerData.getSubAreaMasterPk())) {
                subAreaMasterList.add(getTerritoryById(requestCustomerData.getSubAreaMasterPk()));
            }
            else{
                FilterTalukaData filterTalukaData = new FilterTalukaData();
                subAreaMasterList = getTaulkaForUser(filterTalukaData);
            }
            if(subAreaMasterList!=null && !subAreaMasterList.isEmpty()) {
                customerList = territoryManagementDao.getCustomerForUser(requestCustomerData, subAreaMasterList);
                return customerList;
            }
        }
        else{
            customerList=territoryManagementDao.getDealersForSP(requestCustomerData);
            return customerList;
        }
        return Collections.emptyList();
    }

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
    public List<SubAreaMasterModel> getTerritoriesForCustomer(String customerId) {
        return getTerritoriesForCustomer((NuvocoCustomerModel)userService.getUserForUID(customerId));
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
     * @param customer
     * @return
     */
    @Override
    public CustDepotMasterModel getCustDepotForCustomer(NuvocoCustomerModel customer) {
        return territoryManagementDao.getCustDepotForCustomer(customer);
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
     * @param customer
     * @return
     */
    @Override
    public NuvocoUserModel getSOforCustomer(NuvocoCustomerModel customer) {
        return territoryManagementDao.getSOForSubArea(customer);
    }

    /**
     * @return
     */
    @Override
    public List<NuvocoCustomerModel> getDealersForSubArea() {
        List<SubAreaMasterModel> list = new ArrayList<SubAreaMasterModel>();
        UserModel user = userService.getCurrentUser();
       if(user instanceof NuvocoUserModel) {
            list = getTerritoriesForSO();
        }
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

    /**
     * @param uid
     * @return
     */
    @Override
    public List<SubAreaMasterModel> getTerritoriesForSO(String uid) {
        return territoryManagementDao.getTerritoriesForSO((NuvocoUserModel) userService.getUserForUID(uid));
    }

    /**
     * @param subArea
     * @param dealerCode
     * @return
     */
    @Override
    public List<NuvocoCustomerModel> getAllRetailersForSubAreaTOP(String subArea, String dealerCode) {
        BaseSiteModel site = baseSiteService.getCurrentBaseSite();
        B2BCustomerModel currentUser=(B2BCustomerModel) userService.getCurrentUser();
        List<SubAreaMasterModel> subAreaMasterList  = new ArrayList<SubAreaMasterModel>();
        if(StringUtils.isBlank(subArea)) {
            if(currentUser instanceof NuvocoUserModel) {
                FilterTalukaData filterTalukaData = new FilterTalukaData();
                subAreaMasterList  =  getTaulkaForUser(filterTalukaData);
            }
            else if(currentUser instanceof NuvocoCustomerModel){
                NuvocoUserModel userModel = getSOforCustomer((NuvocoCustomerModel) currentUser);
                subAreaMasterList  =  getTerritoriesForSO(userModel.getUid());
            }
        }
        else {
            subAreaMasterList.add(territoryManagementDao.getTerritoryById(subArea));
        }

        List<NuvocoCustomerModel> resultList = territoryManagementDao.getAllRetailersForSubAreaTOP(subAreaMasterList, site,dealerCode).stream().filter(r->(r.getGroups()).contains(userService.getUserGroupForUID(NuvocoCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))).collect(Collectors.toList());
        return resultList;
    }

    /**
     * @return
     */
    @Override
    public List<String> getAllStatesForSO() {
        B2BCustomerModel customer=(B2BCustomerModel) userService.getCurrentUser();
        BaseSiteModel site = baseSiteService.getCurrentBaseSite();
        return territoryManagementDao.getAllStatesForSO(customer,site);
    }

    /**
     * @param territoryId
     * @return
     */
    @Override
    public SubAreaMasterModel getTerritoryById(String territoryId) {
        return territoryManagementDao.getTerritoryById(territoryId);
    }


    public List<SubAreaMasterModel> getTaulkaForUser(FilterTalukaData filterTalukaData) {
        B2BCustomerModel currentUser=(B2BCustomerModel) userService.getCurrentUser();
        if(currentUser instanceof NuvocoUserModel) {
            return territoryManagementDao.getTalukaForUser(filterTalukaData);
        } /*else if ((((NuvocoCustomerModel) currentUser).getCounterType()!=null) &&
                (((NuvocoCustomerModel) currentUser).getCounterType().equals(CounterType.SP))) {
            return territoryManagementDao.getTalukaForSP(filterTalukaData);

        }*/ else {
            return getTerritoriesForCustomer((NuvocoCustomerModel)currentUser);
        }
    }
}
