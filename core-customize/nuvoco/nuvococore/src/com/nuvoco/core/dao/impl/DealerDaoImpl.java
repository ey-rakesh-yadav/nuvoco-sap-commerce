package com.nuvoco.core.dao.impl;

import com.nuvoco.core.dao.DealerDao;
import com.nuvoco.core.model.NuvocoCustomerModel;
import com.nuvoco.core.model.ReceiptAllocaltionModel;
import com.nuvoco.core.model.RetailerRecAllocateModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public class DealerDaoImpl implements DealerDao {


    private static final Logger LOGGER = Logger.getLogger(DealerDaoImpl.class);

    @Autowired
    FlexibleSearchService flexibleSearchService;

    /**
     * @param dealerCode
     * @return
     */
    @Override
    public List<List<Integer>> getDealerTotalAllocation(NuvocoCustomerModel dealerCode) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT SUM({stockAvlForInfluencer}), SUM({stockAvlForRetailer}) FROM {ReceiptAllocaltion} WHERE {dealerCode}=?dealerCode");
        params.put("dealerCode", dealerCode);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Integer.class,Integer.class));
        query.addQueryParameters(params);
        final SearchResult<List<Integer>> searchResult = flexibleSearchService.search(query);
        return searchResult.getResult()!=null?searchResult.getResult(): Collections.emptyList();
    }

    /**
     * @param productCode
     * @param dealerCode
     * @return
     */
    @Override
    public ReceiptAllocaltionModel getDealerAllocation(ProductModel productCode, NuvocoCustomerModel dealerCode) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {ReceiptAllocaltion} WHERE {dealerCode}=?dealerCode AND {product}=?product");

        params.put("dealerCode", dealerCode);
        params.put("product", productCode);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(ReceiptAllocaltionModel.class));
        query.addQueryParameters(params);
        final SearchResult<ReceiptAllocaltionModel> searchResult = flexibleSearchService.search(query);
        LOGGER.info("In DealerDao:getDealerAllocation method--> query:::" + builder.toString() + ":::Product Code:::" + productCode.getPk().toString() + ":::Dealer Code:::" + dealerCode.getPk().toString());
        LOGGER.info("In DealerDao:getDealerAllocation method--> Show the result of the query:::" + searchResult.getResult());
        if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : null;
        else
            return null;
    }

    /**
     * @param productCode
     * @param dealerCode
     * @return
     */
    @Override
    public RetailerRecAllocateModel getRetailerAllocation(ProductModel productCode, NuvocoCustomerModel dealerCode) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {RetailerRecAllocate} WHERE {dealerCode}=?dealerCode AND {product}=?product");

        params.put("dealerCode", dealerCode);
        params.put("product", productCode);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(RetailerRecAllocateModel.class));
        query.addQueryParameters(params);
        final SearchResult<RetailerRecAllocateModel> searchResult = flexibleSearchService.search(query);
        if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : null;
        else
            return null;
    }
}
