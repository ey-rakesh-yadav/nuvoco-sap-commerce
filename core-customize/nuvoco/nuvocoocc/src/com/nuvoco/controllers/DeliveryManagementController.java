package com.nuvoco.controllers;

import com.nuvoco.facades.DeliverySlotsFacade;
import com.nuvoco.facades.NuvocoB2BOrderfacade;
import com.nuvoco.facades.data.DeliverySlotMasterData;
import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdAndUserIdParam;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.PermitAll;
import java.util.List;

@Controller
@RequestMapping(value = "/{baseSiteId}/delivery")
@ApiVersion("v2")
@Tag(name = "Delivery Management")
@PermitAll
public class DeliveryManagementController extends NuvocooccController{



@Autowired
private DeliverySlotsFacade deliverySlotsFacade;

 @Secured({ "ROLE_CUSTOMERGROUP", "ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
@RequestMapping(value = "/getDeliverySlots", method = RequestMethod.GET)
@ResponseBody
@ApiBaseSiteIdAndUserIdParam
public List<DeliverySlotMasterData> getDeliverySlotList()
{
    return deliverySlotsFacade.getDeliverySlotList();
}

}
