package com.nuvoco.core.dao;

import com.nuvoco.core.enums.OrderType;
import com.nuvoco.core.model.NuvocoCustomerModel;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.store.BaseStoreModel;

import java.util.List;
import java.util.Map;

public interface NuvocoOrderCountDao {

    SearchPageData<OrderModel> findOrdersListByStatusForSO(UserModel user, BaseStoreModel store, OrderStatus[] status, SearchPageData searchPageData, Boolean isCreditLimitBreached, String spApprovalFilter, Boolean approvalPending);

    SearchPageData<OrderEntryModel> findOrderEntriesListByStatusForSO(UserModel user, BaseStoreModel store, OrderStatus[] status, SearchPageData searchPageData, String spApprovalFilter);
    SearchPageData<OrderModel> findOrdersListByStatusForSO(UserModel user, BaseStoreModel store, OrderStatus[] status, SearchPageData searchPageData, String filter , String productName , OrderType orderType, Boolean isCreditLimitBreached, String spApprovalFilter, Boolean approvalPending);

    Integer findCreditBreachCountMTD(NuvocoCustomerModel dealer);
    SearchPageData<OrderEntryModel> findOrderEntriesListByStatusForSO(UserModel user, BaseStoreModel store, OrderStatus[] status, SearchPageData searchPageData, String filter ,String productName , OrderType orderType, String spApprovalFilter);
    Integer findDaysByConstraintName(String constraintName);

    void getApprovalLevelByUser(UserModel user, Map<String, Object> attr);


    Integer findOrdersByStatusForSO(UserModel user, OrderStatus[] status, Boolean approvalPending);
    Integer findOrderEntriesByStatusForSO(UserModel user, OrderStatus[] status);

    public Integer findCancelOrdersByStatusForSO(UserModel currentUser, OrderStatus[] status);

    public Integer findCancelOrderEntriesByStatusForSO(UserModel currentUser, OrderStatus[] status);

    SearchPageData<OrderEntryModel> findOrderEntriesListByStatusForEPOD(UserModel user, BaseStoreModel store, List<String> Status, SearchPageData searchPageData, String filter);


    SearchPageData<OrderModel> findCancelOrdersListByStatusForSO(UserModel user, BaseStoreModel store, OrderStatus[] status, SearchPageData searchPageData, String spApprovalFilter, String monthYear);


    SearchPageData<OrderModel> findCancelOrdersListByStatusForSO(UserModel user, BaseStoreModel store, OrderStatus[] status, SearchPageData searchPageData, String filter ,String productName , OrderType orderType, String spApprovalFilter, String monthYear);

    SearchPageData<OrderEntryModel> findCancelOrderEntriesListByStatusForSO(UserModel user, BaseStoreModel store, OrderStatus[] status, SearchPageData searchPageData, String spApprovalFilter, String monthYear);


    SearchPageData<OrderEntryModel> findCancelOrderEntriesListByStatusForSO(UserModel user, BaseStoreModel store, OrderStatus[] status, SearchPageData searchPageData, String filter ,String productName , OrderType orderType, String spApprovalFilter, String monthYear);

}
