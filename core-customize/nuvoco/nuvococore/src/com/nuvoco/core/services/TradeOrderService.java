package com.nuvoco.core.services;

import com.nuvoco.core.enums.OrderType;

import java.util.List;

public interface TradeOrderService {

    List<OrderType> listAllTradeOrderType();
}
