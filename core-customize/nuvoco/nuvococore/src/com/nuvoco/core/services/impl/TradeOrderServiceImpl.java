package com.nuvoco.core.services.impl;

import com.nuvoco.core.enums.OrderType;
import com.nuvoco.core.services.TradeOrderService;
import de.hybris.platform.enumeration.EnumerationService;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TradeOrderServiceImpl implements TradeOrderService {


    @Resource
    EnumerationService enumerationService;
    /**
     * @return
     */
    @Override
    public List<OrderType> listAllTradeOrderType() {
        List<OrderType> tradeOrderList = enumerationService.getEnumerationValues(OrderType.class);
        return Objects.nonNull(tradeOrderList) ? tradeOrderList : Collections.emptyList();
    }
}
