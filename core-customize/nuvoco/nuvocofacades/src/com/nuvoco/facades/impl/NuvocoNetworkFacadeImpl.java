package com.nuvoco.facades.impl;

import com.nuvoco.core.constants.NuvocoCoreConstants;
import com.nuvoco.core.dao.OrderRequisitionDao;
import com.nuvoco.core.dao.SalesPerformanceDao;
import com.nuvoco.core.model.NuvocoCustomerModel;
import com.nuvoco.core.model.NuvocoUserModel;
import com.nuvoco.core.services.TerritoryManagementService;
import com.nuvoco.facades.NuvocoNetworkFacade;
import com.nuvoco.facades.data.DealerCurrentNetworkData;
import com.nuvoco.facades.data.SalesQuantityData;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class NuvocoNetworkFacadeImpl implements NuvocoNetworkFacade {

    private static final Logger LOG = Logger.getLogger(NuvocoNetworkFacadeImpl.class);

    @Resource
    private UserService userService;

    @Autowired
    OrderRequisitionDao orderRequistionDao;

    @Resource
    private TerritoryManagementService territoryManagementService;

    @Resource
    private BaseSiteService baseSiteService;
    @Resource
    private SalesPerformanceDao salesPerformanceDao;
    DecimalFormat df = new DecimalFormat("#.#");

    /**
     * @param searchKey
     * @param isNew
     * @param networkType
     * @param searchPageData
     * @return
     */
    @Override
    public SearchPageData<DealerCurrentNetworkData> getRetailerDetailedSummaryList(String searchKey, Boolean isNew, String networkType, SearchPageData searchPageData) {
        SearchPageData<NuvocoCustomerModel> searchResult = null ;
        B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();

        if(currentUser instanceof NuvocoUserModel) {
            searchResult = territoryManagementService.getCustomerByTerritoriesAndCounterType(searchPageData, "Retailer", networkType, isNew, searchKey, null, null);
        }
        if(currentUser instanceof  NuvocoCustomerModel){
            if((currentUser.getGroups().contains(userService.getUserGroupForUID(NuvocoCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))){
                searchResult = territoryManagementService.getRetailerListForDealerPagination(searchPageData, networkType, isNew, searchKey);
            }
        }

        List<DealerCurrentNetworkData> dataList = new ArrayList<>();
        if (searchResult != null && searchResult.getResults() != null) {
            List<NuvocoCustomerModel> itemWonList = searchResult.getResults();
            dataList = getRetailerDetailedSummaryListData(itemWonList);
        }
        final SearchPageData<DealerCurrentNetworkData> result = new SearchPageData<>();
        result.setPagination(searchResult.getPagination());
        result.setSorts(searchResult.getSorts());
        result.setResults(dataList);
        LOG.info(result.getResults());
        return result;
    }

    /**
     * @param retailerList
     * @return
     */
    @Override
    public List<DealerCurrentNetworkData> getRetailerDetailedSummaryListData(List<NuvocoCustomerModel> retailerList) {
        String startDate = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).toString();
        String endDate = LocalDate.now().toString();
        String startDateLastMonth = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).minusMonths(1).toString();
        String endDateLastMonth = LocalDate.now().minusMonths(1).toString();

        List<List<Object>> list = orderRequistionDao.getSalsdMTDforRetailer(retailerList, startDate, endDate);
        Map<String, Double> map = list.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
                .collect(Collectors.toMap(each->((NuvocoCustomerModel)each.get(0)).getUid(), each->(Double)each.get(1)));

        List<List<Object>> listLastMonth = orderRequistionDao.getSalsdMTDforRetailer(retailerList, startDateLastMonth, endDateLastMonth);
        Map<String, Double>  mapLastMonth = listLastMonth.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
                .collect(Collectors.toMap(each->((NuvocoCustomerModel)each.get(0)).getUid(), each->(Double)each.get(1)));

        LocalDate currentYearCurrentDate= LocalDate.now();
        LocalDate currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear(), Month.APRIL, 1);
        if(currentYearCurrentDate.getMonth().compareTo(Month.APRIL)<0) {
            currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear()-1, Month.APRIL, 1);
        }
        LocalDate lastYearCurrentDate= currentYearCurrentDate.minusYears(1);//2022-04-02

        LocalDate lastFinancialYearDate = currentFinancialYearDate.minusYears(1);//2022-04-01

        List<List<Object>> currentYTD = orderRequistionDao.getSalsdMTDforRetailer(retailerList, currentFinancialYearDate.toString(), currentYearCurrentDate.toString());
        Map<String, Double>  mapCurrentYTD = currentYTD.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
                .collect(Collectors.toMap(each->((NuvocoCustomerModel)each.get(0)).getUid(), each->(Double)each.get(1)));

        List<List<Object>> lastYTD = orderRequistionDao.getSalsdMTDforRetailer(retailerList, lastFinancialYearDate.toString(), lastYearCurrentDate.toString());

        Map<String, Double>  mapLastYTD = lastYTD.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
                .collect(Collectors.toMap(each->((NuvocoCustomerModel)each.get(0)).getUid(), each->(Double)each.get(1)));

        List<DealerCurrentNetworkData> summaryDataList = new ArrayList<>();
        retailerList.forEach(retailer -> {
            DealerCurrentNetworkData dealerCurrentNetworkData = new DealerCurrentNetworkData();
            var subAraMappinglist = territoryManagementService.getTerritoriesForCustomer(retailer);
            dealerCurrentNetworkData.setCode(retailer.getUid());
            if(retailer.getContactNumber()!=null){
                dealerCurrentNetworkData.setContactNumber(retailer.getMobileNumber());
            }
            dealerCurrentNetworkData.setName(retailer.getName());
            B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
            double salesMtd=0.0,salesQuantity=0.0,salesQuantityLastMonth=0.0,salesMtdLastMonth=0.0,salesLastYearQty=0.0,salesLastYear=0.0,salesCurrentYearQty=0.0,salesCurrentYear=0.0;
            if(currentUser instanceof NuvocoUserModel) {
                if (retailer.getCounterPotential() != null) {
                    dealerCurrentNetworkData.setPotential(String.valueOf(retailer.getCounterPotential()));
                } else {
                    dealerCurrentNetworkData.setPotential("0");
                }
            }else{
                if (retailer.getCounterPotential() != null) {
                    dealerCurrentNetworkData.setPotential(String.valueOf(retailer.getCounterPotential() / 20));
                } else {
                    dealerCurrentNetworkData.setPotential("0");
                }
            }

            if(map.containsKey(retailer.getUid())) {
                salesMtd = map.get(retailer.getUid());
            }

            if(mapLastMonth.containsKey(retailer.getUid())) {
                if(mapLastMonth.get(retailer.getUid())!=null){
                    salesMtdLastMonth = mapLastMonth.get(retailer.getUid());
                }
            }
            if(currentUser instanceof NuvocoUserModel) {
                salesQuantity = salesMtd;
                salesQuantityLastMonth = salesMtdLastMonth ;
            }else{
                salesQuantity = (salesMtd / 20);
                salesQuantityLastMonth = (salesMtdLastMonth / 20);
            }
            SalesQuantityData sales = new SalesQuantityData();
            sales.setRetailerSaleQuantity(salesQuantity);
            sales.setCurrent(salesQuantity);
            sales.setLastMonth(salesQuantityLastMonth);
            dealerCurrentNetworkData.setSalesQuantity(sales);

            if(currentUser instanceof NuvocoUserModel) {
                if (retailer.getLastLiftingDate() != null) {
                    LocalDate today = LocalDate.now();
                    LocalDate transactionDate = retailer.getLastLiftingDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    dealerCurrentNetworkData.setDaySinceLastOrder(String.valueOf(Math.toIntExact(ChronoUnit.DAYS.between(transactionDate, today))));
                } else {
                    dealerCurrentNetworkData.setDaySinceLastOrder(String.valueOf("-"));
                }
            }
            else {
                if (retailer.getLastLiftingDate() != null) {
                    NuvocoCustomerModel retailerSalesForDealer = salesPerformanceDao.getRetailerSalesForDealer(retailer, baseSiteService.getCurrentBaseSite());
                    if (retailerSalesForDealer != null) {
                        LocalDate today = LocalDate.now();
                        LocalDate transactionDate = retailer.getLastLiftingDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                        dealerCurrentNetworkData.setDaySinceLastOrder(String.valueOf(Math.toIntExact(ChronoUnit.DAYS.between(transactionDate, today))));
                    } else {
                        dealerCurrentNetworkData.setDaySinceLastOrder(String.valueOf("-"));
                    }
                }
            }

            if(mapCurrentYTD.containsKey(retailer.getUid())) {
                salesCurrentYear = mapCurrentYTD.get(retailer.getUid());
            }

            if(mapLastYTD.containsKey(retailer.getUid())) {
                salesLastYear = mapLastYTD.get(retailer.getUid());
            }
            if(currentUser instanceof NuvocoUserModel) {
                salesCurrentYearQty = (salesCurrentYear);
                salesLastYearQty = (salesLastYear);
            }else{
                salesCurrentYearQty = (salesCurrentYear / 20);
                salesLastYearQty = (salesLastYear / 20);
            }

            dealerCurrentNetworkData.setSalesYtd(df.format(salesCurrentYearQty));
            dealerCurrentNetworkData.setGrowthRate(df.format(getYearToYearGrowth(salesCurrentYearQty,salesLastYearQty)));
            if(CollectionUtils.isNotEmpty(subAraMappinglist)) {
                var subareaMaster=subAraMappinglist.get(0);
                dealerCurrentNetworkData.setDistrict(subareaMaster.getDistrict());
                dealerCurrentNetworkData.setTaluka(subareaMaster.getTaluka());
            }
            summaryDataList.add(dealerCurrentNetworkData);
        });
        AtomicInteger rank=new AtomicInteger(1);
        summaryDataList.stream().sorted(Comparator.comparing(nw -> nw.getSalesQuantity().getRetailerSaleQuantity())).forEach(data->data.setRank(String.valueOf(rank.getAndIncrement())));
        return summaryDataList;
    }

    private Double getYearToYearGrowth(double salesCurrentYearQty, double salesLastYearQty){
        if(salesLastYearQty>0) {
            return   (((salesCurrentYearQty- salesLastYearQty) / salesLastYearQty) * 100);
        }
        return 0.0;
    }
}
