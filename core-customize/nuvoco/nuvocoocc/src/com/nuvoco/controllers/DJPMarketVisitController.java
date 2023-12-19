package com.nuvoco.controllers;


import com.nuvoco.annotation.ApiBaseSiteIdAndUserIdAndTerritoryParam;
import com.nuvoco.facades.DJPVisitFacade;
import com.nuvoco.facades.data.DJPRouteScoreData;
import com.nuvoco.facades.data.ObjectiveData;
import com.nuvoco.security.NuvocoSecuredAccessConstants;
import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping(value = "/{baseSiteId}/users/{userId}/marketVisit")
@ApiVersion("v2")
@Tag(name = "DJP Market Visit Controller")
public class DJPMarketVisitController extends NuvocooccController{


    @Autowired
    DJPVisitFacade djpVisitFacade;

    //new djp route score method
    @Secured({ NuvocoSecuredAccessConstants.ROLE_B2BADMINGROUP, NuvocoSecuredAccessConstants.ROLE_TRUSTED_CLIENT,NuvocoSecuredAccessConstants.ROLE_CUSTOMERGROUP, NuvocoSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/djpRoutes", method = RequestMethod.POST)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public List<DJPRouteScoreData> getDJPRoutes(@RequestParam String plannedDate, @RequestParam String subAreaMasterPk) {
        return djpVisitFacade.getDJPRouteScores(plannedDate, subAreaMasterPk);
    }

    @Secured({ NuvocoSecuredAccessConstants.ROLE_B2BADMINGROUP, NuvocoSecuredAccessConstants.ROLE_TRUSTED_CLIENT,NuvocoSecuredAccessConstants.ROLE_CUSTOMERGROUP, NuvocoSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/djpObjectives", method = RequestMethod.POST)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public List<ObjectiveData> getDJPAllObjective(@RequestParam String routeId, @RequestParam(required = false) String routeScoreId) {
        return djpVisitFacade.getDJPObjective(routeId, routeScoreId);
    }

}
