package com.nuvoco.facades.impl;

import com.nuvoco.core.services.NuvocoOrderService;
import com.nuvoco.facades.NuvocoOrderFacade;
import org.springframework.beans.factory.annotation.Autowired;

public class NuvocoOrderFacadeImpl implements NuvocoOrderFacade {


    @Autowired
    private NuvocoOrderService nuvocoOrderService;

    /**
     * @param orderStatus
     * @param approvalPending
     * @return
     */
    @Override
    public Integer getOrderCountByStatus(String orderStatus, Boolean approvalPending) {
        return nuvocoOrderService.getOrderCountByStatus(orderStatus, approvalPending);
    }

    /**
     * @param orderStatus
     * @return
     */
    @Override
    public Integer getOrderEntryCountByStatus(String orderStatus) {
        return nuvocoOrderService.getOrderEntryCountByStatus(orderStatus);
    }

    /**
     * @param orderStatus
     * @return
     */
    @Override
    public Integer getCancelOrderCountByStatus(String orderStatus) {
        return nuvocoOrderService.getCancelOrderCountByStatus(orderStatus);
    }

    /**
     * @param orderStatus
     * @return
     */
    @Override
    public Integer getCancelOrderEntryCountByStatus(String orderStatus) {
        return nuvocoOrderService.getCancelOrderEntryCountByStatus(orderStatus);
    }
}
