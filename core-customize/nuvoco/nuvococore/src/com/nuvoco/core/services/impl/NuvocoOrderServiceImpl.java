package com.nuvoco.core.services.impl;

import com.nuvoco.core.constants.NuvocoCoreConstants;
import com.nuvoco.core.dao.NuvocoOrderCountDao;
import com.nuvoco.core.services.NuvocoOrderService;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.Set;

public class NuvocoOrderServiceImpl implements NuvocoOrderService {

    @Autowired
    UserService userService;

    @Autowired
    NuvocoOrderCountDao nuvocoOrderCountDao;

    @Autowired
    ConfigurationService configurationService;

    /**
     * @param orderStatus
     * @param approvalPending
     * @return
     */
    @Override
    public Integer getOrderCountByStatus(String orderStatus, Boolean approvalPending) {
        final UserModel currentUser = userService.getCurrentUser();
        String statues = validateAndMapOrderStatuses(orderStatus);
        final Set<OrderStatus> statusSet = extractOrderStatuses(statues);
        return nuvocoOrderCountDao.findOrdersByStatusForSO(currentUser,statusSet.toArray(new OrderStatus[statusSet.size()]),approvalPending);

    }

    /**
     * @param orderStatus
     * @return
     */
    @Override
    public Integer getOrderEntryCountByStatus(String orderStatus) {
        final UserModel currentUser = userService.getCurrentUser();
        String statues = validateAndMapOrderStatuses(orderStatus);
        final Set<OrderStatus> statusSet = extractOrderStatuses(statues);
        return nuvocoOrderCountDao.findOrderEntriesByStatusForSO(currentUser,statusSet.toArray(new OrderStatus[statusSet.size()]));

    }

    /**
     * @param orderStatus
     * @return
     */
    @Override
    public Integer getCancelOrderCountByStatus(String orderStatus) {
        final UserModel currentUser = userService.getCurrentUser();
        String statues = validateAndMapOrderStatuses(orderStatus);
        final Set<OrderStatus> statusSet = extractOrderStatuses(statues);
        return nuvocoOrderCountDao.findCancelOrdersByStatusForSO(currentUser,statusSet.toArray(new OrderStatus[statusSet.size()]));
    }

    /**
     * @param orderStatus
     * @return
     */
    @Override
    public Integer getCancelOrderEntryCountByStatus(String orderStatus) {
        final UserModel currentUser = userService.getCurrentUser();
        String statues = validateAndMapOrderStatuses(orderStatus);
        final Set<OrderStatus> statusSet = extractOrderStatuses(statues);
        return nuvocoOrderCountDao.findCancelOrderEntriesByStatusForSO(currentUser,statusSet.toArray(new OrderStatus[statusSet.size()]));
    }

    protected Set<OrderStatus> extractOrderStatuses(final String statuses)
    {
       // final String[] statusesStrings = statuses.split(NuvocoCoreConstants.ORDER.ENUM_VALUES_SEPARATOR);
       //commented for poc
        final Set<OrderStatus> statusesEnum = new HashSet<>();
        /*for (final String status : statusesStrings)
        {
            statusesEnum.add(OrderStatus.valueOf(status));
        }*/

        statusesEnum.add(OrderStatus.valueOf(statuses));
        return statusesEnum;
    }

    public String validateAndMapOrderStatuses(final String inputStatus){
        String statuses;
        switch(inputStatus){
            case NuvocoCoreConstants.ORDER.PENDING_FOR_APPROVAL_STATUS:
                statuses = configurationService.getConfiguration().getString(NuvocoCoreConstants.ORDER.PENDING_FOR_APPROVAL_STATUS_MAPPING);
                break;

            case NuvocoCoreConstants.ORDER.VEHICLE_ARRIVAL_CONFIRMATION_STATUS:
                statuses = configurationService.getConfiguration().getString(NuvocoCoreConstants.ORDER.VEHICLE_ARRIVAL_CONFIRMATION_STATUS_MAPPING);
                break;

            case NuvocoCoreConstants.ORDER.WAITING_FOR_DISPATCH_STATUS:
                statuses = configurationService.getConfiguration().getString(NuvocoCoreConstants.ORDER.WAITING_FOR_DISPATCH_STATUS_MAPPING);
                break;

            case NuvocoCoreConstants.ORDER.TO_BE_DELIVERED_BY_TODAY_STATUS:
                statuses = configurationService.getConfiguration().getString(NuvocoCoreConstants.ORDER.TO_BE_DELIVERED_BY_TODAY_STATUS_MAPPING);
                break;

            case NuvocoCoreConstants.ORDER.ORDER_CANCELLATION_STATUS:
                statuses = configurationService.getConfiguration().getString(NuvocoCoreConstants.ORDER.ORDER_CANCELLATION_STATUS_MAPPING);
                break;

            case NuvocoCoreConstants.ORDER.ORDER_LINE_CANCELLATION_STATUS:
                statuses = configurationService.getConfiguration().getString(NuvocoCoreConstants.ORDER.ORDER_LINE_CANCELLATION_STATUS_MAPPING);
                break;

            default :
                statuses = inputStatus;
        }
        return statuses;
    }

}
