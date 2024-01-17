package com.nuvoco.core.dao.impl;

import com.nuvoco.core.dao.GeographicalRegionDao;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public class GeographicalRegionDaoImpl implements GeographicalRegionDao {


    @Autowired
    FlexibleSearchService flexibleSearchService;

    @Autowired
    UserService userService;

    @Autowired
    BaseSiteService baseSiteService;

    /**
     * @param geographicalState
     * @param district
     * @param taluka
     * @param erpCity
     * @return
     */
    @Override
    public List<String> getBusinessState(String geographicalState, String district, String taluka, String erpCity) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT DISTINCT {state} FROM {GeographicalMaster} WHERE {geographicalState}=?geographicalState AND {district}=?district AND {taluka}=?taluka AND {erpCity}=?erpCity ");
        params.put("geographicalState", geographicalState);
        params.put("district", district);
        params.put("taluka", taluka);
        params.put("erpCity", erpCity);
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(String.class));
        query.addQueryParameters(params);
        final SearchResult<String> searchResult = flexibleSearchService.search(query);
        return searchResult.getResult();
    }

    /**
     * @param state
     * @return
     */
    @Override
    public List<String> findAllLpSourceDistrict(String state) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT DISTINCT {g.district} FROM {GeographicalMaster as g join DestinationSourceMaster as d on {d.destinationState}={g.geographicalState} and {d.destinationDistrict}={g.district}  and {d.destinationCity}={g.erpCity}  and  {d.destinationTaluka}={g.taluka} } WHERE {d.brand}=?brand and {g.geographicalState}=?state  ");
        params.put("state", state);
        params.put("brand", baseSiteService.getCurrentBaseSite());
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(String.class));
        query.addQueryParameters(params);
        final SearchResult<String> searchResult = flexibleSearchService.search(query);
        return searchResult.getResult();
    }

    /**
     * @param state
     * @param district
     * @return
     */
    @Override
    public List<String> findAllLpSourceTaluka(String state, String district) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT DISTINCT {g.taluka} FROM {GeographicalMaster as g join DestinationSourceMaster as d on {d.destinationState}={g.geographicalState} and {d.destinationDistrict}={g.district}  and {d.destinationCity}={g.erpCity} and  {d.destinationTaluka}={g.taluka} } WHERE {d.brand}=?brand and {g.geographicalState}=?state AND {g.district}=?district ");
        params.put("state", state);
        params.put("district", district);
        params.put("brand", baseSiteService.getCurrentBaseSite());
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(String.class));
        query.addQueryParameters(params);
        final SearchResult<String> searchResult = flexibleSearchService.search(query);
        return searchResult.getResult();
    }

    /**
     * @param state
     * @param district
     * @param taluka
     * @return
     */
    @Override
    public List<String> findAllLpSourceErpCity(String state, String district, String taluka) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT DISTINCT {erpCity} FROM {GeographicalMaster as g join DestinationSourceMaster as d on {d.destinationState}={g.geographicalState} and {d.destinationDistrict}={g.district}  and {d.destinationCity}={g.erpCity} and  {d.destinationTaluka}={g.taluka} } WHERE {d.brand}=?brand and {g.geographicalState}=?state AND {g.district}=?district AND {g.taluka}=?taluka ");
        params.put("state", state);
        params.put("district", district);
        params.put("taluka", taluka);
        params.put("brand", baseSiteService.getCurrentBaseSite());
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(String.class));
        query.addQueryParameters(params);
        final SearchResult<String> searchResult = flexibleSearchService.search(query);
        return searchResult.getResult();
    }

    /**
     * @param customerCode
     * @return
     */
    @Override
    public List<String> findUserState(String customerCode) {
        List<String> result = new ArrayList<String>();
        B2BCustomerModel customer = null;
        if(customerCode!=null) {
            customer = (B2BCustomerModel) userService.getUserForUID(customerCode);
        }
        else {
            customer = (B2BCustomerModel) userService.getCurrentUser();
        }
        result.add(customer.getState());
        return result;
    }
}
