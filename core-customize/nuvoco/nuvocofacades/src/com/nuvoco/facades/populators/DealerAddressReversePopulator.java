package com.nuvoco.facades.populators;

import de.hybris.platform.commercefacades.user.converters.populator.AddressReversePopulator;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.CountryData;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Resource;

public class DealerAddressReversePopulator extends AddressReversePopulator {


    @Override
    public void populate(final AddressData addressData, final AddressModel addressModel) throws ConversionException
    {

        final CountryData countrydata = new CountryData();
        countrydata.setIsocode("IN");
        countrydata.setName("India");
        addressData.setCountry(countrydata);

        super.populate(addressData, addressModel);

        addressModel.setState(addressData.getState());
        addressModel.setTaluka(addressData.getTaluka());
        addressModel.setCity(addressData.getCity());
        addressModel.setDistrict(addressData.getDistrict());
        if(addressData.getErpId()!=null) {
            addressModel.setErpAddressId(addressData.getErpId());
        }
        addressModel.setLongitude(addressData.getLongitude());
        addressModel.setLatitude(addressData.getLatitude());

        addressModel.setErpCity(addressData.getErpCity());
        if(StringUtils.isNotBlank(addressData.getRetailerUid())){
            addressModel.setRetailerUid(addressData.getRetailerUid());
        }
        if(StringUtils.isNotBlank(addressData.getRetailerName())){
            addressModel.setRetailerName(addressData.getRetailerName());
        }
        addressModel.setCellphone(addressData.getCellphone());
        addressModel.setEmail(addressData.getEmail());
        addressModel.setAccountName(addressData.getAccountName());
        addressModel.setRetailerAddressPk(addressData.getRetailerAddressPk());
    }
}
