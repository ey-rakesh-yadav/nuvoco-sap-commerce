package com.nuvoco.facades;

import com.nuvoco.facades.data.*;
import com.nuvoco.facades.visit.data.InfluencerSummaryData;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.storelocator.data.RouteData;
import de.hybris.platform.webservicescommons.dto.error.ErrorListWsDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface DJPVisitFacade {


    List<DJPRouteScoreData> getDJPRouteScores(String plannedDate, String subAreaMasterPk);

    List<ObjectiveData> getDJPObjective(String routeId, String routeScoreId);
    public boolean submitMarketMappingDetails(CounterVisitMasterData data);
  //  public boolean submitBrandInsightDetails(String counterVisitId, BrandingInsightData brandingInsightData);

  //  public boolean submitLeadGeneration(LeadMasterData leadMasterData, String counterVisitId);

    public boolean submitFlagDealer(String counterVisitId, boolean isFlagged, String remarkForFlag);

    //public boolean submitFeedbackAndComplaints(String counterVisitId, FeedbackAndComplaintsListData data);

   // DropdownListData getEnumTypes(String type);

   // public CompetitorProductListData getCompetitorProducts(String brandId);

    //SearchPageData<VisitMasterData> getMarketVisitDetails(SearchPageData searchPageData);

    public List<TruckModelData> getAllTrucks();

    public boolean submitTruckFleetDetails(DealerFleetListData dealerFleetListData, String counterVisitId);

   // CounterAggregationData getCounterAggregationData(String counterVisitId);

   // List<CounterVisitData> getCounterList(String id);

    //void finalizeCounterVisitPlan(final DJPFinalizedPlanWsDTO finalizedPlan);

  //  public List<DJPCounterScoreData> getDJPCounterScores(String routeScoreId, String objectiveId);

    //public List<DJPRouteScoreData> getDJPRouteScores(String plannedDate, String district, String taluka);

  //  public List<ObjectiveData> getDJPObjective(String routeScoreId);

    boolean startDjpVisit(String visitId);

    boolean completeDjpVisit(String visitId);

    boolean startCounterVisit(String counterVisitId);

    long completeCounterVisit(String counterVisitId);

   // List<CounterDetailsData> getExistingCounters(final DJPFinalizedPlanWsDTO plannedData);

   // SearchPageData<VisitMasterData> getReviewLogs(SearchPageData searchPageData, String startDate, String endDate,String searchKey, boolean isDjpApprovalWidget);

    public Long getCountOfCounterNotVisited();

    public Long getCountOfTotalJouneyPlanned();

    public Map<String, Double> getAvgTimeSpent();

    //public List<CounterDetailsData> getAdHocExistingCounters(DJPFinalizedPlanWsDTO plannedData);

    //void createAndSaveSiteDetails(final AddNewSiteData siteData);

    List<RouteData> getRoutesForSalesofficer();

    //boolean submitSiteVisitForm(String counterVisitId, SiteVisitFormData siteVisitFormData);

   // public List<CounterDetailsData> getcounterNotVisitedList(int month, int year);

    public Map<String, String> getLastSixCounterVisitDates(String customerId);

    public Map<String, Object> counterVisitedForSelectedRoutes(String routeScoreId);

   // public boolean saveOrderRequisitionForTaggedSites(OrderRequistionMasterData orderRequisitionData, String counterVisitId,
     //                                                 String siteCode);
    public String getLastVisitDate(String counterVisitId);
    public Integer getVisitCountMTD(String counterVisitId);

   // ErrorListWsDTO updateAdHocCounters(UpdateCountersWsDTO adHocCountersWsDTO);

   // ErrorListWsDTO updateCounters(UpdateCountersWsDTO updateCountersWsDTO);

    //public List<CounterVisitData> getSelectedCounterList(String id);

    //CounterVisitAnalyticsWsDTO getCompletedVisitStatisticsData();

   // public SiteSummaryData getSiteSummary(String counterVisitId);


    public RetailerSummaryData getRetailerSummary(String code);

    public CounterVisitMasterData getCounterVisitFormDetails(String counterVisitId);

    public DealerFleetListData getDealerFleetDetails(String counterVisitId);

    String getDJPPlanComplianceForSO();

    public InfluencerSummaryData getInfluencerSummary(String counterVisitId);

    //public NuvocoSiteListData getInfluencerDetails(String counterVisitId);

    //public SiteVisitFormData getLastSiteVisitFormData(String counterVisitId);

    boolean submitUnflagDealer(String counterVisitId, boolean isUnFlagged, String remarkForUnflag);
    //public List<CounterVisitData> getTodaysPlan();

    public String getRouteForId(String id);

    //public DropdownListData getListOfRoutes(List<String> subAreas);

    Double getTotalOrderGenerated(String siteCode, String counterVisitId);

  //  String submitSchemeDetails(String counterVisitId, SchemeDetailsData schemeDetailsData);

    void submitSchemeDocuments(String schemeID, MultipartFile[] files);

    public SalesHistoryData getSalesHistoryForDealer(String counterVisitId);

    //MarketVisitDetailsData fetchMarketVisitDetailsData(final String visitId);

    public List<MonthlySalesData> getLastSixMonthSalesForDealer(String counterVisitId);


    public Map<String, Object> counterVisitedForRoutes(String route);
}
