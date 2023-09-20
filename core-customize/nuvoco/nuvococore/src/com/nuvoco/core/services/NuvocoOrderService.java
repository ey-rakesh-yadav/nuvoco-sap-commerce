package com.nuvoco.core.services;

public interface NuvocoOrderService {

    Integer getOrderCountByStatus(String orderStatus,Boolean approvalPending);

    Integer getOrderEntryCountByStatus(String orderStatus);

    public Integer getCancelOrderCountByStatus(String orderStatus);

    public Integer getCancelOrderEntryCountByStatus(String orderStatus);
}
