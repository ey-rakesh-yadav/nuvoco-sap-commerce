package com.nuvoco.facades.impl;

import com.nuvoco.core.enums.OrderType;
import com.nuvoco.core.services.TradeOrderService;
import com.nuvoco.facades.TradeOrderFacade;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class TradeOrderFacadeImpl implements TradeOrderFacade {


    @Autowired
    TradeOrderService tradeOrderService;

    /**
     * @return
     */
    @Override
    public List<OrderType> listAllTradeOrderType() {
        List<OrderType> tradeOrderlist = tradeOrderService.listAllTradeOrderType();
        return tradeOrderlist;
    }
}
