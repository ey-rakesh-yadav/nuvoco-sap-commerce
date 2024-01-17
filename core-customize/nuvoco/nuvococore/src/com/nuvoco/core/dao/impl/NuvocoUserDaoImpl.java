package com.nuvoco.core.dao.impl;

import com.nuvoco.core.dao.NuvocoUserDao;
import com.nuvoco.core.model.NuvocoCustomerModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class NuvocoUserDaoImpl implements NuvocoUserDao {


    @Autowired
    FlexibleSearchService flexibleSearchService;
    /**
     * @param pk
     * @return
     */
    @Override
    public AddressModel getAddressByPk(String pk) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("pk", pk);

        String searchQuery = "select {a.pk} from {Address as a} where {a.pk}=?pk ";
        final FlexibleSearchQuery query = new FlexibleSearchQuery(searchQuery);
        query.addQueryParameters(params);
        final SearchResult<AddressModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;

    }

    /**
     * @param retailerAddressPk
     * @param customer
     * @return
     */
    @Override
    public AddressModel getDealerAddressByRetailerPk(String retailerAddressPk, NuvocoCustomerModel customer) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("retailerAddressPk", retailerAddressPk);
        params.put("owner", customer);

        String searchQuery = "select {a.pk} from {Address as a} where {a.retailerAddressPk}=?retailerAddressPk and {a.owner}=?owner ";
        final FlexibleSearchQuery query = new FlexibleSearchQuery(searchQuery);
        query.addQueryParameters(params);
        final SearchResult<AddressModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;

    }
}
