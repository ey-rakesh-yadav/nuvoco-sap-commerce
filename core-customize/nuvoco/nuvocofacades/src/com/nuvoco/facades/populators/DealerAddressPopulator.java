package com.nuvoco.facades.populators;

import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.CountryData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.core.model.user.AddressModel;
import org.apache.commons.lang.StringUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class DealerAddressPopulator  implements Populator<AddressModel, AddressData> {

    public CountryData populateCountry(CountryModel source)
    {
        CountryData countryData = new CountryData();
        if(source != null) {
            countryData.setIsocode(source.getIsocode());
            countryData.setName(source.getName());
        }
        return countryData;
    }

    @Override
    public void populate(AddressModel source, AddressData target) {
        target.setState(source.getState());
        target.setTaluka(source.getTaluka());
        target.setCity(source.getCity());
        target.setDistrict(source.getDistrict());
        target.setLongitude(source.getLongitude());
        target.setLatitude(source.getLatitude());
        target.setErpCity(source.getErpCity());
        target.setCountry(populateCountry(source.getCountry()));
        if(source.getCreationtime()!=null) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String strDate = dateFormat.format(source.getCreationtime());
            target.setCreatedDate(strDate);
        }
        if(source.getModifiedtime()!=null) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String strDate = dateFormat.format(source.getModifiedtime());
            target.setModifiedDate(strDate);
        }
        if(StringUtils.isNotBlank(source.getRetailerUid())){
            target.setRetailerUid(source.getRetailerUid());
        }

        if(StringUtils.isNotBlank(source.getRetailerName())){
            target.setRetailerName(source.getRetailerName());
        }
        if(null!= source.getIsPrimaryAddress()){
            target.setIsPrimaryAddress(source.getIsPrimaryAddress());
        }
        target.setCellphone(source.getCellphone());
        target.setEmail(source.getEmail());
        target.setAccountName(source.getAccountName());
        target.setErpId(source.getErpAddressId());
        target.setRetailerAddressPk(source.getRetailerAddressPk());
    }
}
