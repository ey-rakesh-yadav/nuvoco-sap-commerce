package com.nuvoco.facades.impl;

import com.nuvoco.core.constants.NuvocoCoreConstants;
import com.nuvoco.core.dao.DealerDriverDetailsDao;
import com.nuvoco.core.dao.DealerVehicleDetailsDao;
import com.nuvoco.core.model.DealerDriverDetailsModel;
import com.nuvoco.core.model.DealerVehicleDetailsModel;
import com.nuvoco.core.model.NuvocoCustomerModel;
import com.nuvoco.core.services.DealerTransitService;
import com.nuvoco.core.services.NuvocoCustomerService;
import com.nuvoco.core.services.TerritoryManagementService;
import com.nuvoco.facades.DealerFacade;
import com.nuvoco.facades.data.NuvocoAddressData;
import com.nuvoco.facades.data.NuvocoCustomerData;
import com.nuvoco.facades.data.NuvocoImageData;
import com.nuvoco.facades.data.vehicle.DealerDriverDetailsData;
import com.nuvoco.facades.data.vehicle.DealerDriverDetailsListData;
import com.nuvoco.facades.data.vehicle.DealerVehicleDetailsData;
import com.nuvoco.facades.data.vehicle.DealerVehicleDetailsListData;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.security.PrincipalGroupModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.webservicescommons.dto.error.ErrorListWsDTO;
import de.hybris.platform.webservicescommons.dto.error.ErrorWsDTO;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class DealerFacadeImpl implements DealerFacade {

    @Autowired
    UserService userService;
    @Autowired
    NuvocoCustomerService nuvocoCustomerService;
    @Autowired
    Converter<AddressModel, NuvocoAddressData> nuvocoAddressConverter;

    @Autowired
    TerritoryManagementService territoryManagementService;

    @Resource
    private DealerTransitService dealerTransitService;

    @Resource
    private Converter<DealerDriverDetailsModel, DealerDriverDetailsData> dealerDriverDetailsConverter;

    @Resource
    private Populator<DealerVehicleDetailsData,DealerVehicleDetailsModel> dealerVehicleDetailsReversePopulator;

    @Autowired
    private Populator<DealerDriverDetailsData, DealerDriverDetailsModel> dealerDriverDetailsReversePopulator;

     @Autowired
    private Converter<UserModel, CustomerData> customerConverter;
     @Autowired
     private DealerVehicleDetailsDao dealerVehicleDetailsDao;

     @Autowired
     private DealerDriverDetailsDao dealerDriverDetailsDao;

   @Resource
    private Converter<DealerVehicleDetailsModel, DealerVehicleDetailsData> dealerVehicleDetailsConverter;

   @Autowired
   private ModelService modelService;


    /**
     * @param uid
     * @return
     */
    @Override
    public NuvocoCustomerData getCustomerProfile(String uid) {
        B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();

        if(currentUser!=null && currentUser instanceof NuvocoCustomerModel)
        {
            if((null == uid) || (null == currentUser.getUid()) || !currentUser.getUid().equalsIgnoreCase(uid))
            {
                throw new UnsupportedOperationException("Given uid" + uid + " " + "is not matching with logged in user " + currentUser.getUid());
            }
        }
        NuvocoCustomerModel customer = (NuvocoCustomerModel) userService.getUserForUID(uid);

        NuvocoCustomerData data = new NuvocoCustomerData();


        if(Objects.nonNull(customer))
        {
            NuvocoImageData profilePic = new NuvocoImageData();
            if(customer.getProfilePicture()!=null)
            {
                profilePic.setUrl(customer.getProfilePicture().getURL());
                data.setProfilePic(profilePic);
            }

            data.setName(customer.getName());
            data.setContactNumber(customer.getMobileNumber());
            data.setEmailId(customer.getEmail());
            data.setErpCustomerNo(customer.getCustomerNo());
            if(customer.getPanCard()!=null){
                data.setPanNo(customer.getPanCard());
            }
            if(customer.getTanNo()!=null){
                data.setTanNo(customer.getTanNo());
            }
            if(customer.getGstIN()!=null){
                data.setGstIn(customer.getGstIN());
            }


            Collection<AddressModel> list = customer.getAddresses();
            if(CollectionUtils.isNotEmpty(list)) {
                List<AddressModel> billingAddressList = list.stream().filter(a -> a.getBillingAddress()).collect(Collectors.toList());
                if(billingAddressList != null && !billingAddressList.isEmpty()) {
                    AddressModel billingAddress = billingAddressList.get(0);
                    if(null != billingAddress)
                    {
                       data.setAddress((nuvocoAddressConverter.convert(billingAddress)));
                    }
                }
            }

            Set<PrincipalGroupModel> ugSet = customer.getGroups();

            if(ugSet.contains(userService.getUserGroupForUID(NuvocoCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))
            {
               data.setRetailerCount(territoryManagementService.getRetailerListForDealer().size());
              //  data.setInfluencerCount(territoryManagementService.getInfluencerListForDealer().size());

            }
            else if(ugSet.contains(userService.getUserGroupForUID(NuvocoCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)))
            {
               // data.setInfluencerCount(territoryManagementService.getInfluencerListForRetailer().size());
            }

             data.setFleetCount(dealerTransitService.fetchVehicleDetailsForDealer(customer).size());
             data.setUid(customer.getUid());

            Date doj = customer.getDateOfJoining();

            if(doj!=null)
            {
                int year = Calendar.getInstance().get(Calendar.YEAR);

                Calendar cal = Calendar.getInstance();
                cal.setTime(doj);

                int yearOfJoining = cal.get(Calendar.YEAR);

                data.setYearsOfAssociation(year-yearOfJoining);
            }

        }
        return data;
    }

    /**
     * @param dealerUid
     * @return
     */
    @Override
    public DealerVehicleDetailsListData getDealerVehicleDetails(String dealerUid) {

        final NuvocoCustomerModel nuvocoCustomer = nuvocoCustomerService.getCustomerForUid(dealerUid);
        BigDecimal totalCapacity = BigDecimal.ZERO;
        List<DealerVehicleDetailsModel> dealerVehicleDetailsModelList = dealerTransitService.fetchVehicleDetailsForDealer(nuvocoCustomer);
        List<DealerVehicleDetailsData> dealerVehicleDetailsDataList = new ArrayList<>();
        DealerVehicleDetailsListData dealerVehicleDetailsList = new DealerVehicleDetailsListData();
        if(CollectionUtils.isNotEmpty(dealerVehicleDetailsModelList)){
            for(DealerVehicleDetailsModel dealerVehicleDetailsModel : dealerVehicleDetailsModelList){
                totalCapacity = totalCapacity.add(BigDecimal.valueOf(dealerVehicleDetailsModel.getCapacity()));
                final DealerVehicleDetailsData dealerVehicleDetailsData = new DealerVehicleDetailsData();
                dealerVehicleDetailsConverter.convert(dealerVehicleDetailsModel,dealerVehicleDetailsData);
                dealerVehicleDetailsDataList.add(dealerVehicleDetailsData);
            }
        }
        final CustomerData customerData = new CustomerData();
        customerConverter.convert(nuvocoCustomer,customerData);

        dealerVehicleDetailsList.setVehicleDetails(dealerVehicleDetailsDataList);
        dealerVehicleDetailsList.setDealer(customerData);
        dealerVehicleDetailsList.setFleetCount(String.valueOf(dealerVehicleDetailsDataList.size()));
        dealerVehicleDetailsList.setTotalCapacity(totalCapacity.stripTrailingZeros().toPlainString());
        return dealerVehicleDetailsList;

    }

    /**
     * @param dealerUid
     * @return
     */
    @Override
    public DealerDriverDetailsListData getDealerDriverDetails(String dealerUid) {

        final NuvocoCustomerModel sclCustomer = nuvocoCustomerService.getCustomerForUid(dealerUid);
        List<DealerDriverDetailsModel> dealerDriverDetailsModelList = dealerTransitService.fetchDriverDetailsForDealer(sclCustomer);
        List<DealerDriverDetailsData> dealerDriverDetailsDataList = new ArrayList<>();
        DealerDriverDetailsListData dealerDriverDetailsList = new DealerDriverDetailsListData();
        if(CollectionUtils.isNotEmpty(dealerDriverDetailsModelList)){
            for(DealerDriverDetailsModel dealerDriverDetailsModel : dealerDriverDetailsModelList){
                final DealerDriverDetailsData dealerDriverDetailsData = new DealerDriverDetailsData();
                dealerDriverDetailsConverter.convert(dealerDriverDetailsModel,dealerDriverDetailsData);
                dealerDriverDetailsDataList.add(dealerDriverDetailsData);
            }
        }
        final CustomerData customerData = new CustomerData();
        customerConverter.convert(sclCustomer,customerData);

        dealerDriverDetailsList.setDriverDetails(dealerDriverDetailsDataList);
        dealerDriverDetailsList.setDealer(customerData);
        dealerDriverDetailsList.setNoOfDrivers(String.valueOf(dealerDriverDetailsDataList.size()));
        return dealerDriverDetailsList;

    }

    /**
     * @param dealerVehicleDetailsListData
     * @param dealerUid
     * @return
     */
    @Override
    public ErrorListWsDTO createDealerVehicleDetails(DealerVehicleDetailsListData dealerVehicleDetailsListData, String dealerUid) {

        List<DealerVehicleDetailsModel> dealerVehicleDetailsModelList =  new ArrayList<>();
        List<DealerVehicleDetailsData> dealerVehicleDetailsDataList = dealerVehicleDetailsListData.getVehicleDetails();
        final ErrorListWsDTO errorListWsDTO = new ErrorListWsDTO();
        final List<ErrorWsDTO> errorWsDTOList = new ArrayList<>();

        for(DealerVehicleDetailsData dealerVehicleDetailsData : dealerVehicleDetailsDataList){

            DealerVehicleDetailsModel dealerVehicleDetailsModel = dealerVehicleDetailsDao.findVehicleDetailsByVehicleNumber(dealerVehicleDetailsData.getVehicleNumber());
            if(dealerVehicleDetailsModel!=null)
            {
                dealerVehicleDetailsReversePopulator.populate(dealerVehicleDetailsData,dealerVehicleDetailsModel);
                dealerVehicleDetailsModelList.add(dealerVehicleDetailsModel);
            }
            else
            {
                dealerVehicleDetailsModel=modelService.create(DealerVehicleDetailsModel.class);
                dealerVehicleDetailsReversePopulator.populate(dealerVehicleDetailsData,dealerVehicleDetailsModel);
                dealerVehicleDetailsModelList.add(dealerVehicleDetailsModel);
            }
        }
        dealerTransitService.saveVehicleDetailsForDealer(dealerVehicleDetailsModelList,dealerUid);
        errorListWsDTO.setErrors(errorWsDTOList);
        return errorListWsDTO;
    }

    /**
     * @param dealerDriverDetailsListData
     * @param dealerUid
     * @return
     */
    @Override
    public ErrorListWsDTO createDealerDriverDetails(DealerDriverDetailsListData dealerDriverDetailsListData, String dealerUid) {

        List<DealerDriverDetailsModel> dealerDriverDetailsModelList =  new ArrayList<>();
        List<DealerDriverDetailsData> dealerDriverDetailsDataList = dealerDriverDetailsListData.getDriverDetails();

        final ErrorListWsDTO errorListWsDTO = new ErrorListWsDTO();
        final List<ErrorWsDTO> errorWsDTOList = new ArrayList<>();

        for(DealerDriverDetailsData dealerDriverDetailsData : dealerDriverDetailsDataList){

            DealerDriverDetailsModel dealerDriverDetailsModel = dealerDriverDetailsDao.findDriverDetailsByContactNumber(dealerDriverDetailsData.getContactNumber());
            if(dealerDriverDetailsModel!=null)
            {
                dealerDriverDetailsReversePopulator.populate(dealerDriverDetailsData,dealerDriverDetailsModel);
                dealerDriverDetailsModelList.add(dealerDriverDetailsModel);
            }
            else
            {
                dealerDriverDetailsModel = modelService.create(DealerDriverDetailsModel.class);
                dealerDriverDetailsReversePopulator.populate(dealerDriverDetailsData,dealerDriverDetailsModel);
                dealerDriverDetailsModelList.add(dealerDriverDetailsModel);
            }
        }
        dealerTransitService.saveDriverDetailsForDealer(dealerDriverDetailsModelList,dealerUid);

        errorListWsDTO.setErrors(errorWsDTOList);
        return errorListWsDTO;
    }

    /**
     * @param vehicleNumber
     * @return
     */
    @Override
    public ErrorWsDTO removeVehicle(String vehicleNumber) {
        return dealerTransitService.removeVehicle(vehicleNumber);
    }

    /**
     * @param contactNumber
     * @return
     */
    @Override
    public ErrorWsDTO removeDriver(String contactNumber) {
        return dealerTransitService.removeDriver(contactNumber);
    }

}
