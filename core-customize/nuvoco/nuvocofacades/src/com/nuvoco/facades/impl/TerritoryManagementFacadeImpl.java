package com.nuvoco.facades.impl;

import com.nuvoco.core.model.NuvocoCustomerModel;
import com.nuvoco.core.services.TerritoryManagementService;
import com.nuvoco.facades.TerritoryManagementFacade;
import com.nuvoco.facades.data.CustomerListData;
import com.nuvoco.facades.data.RequestCustomerData;
import com.nuvoco.facades.prosdealer.data.DealerListData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TerritoryManagementFacadeImpl implements TerritoryManagementFacade {

    @Autowired
    TerritoryManagementService territoryManagementService;
    @Autowired
    Converter<AddressModel, AddressData> addressConverter;

    @Autowired
    UserService userService;

    @Autowired
    Converter<NuvocoCustomerModel,CustomerData> dealerBasicConverter;


    /**
     * @param searchPageData
     * @param filter
     * @return
     */
    @Override
    public SearchPageData<CustomerData> getDealerListForRetailerPagination(SearchPageData searchPageData, String filter) {
        final SearchPageData<CustomerData> result = new SearchPageData<>();
        SearchPageData<NuvocoCustomerModel> retailerListForDealer = territoryManagementService.getDealerListForRetailerPagination(searchPageData,filter);
        result.setPagination(retailerListForDealer.getPagination());
        result.setSorts(retailerListForDealer.getSorts());
        List<CustomerData> customerData = dealerBasicConverter.convertAll(retailerListForDealer.getResults());
        result.setResults(customerData);
        return result;
    }



    /**
     * @param searchPageData
     * @param networkType
     * @param isNew
     * @param filter
     * @return
     */
    @Override
    public SearchPageData<CustomerData> getRetailerListForDealerPagination(SearchPageData searchPageData, String networkType, boolean isNew, String filter) {
        final SearchPageData<CustomerData> result = new SearchPageData<>();
        SearchPageData<NuvocoCustomerModel> retailerListForDealer = territoryManagementService.getRetailerListForDealerPagination(searchPageData, networkType, isNew, filter);
        result.setPagination(retailerListForDealer.getPagination());
        result.setSorts(retailerListForDealer.getSorts());
        List<CustomerData> customerData = dealerBasicConverter.convertAll(retailerListForDealer.getResults());
        result.setResults(customerData);
        return result;
    }

    /**
     * @param customerData
     * @return
     */
    @Override
    public CustomerListData getCustomerForUser(RequestCustomerData customerData) {
        List<NuvocoCustomerModel> customerList = territoryManagementService.getCustomerforUser(customerData);
        List<CustomerData> allData = new ArrayList<CustomerData>();
        if(customerList!=null && !customerList.isEmpty()) {

            for(NuvocoCustomerModel source: customerList) {
                CustomerData target = new CustomerData();
                target.setUid(source.getUid());
                target.setName(source.getName());
                target.setEmail(source.getEmail());
                target.setState(source.getState());
                target.setCustomerId(source.getCustomerNo());
                target.setContactNumber(source.getMobileNumber());
                target.setDealerCategory(source.getDealerCategory()!=null?source.getDealerCategory().getCode():"");
                if(source.getCounterType()!=null) {
                    target.setPartnerType(source.getCounterType().getCode());
                }
//				if(null!=source.getProfilePicture()){
//					populateProfilePicture(source.getProfilePicture(),target);
//				}
                allData.add(target);
            }
        }
        CustomerListData dataList = new CustomerListData();
        dataList.setCustomers(allData);
        return dataList;
    }

    /**
     * @return
     */
    @Override
    public Integer getDealerCountForRetailer() {
        return territoryManagementService.getDealerCountForRetailer();
    }


    /**
     * @return
     */
    @Override
    public Integer getRetailerCountForDealer() {
        return territoryManagementService.getRetailerCountForDealer();
    }

    /**
     * @return
     */
    @Override
    public DealerListData getRetailerListForDealer() {
        DealerListData data=new DealerListData();
        List<CustomerData> customerDataList=new ArrayList<>();
        List<NuvocoCustomerModel> retailerListForDealer = territoryManagementService.getRetailerListForDealer();
        if(retailerListForDealer!=null && !retailerListForDealer.isEmpty()) {
            customerDataList = Optional.of(retailerListForDealer.stream()
                    .map(b2BCustomer -> dealerBasicConverter
                            .convert(b2BCustomer)).collect(Collectors.toList())).get();
        }
        data.setDealers(customerDataList);
        return data;
    }

    /**
     * @return
     */
    @Override
    public DealerListData getAllDealersForSubArea() {
        //New Territory Change
        List<NuvocoCustomerModel> dealerList = territoryManagementService.getDealersForSubArea();
        List<CustomerData> dealerData = new ArrayList<CustomerData>();
        if(dealerList!=null && !dealerList.isEmpty()) {
            dealerData=Optional.of(dealerList.stream()
                    .map(b2BCustomer -> dealerBasicConverter
                            .convert(b2BCustomer)).collect(Collectors.toList())).get();
        }
        DealerListData dataList = new DealerListData();
        dataList.setDealers(dealerData);
        return dataList;
    }

    /**
     * @return
     */
    @Override
    public DealerListData getAllRetailersForSubArea() {
        //New Territory Change
        List<NuvocoCustomerModel> retailerList = territoryManagementService.getRetailersForSubArea();
        List<CustomerData> retailerData = new ArrayList<CustomerData>();
        if(retailerList!=null && !retailerList.isEmpty()) {
            retailerData=Optional.of(retailerList.stream()
                    .map(b2BCustomer -> dealerBasicConverter
                            .convert(b2BCustomer)).collect(Collectors.toList())).get();
        }
        DealerListData dataList = new DealerListData();
        dataList.setDealers(retailerData);
        return dataList;
    }

    /**
     * @param customerId
     * @return
     */
    @Override
    public List<String> getAllSubAreaForCustomer(String customerId) {
        return territoryManagementService.getAllSubAreaForCustomer(customerId);
    }

    /**
     * @param subArea
     * @param dealerCode
     * @return
     */
    @Override
    public DealerListData getAllRetailersForSubAreaTOP(String subArea, String dealerCode) {
        List<NuvocoCustomerModel> retailerList = territoryManagementService.getAllRetailersForSubAreaTOP(subArea,dealerCode);
        List<CustomerData> retailerData = new ArrayList<CustomerData>();
        if(retailerList!=null && !retailerList.isEmpty()) {
            retailerData=Optional.of(retailerList.stream()
                    .map(b2BCustomer -> dealerBasicConverter
                            .convert(b2BCustomer)).collect(Collectors.toList())).get();
        }
        DealerListData dataList = new DealerListData();
        dataList.setDealers(retailerData);
        return dataList;
    }

    /**
     * @return
     */
    @Override
    public List<String> getAllStatesForSO() {
        return territoryManagementService.getAllStatesForSO();
    }
}
