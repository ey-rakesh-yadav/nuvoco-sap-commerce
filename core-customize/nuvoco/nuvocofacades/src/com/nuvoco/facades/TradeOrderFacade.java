package com.nuvoco.facades;

import com.nuvoco.core.enums.OrderType;

import java.util.List;

public interface TradeOrderFacade {

    List<OrderType> listAllTradeOrderType();
}
