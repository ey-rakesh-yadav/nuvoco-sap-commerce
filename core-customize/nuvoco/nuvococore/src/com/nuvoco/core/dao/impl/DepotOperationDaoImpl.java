package com.nuvoco.core.dao.impl;

import com.nuvoco.core.dao.DepotOperationDao;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

public class DepotOperationDaoImpl implements DepotOperationDao {


    @Autowired
    FlexibleSearchService flexibleSearchService;
    private static final String FIND_DEPOT_ADDRESS_BY_PK = "SELECT {address:pk} FROM {Address AS address} WHERE {address:pk}=?pk " ;
    /**
     * @param pk
     * @return
     */
    @Override
    public AddressModel findDepotAddressByPk(String pk) {
        final Map<String, Object> queryParams = new HashMap<String, Object>();
        queryParams.put("pk", pk);
        final SearchResult<AddressModel> result = flexibleSearchService.search(FIND_DEPOT_ADDRESS_BY_PK,
                queryParams);
        return result.getResult().get(0);
    }
}
