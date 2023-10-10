package com.nuvoco.core.dao.impl;

import com.nuvoco.core.dao.NuvocoSiteMessageDao;
import com.nuvoco.core.enums.NotificationStatus;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.notificationservices.model.SiteMessageForCustomerModel;
import de.hybris.platform.notificationservices.model.SiteMessageModel;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;

import java.util.*;

import de.hybris.platform.notificationservices.dao.impl.DefaultSiteMessageDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchParameter;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchService;
import org.springframework.beans.factory.annotation.Autowired;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

public class NuvocoSiteMessageDaoImpl extends DefaultSiteMessageDao implements NuvocoSiteMessageDao {




    private static final String SEARCH_MESSAGE = "select {smc:pk} from {" + SiteMessageForCustomerModel._TYPECODE
            + " as smc left join " + SiteMessageModel._TYPECODE
            + " as sm on {smc:message} = {sm:pk}} where {smc:customer} = ?customer AND {sm:expiryDate} >= ?currentDate AND {sm:status} = ?status ORDER BY {smc:creationtime} desc";

   @Autowired
   private PaginatedFlexibleSearchService paginatedFlexibleSearchService;

    @Autowired
    private Map<String, String> siteMessageSortCodeToQueryAlias;





    @Override
    public SearchPageData<SiteMessageForCustomerModel> findPaginatedMessages(CustomerModel customer, SearchPageData searchPageData) {
        final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
        parameter.setSearchPageData(searchPageData);

        Calendar cal = Calendar.getInstance();
        Date currentDate=cal.getTime();


        final FlexibleSearchQuery query = new FlexibleSearchQuery(SEARCH_MESSAGE);
        query.addQueryParameter("customer", customer);
        query.addQueryParameter("currentDate",currentDate);
        query.addQueryParameter("status", NotificationStatus.UNREAD);
        parameter.setFlexibleSearchQuery(query);

        parameter.setSortCodeToQueryAlias(siteMessageSortCodeToQueryAlias);

        return paginatedFlexibleSearchService.search(parameter);
    }




    /**
     * @param siteMessageId
     * @return
     */
    @Override
    public SiteMessageModel findSiteMessageById(String siteMessageId) {
        validateParameterNotNullStandardMessage("siteMessageId", siteMessageId);
        final List<SiteMessageModel> siteMessageModels = this.find(Collections.singletonMap(SiteMessageModel.PK, siteMessageId));
        if (siteMessageModels.size() > 1)
        {
            throw new AmbiguousIdentifierException(
                    String.format("Found %d sitemessage with the siteMessageId value: '%s', which should be unique", siteMessageModels.size(),
                            siteMessageId));
        }
        else
        {
            return siteMessageModels.isEmpty() ? null : siteMessageModels.get(0);
        }
    }
}
