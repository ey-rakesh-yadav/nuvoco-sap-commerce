package com.nuvoco.core.services.impl;

import com.nuvoco.core.dao.*;
import com.nuvoco.core.enums.CustomerCategory;
import com.nuvoco.core.enums.NetworkType;
import com.nuvoco.core.model.*;
import com.nuvoco.core.services.DJPVisitService;
import com.nuvoco.core.services.TerritoryManagementService;
import com.nuvoco.core.utility.NuvocoDateUtility;
import com.nuvoco.facades.data.DealerSummaryData;
import com.nuvoco.facades.data.MonthlySalesData;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public class DJPVisitServiceImpl implements DJPVisitService {

    private static final Logger LOG = Logger.getLogger(DJPVisitServiceImpl.class);

    @Autowired
    NuvocoTruckDao nuvocoTruckDao;

    @Autowired
    RouteMasterDao routeMasterDao;

    @Autowired
    DJPRunDao djpRunDao;

    @Autowired
    NuvocoOrderCountDao nuvocoOrderCountDao;

    @Autowired
    CollectionDao collectionDao;

    @Autowired
    CounterVisitMasterDao counterVisitDao;

    @Autowired
    BaseSiteService baseSiteService;
    @Autowired
    DJPVisitDao djpVisitDao;

    @Autowired
    UserService userService;

    @Autowired
    OrderValidationProcessDao orderValidationProcessDao;

    @Autowired
    TerritoryManagementService territoryManagementService;

    @Autowired
    SalesPerformanceDao salesPerformanceDao;

    @Autowired
    SalesPlanningDao salesPlanningDao;

    @Autowired
    ObjectiveDao objectiveDao;



    /**
     * @return
     */
    @Override
    public List<ObjectiveModel> getAllObjective() {
        return objectiveDao.findAllObjective();
    }

    /**
     * @return
     */
    @Override
    public List<TruckModelMasterModel> findAllTrucks() {
        List<TruckModelMasterModel> truckModelList = nuvocoTruckDao.findAllTrucks();
        return Objects.nonNull(truckModelList) ? truckModelList : Collections.emptyList();
    }

    /**
     * @param counterVisitId
     * @return
     */
    @Override
    public NuvocoCustomerModel getProductMixForDealerSummary(String counterVisitId) {
     //   CounterVisitMasterModel counterVisitModel = counterVisitDao.findCounterVisitById(counterVisitId);
       // return counterVisitModel.getSclCustomer();
        return null;
    }

    /**
     * @param counterVisitId
     * @return
     */
    @Override
    public DealerSummaryData getDealerSummary(String counterVisitId) {

        Calendar cal = Calendar.getInstance();
        int month1 = cal.get(Calendar.MONTH);
        int year1 = cal.get(Calendar.YEAR);

        cal.add(Calendar.MONTH, -1);
        int month2 = cal.get(Calendar.MONTH);
        int year2 = cal.get(Calendar.YEAR);

        DealerSummaryData data = new DealerSummaryData();

        CounterVisitMasterModel counterVisitModel = counterVisitDao.findCounterVisitById(counterVisitId);
        try {
            data.setSales(counterVisitModel.getTotalSale()!=null?counterVisitModel.getTotalSale():0.0);
            NuvocoCustomerModel nuvocoCustomer = counterVisitModel.getNuvocoCustomer();
            if(Objects.nonNull(nuvocoCustomer))
            {
                if(Objects.nonNull(nuvocoCustomer.getDealerCategory())){
                    data.setDealerCategory(nuvocoCustomer.getDealerCategory().getCode());
                }
                int visits = djpVisitDao.getVisitCountMTD(nuvocoCustomer, month1, year1);
                data.setVisits(visits);
                data.setPotential(nuvocoCustomer.getCounterPotential());
                data.setLastVisitDate(nuvocoCustomer.getLastVisitTime());
                data.setWholeSale(nuvocoCustomer.getWholeSale());
                data.setCounterSale(nuvocoCustomer.getRetailSale());

                List<List<Object>> currentMonthCounterShareInfo = djpVisitDao.getCounterSharesForDealerOrRetailer(counterVisitModel.getNuvocoCustomer().getPk().toString(), month1, year1);
                try {
                    data.setCurrentMonthCounterShare((Double)currentMonthCounterShareInfo.get(0).get(0)!=null?((Double)currentMonthCounterShareInfo.get(0).get(0)/Double.valueOf((Integer)currentMonthCounterShareInfo.get(0).get(1))):0.0);
                }catch(Exception e)
                {
                    data.setCurrentMonthCounterShare(0.0);
                }

                List<List<Object>> lastMonthCounterShareInfo = djpVisitDao.getCounterSharesForDealerOrRetailer(counterVisitModel.getNuvocoCustomer().getPk().toString(), month2, year2);
                try {
                    data.setLastMonthCounterShare((Double)lastMonthCounterShareInfo.get(0).get(0)!=null?((Double)lastMonthCounterShareInfo.get(0).get(0)/Double.valueOf((Integer)lastMonthCounterShareInfo.get(0).get(1))):0.0);
                }catch(Exception e)
                {
                    data.setLastMonthCounterShare(0.0);
                }

                BaseSiteModel brand = baseSiteService.getCurrentBaseSite();
                CustomerCategory category = CustomerCategory.TR;

                Double totalOutstandingAmount = 0.0;
                if(nuvocoCustomer.getCustomerNo()!=null) {
                    totalOutstandingAmount = djpVisitDao.getDealerOutstandingAmount(nuvocoCustomer.getCustomerNo());
                }

                cal = Calendar.getInstance();
                Date curDate = cal.getTime();
                cal.add(Calendar.YEAR, -1);
                Date prevYear = cal.getTime();

                Double dailyAverageSales = 0.0;
                if(nuvocoCustomer.getCustomerNo()!=null) {
                    dailyAverageSales = collectionDao.getDailyAverageSalesForDealer(nuvocoCustomer.getCustomerNo());
                }

                Double outstandingDays = 0.0;
                if(dailyAverageSales!=0.0)
                {
                    outstandingDays = totalOutstandingAmount/dailyAverageSales;
                }
                data.setOutstandingDays(outstandingDays);

                data.setOutstandingAmount(totalOutstandingAmount);
                //Credit consumed/credit utilized = [(outstanding amount + pending orders)/Total credit limit]*100

                //Credit utilized = outstanding amount+pending orders

                // Double securityDeposit = collectionDao.getSecurityDepositForDealer(sclCustomer.getCustomerNo());
                // Double creditLimitMultiplier = 2.0;
                Double creditLimit = 0.0;
                if(nuvocoCustomer.getCustomerNo()!=null) {
                    creditLimit = djpVisitDao.getDealerCreditLimit(nuvocoCustomer.getCustomerNo());
                }
                Double availableCredit = creditLimit - totalOutstandingAmount;
                double pendingOrderAmount = orderValidationProcessDao.getPendingOrderAmount(nuvocoCustomer.getPk().toString());
                if (creditLimit != 0.0) {
                    //Double creditConsumed = (totalOutstandingAmount / (totalOutstandingAmount + availableCredit)) * 100;
                    double creditConsumed = ((totalOutstandingAmount + pendingOrderAmount) / creditLimit) * 100;
                    data.setCreditConsumed(creditConsumed);
                } else {
                    data.setCreditConsumed(0.0);
                }
                if (availableCredit > 0.0) {
                    data.setAvailableCredit(availableCredit);
                } else {
                    data.setAvailableCredit(0.0);
                }

                if(nuvocoCustomer.getCustomerNo()!=null) {
                    setOutStandingBuckets(data,nuvocoCustomer.getCustomerNo());

                    Date maxDate=djpVisitDao.getLastLiftingDateForDealer(nuvocoCustomer.getCustomerNo(), brand);
                    if(maxDate != null) {
                        data.setLastLiftingDate(maxDate);

                        Double lastLiftingQuantity= djpVisitDao.getLastLiftingQuantityForDealer(nuvocoCustomer.getCustomerNo(),brand,maxDate);
                        data.setLastLiftingQuantity(lastLiftingQuantity);

                        cal=Calendar.getInstance();                        cal.add(Calendar.MONTH, -1);
                        Date date = cal.getTime();
                        cal.add(Calendar.MONTH, -2);
                        Date date2 = cal.getTime();

                        if(data.getLastLiftingDate()!=null)
                        {
                            if(date.compareTo(data.getLastLiftingDate())<0)
                                data.setNetworkType(NetworkType.ACTIVE.getCode());
                            else if(date2.compareTo(data.getLastLiftingDate())<0)
                                data.setNetworkType(NetworkType.INACTIVE.getCode());
                            else
                                data.setNetworkType(NetworkType.DORMANT.getCode());
                        }
                    }

                    Integer creditBreachedMTDCount = nuvocoOrderCountDao.findCreditBreachCountMTD(nuvocoCustomer);
                    data.setCreditBreachCount(creditBreachedMTDCount);
                    List<List<Double>> bucketList = djpVisitDao.getOutstandingBucketsForDealer(nuvocoCustomer.getCustomerNo());
                    if (CollectionUtils.isNotEmpty(bucketList) && CollectionUtils.isNotEmpty(bucketList.get(0))) {
                        double bucketsTotal = bucketList.get(0).stream().filter(b -> b != null).mapToDouble(b -> b.doubleValue()).sum();
                        data.setBucketsTotal(String.valueOf(bucketsTotal));
                    }
                }
                else {
                    data.setCreditBreachCount(0);
                    data.setBucketsTotal("0.0");
                    data.setLastLiftingQuantity(0.0);
                    data.setLastLiftingDate(null);
                }

                //flag/unflag
                data.setIsDealerFlag(nuvocoCustomer.getIsDealerFlag());
                data.setRemarkForFlag(nuvocoCustomer.getRemarkForFlag());
                //data.setCurrentRemarkForFlag(sclCustomer.getCurrentRemarkForFlag());


                UserModel user = userService.getCurrentUser();
                userService.setCurrentUser(nuvocoCustomer);
                Integer retailerNetwork = territoryManagementService.getRetailerCountForDealer();
                data.setRetailerNetwork(retailerNetwork!=null?retailerNetwork:0);

                Integer influencerNetwork = territoryManagementService.getInfluencerCountForDealer();
                data.setInfluencerNetwork(influencerNetwork!=null?influencerNetwork:0);
                userService.setCurrentUser(user);

            }


        }catch(NullPointerException e)
        {
            LOG.info("CounterVisitModel not found");
        }
        return data;
    }

    /**
     * @param counterVisitId
     * @return
     */
    @Override
    public List<MonthlySalesData> getLastSixMonthSalesForDealer(String counterVisitId) {
        List<MonthlySalesData> dataList = new ArrayList<>();

        CounterVisitMasterModel counterVisitModel = counterVisitDao.findCounterVisitById(counterVisitId);
        if (Objects.nonNull(counterVisitModel) && Objects.nonNull(counterVisitModel.getNuvocoCustomer())) {
            NuvocoCustomerModel customer = counterVisitModel.getNuvocoCustomer();
            getMonthlySalesForDealer(dataList, customer);
        }

        return dataList;
    }

    /**
     * @param plannedDate
     * @param subAreaMasterPk
     * @param recommendedRoute
     * @return
     */
    @Override
    public List<RouteMasterModel> getRouteMasterList(String plannedDate, String subAreaMasterPk, List<RouteMasterModel> recommendedRoute) {
        SubAreaMasterModel subAreaMaster = territoryManagementService.getTerritoryById(subAreaMasterPk);
        BaseSiteModel site = baseSiteService.getCurrentBaseSite();
        List<RouteMasterModel> routeMasterList = routeMasterDao.findBySubAreaAndBrand(subAreaMaster, site);
        List<RouteMasterModel> output = new ArrayList<>();
        DJPRunMasterModel model = djpRunDao.findByPlannedDateAndUser(plannedDate, subAreaMaster.getDistrict(), subAreaMaster.getTaluka(), site.getUid());
        if(model!=null) {
            RouteMasterModel recommendedRoute1 = model.getRecommendedRoute1();
            RouteMasterModel recommendedRoute2 = model.getRecommendedRoute2();
            if(recommendedRoute1!=null) {
                recommendedRoute.add(recommendedRoute1);
                output.add(recommendedRoute1);
            }
            if(recommendedRoute2!=null) {
                recommendedRoute.add(recommendedRoute2);
                output.add(recommendedRoute2);
            }
        }
        output.addAll(routeMasterList.stream().filter(route-> !output.contains(route)).collect(Collectors.toList()));

        return output;
    }

    /**
     * @param plannedDate
     * @param recommendedRoute
     * @param district
     * @param taluka
     * @return
     */
    @Override
    public Collection<DJPRouteScoreMasterModel> getDJPRouteScores(String plannedDate, List<DJPRouteScoreMasterModel> recommendedRoute, String district, String taluka) {
        List<VisitMasterModel>  list = null;
        try {
            list = djpVisitDao.getPlannedVisitForToday(userService.getCurrentUser(), plannedDate);
        }
        catch(Exception e) {

        }
        //      if(list!=null && !list.isEmpty()) {
        //          throw new IllegalArgumentException(String.format("Visit is already planned for this date %s", plannedDate));
        //      }
        DJPRunMasterModel model = djpRunDao.findByPlannedDateAndUser(plannedDate, district, taluka, baseSiteService.getCurrentBaseSite().getUid());
        List<DJPRouteScoreMasterModel> output = new ArrayList<DJPRouteScoreMasterModel>();

        if(model!=null) {
            List<DJPRouteScoreMasterModel> routeScoreList = djpVisitDao.findAllRouteForPlannedDate(model);
            RouteMasterModel recommendedRoute1 = model.getRecommendedRoute1();
            RouteMasterModel recommendedRoute2 = model.getRecommendedRoute2();
            Optional<DJPRouteScoreMasterModel> recommendedDjpRoute1 = routeScoreList.stream().filter(djpRoute -> recommendedRoute1!=null && djpRoute.getRoute()!=null && djpRoute.getRoute().getRouteId().equals(recommendedRoute1.getRouteId())).findFirst();
            Optional<DJPRouteScoreMasterModel> recommendedDjpRoute2 = routeScoreList.stream().filter(djpRoute -> recommendedRoute2!=null && djpRoute.getRoute()!=null && djpRoute.getRoute().getRouteId().equals(recommendedRoute2.getRouteId())).findFirst();
            if(recommendedDjpRoute1.isPresent()) {
                output.add(recommendedDjpRoute1.get());
                recommendedRoute.add(recommendedDjpRoute1.get());
            }
            if(recommendedDjpRoute2.isPresent()) {
                output.add(recommendedDjpRoute2.get());
                recommendedRoute.add(recommendedDjpRoute2.get());
            }
            output.addAll(routeScoreList.stream().filter(djpRoute-> !output.contains(djpRoute)).collect(Collectors.toList()));
        }
        return output;
    }

    /**
     * @param dataList
     * @param customer
     */
    @Override
    public void getMonthlySalesForDealer(List<MonthlySalesData> dataList, NuvocoCustomerModel customer) {

        if (customer != null) {
            String customerNo = customer.getCustomerNo() != null ? customer.getCustomerNo() : " ";

            BaseSiteModel currentBaseSite = baseSiteService.getCurrentBaseSite();

            CustomerCategory category = CustomerCategory.TR;

            LocalDate currentMonth = LocalDate.now();
            //var salesOfficer=(SclUserModel) userService.getCurrentUser();
            //var subAreas = territoryManagementService.getTerritoriesForSO();
            for (int i = 1; i <= 6; i++) {
                MonthlySalesData data = new MonthlySalesData();
                //Double currentMonthSale = djpVisitDao.getSalesHistoryData(customerNo, currentMonth.getMonthValue(), currentMonth.getYear(), category, currentBaseSite);
                Double currentMonthSale = salesPerformanceDao.getActualTargetSalesForSelectedMonthAndYearForDealer(customer, currentBaseSite, currentMonth.getMonthValue(), currentMonth.getYear(),null);
                LOG.info("currentMonthSale:"+currentMonthSale);
                LocalDate lastMonth = currentMonth.minusMonths(1);
                //Double lastMonthSale = djpVisitDao.getSalesHistoryData(customerNo, lastMonth.getMonthValue(), lastMonth.getYear(), category, currentBaseSite);
                Double lastMonthSale= salesPerformanceDao.getActualTargetSalesForSelectedMonthAndYearForDealer(customer, currentBaseSite, lastMonth.getMonthValue(), lastMonth.getYear(),null);
                data.setActualSales(currentMonthSale);
                double growth = currentMonthSale - lastMonthSale;
                data.setGrowth(growth);

                Date date = Date.from(currentMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());
                var monthYear = NuvocoDateUtility.getFormattedDate(date, "MMM-YYYY");
                Double targetSale = salesPlanningDao.getDealerSalesAnnualTarget(customer.getUid(),monthYear);
                data.setTargetSales(targetSale);
                if (targetSale > 0) {
                    data.setPercentage((currentMonthSale / targetSale) * 100);
                }else {
                    data.setPercentage(0.0);
                }

                if(Objects.nonNull(monthYear)) {
                    data.setMonthYear(monthYear.replace("-", " "));
                }
                dataList.add(data);
                currentMonth = lastMonth;
            }
        }
    }

    private void setOutStandingBuckets(DealerSummaryData data, String customerId)
    {
        List<List<Double>> bucketList = djpVisitDao.getOutstandingBucketsForDealer(customerId);
        if(!bucketList.isEmpty()&&!Objects.isNull(bucketList))
        {
            try {
                data.setBucket1(bucketList.get(0).get(0)!=null?bucketList.get(0).get(0):0.0);
                data.setBucket2(bucketList.get(0).get(1)!=null?bucketList.get(0).get(1):0.0);
                data.setBucket3(bucketList.get(0).get(2)!=null?bucketList.get(0).get(2):0.0);
                data.setBucket4(bucketList.get(0).get(3)!=null?bucketList.get(0).get(3):0.0);
                data.setBucket5(bucketList.get(0).get(4)!=null?bucketList.get(0).get(4):0.0);
                data.setBucket6(bucketList.get(0).get(5)!=null?bucketList.get(0).get(5):0.0);
                data.setBucket7(bucketList.get(0).get(6)!=null?bucketList.get(0).get(6):0.0);
                data.setBucket8(bucketList.get(0).get(7)!=null?bucketList.get(0).get(7):0.0);
                data.setBucket9(bucketList.get(0).get(8)!=null?bucketList.get(0).get(8):0.0);
                data.setBucket10(bucketList.get(0).get(9)!=null?bucketList.get(0).get(9):0.0);
            }catch(Exception e)
            {
                LOG.info(e);
            }

        }
        else
        {
            data.setBucket1(0.0);
            data.setBucket2(0.0);
            data.setBucket3(0.0);
            data.setBucket4(0.0);
            data.setBucket5(0.0);
            data.setBucket6(0.0);
            data.setBucket7(0.0);
            data.setBucket8(0.0);
            data.setBucket9(0.0);
            data.setBucket10(0.0);
        }
    }
}
