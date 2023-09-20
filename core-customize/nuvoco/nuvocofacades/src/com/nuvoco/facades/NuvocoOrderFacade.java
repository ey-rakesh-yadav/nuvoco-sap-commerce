package com.nuvoco.facades;

public interface NuvocoOrderFacade {

    public Integer getOrderCountByStatus(String orderStatus, Boolean approvalPending);

    public Integer getOrderEntryCountByStatus(String orderStatus);

    public Integer getCancelOrderCountByStatus(String orderStatus);
    public Integer getCancelOrderEntryCountByStatus(String orderStatus);
}
