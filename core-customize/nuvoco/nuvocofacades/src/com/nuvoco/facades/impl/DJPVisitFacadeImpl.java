package com.nuvoco.facades.impl;

import com.nuvoco.core.dao.DJPRouteScoreDao;
import com.nuvoco.core.dao.DJPVisitDao;
import com.nuvoco.core.model.*;
import com.nuvoco.core.services.DJPVisitService;
import com.nuvoco.facades.DJPVisitFacade;
import com.nuvoco.facades.data.*;
import com.nuvoco.facades.visit.data.InfluencerSummaryData;
import de.hybris.platform.storelocator.data.RouteData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

public class DJPVisitFacadeImpl implements DJPVisitFacade {


    @Autowired
    DJPVisitService djpVisitService;

    @Autowired
    DJPRouteScoreDao djpRouteScoreDao;

    @Autowired
    DJPVisitDao djpVisitDao;


    /**
     * @param plannedDate
     * @param subAreaMasterPk
     * @return
     */
    @Override
    public List<DJPRouteScoreData> getDJPRouteScores(String plannedDate, String subAreaMasterPk) {
        List<DJPRouteScoreData> dataList = new ArrayList<DJPRouteScoreData>();
        List<DJPRouteScoreMasterModel> reccomendedRoutes = new ArrayList<DJPRouteScoreMasterModel>();
        List<RouteMasterModel> recommendedRoute = new ArrayList<>();
        List<RouteMasterModel> routeMasterList =  djpVisitService.getRouteMasterList(plannedDate, subAreaMasterPk, recommendedRoute);
        if(routeMasterList!=null && !routeMasterList.isEmpty()) {
            Collection<DJPRouteScoreMasterModel> modelList = djpVisitService.getDJPRouteScores(plannedDate, reccomendedRoutes, routeMasterList.get(0).getSubAreaMaster().getDistrict(), routeMasterList.get(0).getSubAreaMaster().getTaluka());
            routeMasterList.forEach(model -> {
                DJPRouteScoreData data = new DJPRouteScoreData();
                Optional<DJPRouteScoreMasterModel> djpRouteModelopt = modelList.stream()
                        .filter(djpRoute-> djpRoute.getRoute()!=null && djpRoute.getRoute().equals(model)).findAny();
                if(djpRouteModelopt.isPresent()) {
                    DJPRouteScoreMasterModel djpRoutesModel = djpRouteModelopt.get();
                    data.setId(djpRoutesModel.getId());
                    data.setRouteScore(djpRoutesModel.getRoutesScore());
                }

                data.setRoute(model.getRouteId());
                data.setRouteName(model.getRouteName()!=null ? model.getRouteName() : model.getRouteId());
                Boolean recommended = Boolean.FALSE;
                if(recommendedRoute.contains(model)) {
                    recommended = true;
                }
                data.setRecommended(recommended);
                dataList.add(data);
            });
        }
        return dataList;
    }

    /**
     * @param routeId
     * @param routeScoreId
     * @return
     */
    @Override
    public List<ObjectiveData> getDJPObjective(String routeId, String routeScoreId) {
        List<ObjectiveData> dataList = new ArrayList<ObjectiveData>();
        Collection<ObjectiveModel> modelList = djpVisitService.getAllObjective();

        if(routeScoreId!=null) {
            DJPRouteScoreMasterModel djpRouteScore = djpRouteScoreDao.findByRouteScoreId(routeScoreId);
            if(djpRouteScore!=null) {
                ObjectiveModel recommendedObj1 = djpVisitDao.findOjectiveById(djpRouteScore.getRecommendedObj1());
                if(recommendedObj1!=null) {
                    populateObjectiveDate(recommendedObj1, true, dataList);
                    modelList = modelList.stream().filter(obj->!obj.equals(recommendedObj1)).collect(Collectors.toList());
                }
                ObjectiveModel recommendedObj2 = djpVisitDao.findOjectiveById(djpRouteScore.getRecommendedObj2());
                if(recommendedObj2!=null) {
                    populateObjectiveDate(recommendedObj2, true, dataList);
                    modelList = modelList.stream().filter(obj->!obj.equals(recommendedObj2)).collect(Collectors.toList());
                }
            }
        }
        if(modelList!=null) {
            modelList.forEach(model -> {
                populateObjectiveDate(model, false, dataList);
            });
        }
        return dataList;
    }

    private void populateObjectiveDate(ObjectiveModel model, Boolean recommended, List<ObjectiveData> dataList) {
        ObjectiveData data = new ObjectiveData();
        data.setId(model.getObjectiveId());
        data.setName(model.getObjectiveName());
        data.setRecommended(recommended);
        dataList.add(data);
    }

    @Override
    public boolean submitMarketMappingDetails(CounterVisitMasterData data) {
        return false;
    }

    @Override
    public boolean submitFlagDealer(String counterVisitId, boolean isFlagged, String remarkForFlag) {
        return false;
    }

    /**
     * @return
     */
    @Override
    public List<TruckModelData> getAllTrucks() {
        List<TruckModelMasterModel> modelList =  djpVisitService.findAllTrucks();
        List<TruckModelData> dataList = new ArrayList<TruckModelData>();
        modelList.stream().forEach(truck->
        {
            TruckModelData data = new TruckModelData();
            data.setTruckModel(truck.getTruckModel());
            data.setCapacity(truck.getCapacity());
            data.setVehicleMake(truck.getVehicleMake());
            data.setVehicleType(String.valueOf(truck.getVehicleType()));
            //data.setCount(truck.getCount());
            dataList.add(data);
        });
        return dataList;
    }

    @Override
    public boolean submitTruckFleetDetails(DealerFleetListData dealerFleetListData, String counterVisitId) {
        return false;
    }


    @Override
    public boolean startDjpVisit(String visitId) {
        return false;
    }

    @Override
    public boolean completeDjpVisit(String visitId) {
        return false;
    }

    @Override
    public boolean startCounterVisit(String counterVisitId) {
        return false;
    }

    @Override
    public long completeCounterVisit(String counterVisitId) {
        return 0;
    }

    @Override
    public Long getCountOfCounterNotVisited() {
        return null;
    }

    @Override
    public Long getCountOfTotalJouneyPlanned() {
        return null;
    }

    @Override
    public Map<String, Double> getAvgTimeSpent() {
        return null;
    }

    @Override
    public List<RouteData> getRoutesForSalesofficer() {
        return null;
    }

    @Override
    public Map<String, String> getLastSixCounterVisitDates(String customerId) {
        return null;
    }

    @Override
    public Map<String, Object> counterVisitedForSelectedRoutes(String routeScoreId) {
        return null;
    }

    @Override
    public String getLastVisitDate(String counterVisitId) {
        return null;
    }

    @Override
    public Integer getVisitCountMTD(String counterVisitId) {
        return null;
    }


    @Override
    public RetailerSummaryData getRetailerSummary(String code) {
        return null;
    }

    @Override
    public CounterVisitMasterData getCounterVisitFormDetails(String counterVisitId) {
        return null;
    }

    @Override
    public DealerFleetListData getDealerFleetDetails(String counterVisitId) {
        return null;
    }

    @Override
    public String getDJPPlanComplianceForSO() {
        return null;
    }

    @Override
    public InfluencerSummaryData getInfluencerSummary(String counterVisitId) {
        return null;
    }

    @Override
    public boolean submitUnflagDealer(String counterVisitId, boolean isUnFlagged, String remarkForUnflag) {
        return false;
    }

    @Override
    public String getRouteForId(String id) {
        return null;
    }

    @Override
    public Double getTotalOrderGenerated(String siteCode, String counterVisitId) {
        return null;
    }

    @Override
    public void submitSchemeDocuments(String schemeID, MultipartFile[] files) {

    }

    @Override
    public SalesHistoryData getSalesHistoryForDealer(String counterVisitId) {
        return null;
    }

    @Override
    public List<MonthlySalesData> getLastSixMonthSalesForDealer(String counterVisitId) {
        return djpVisitService.getLastSixMonthSalesForDealer(counterVisitId);
    }

    @Override
    public Map<String, Object> counterVisitedForRoutes(String route) {
        return null;
    }
}
