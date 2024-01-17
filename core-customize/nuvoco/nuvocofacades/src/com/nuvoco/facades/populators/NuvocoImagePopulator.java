package com.nuvoco.facades.populators;

import com.nuvoco.facades.data.NuvocoImageData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import javax.annotation.Resource;
import java.util.Objects;

public class NuvocoImagePopulator implements Populator<MediaModel, NuvocoImageData> {


    @Resource
    private ConfigurationService configurationService;
    @Override
    public void populate(MediaModel source, NuvocoImageData target) throws ConversionException {
        if(Objects.nonNull(source)) {
            target.setFileName(source.getRealFileName());
            target.setUrl(createMediaUrl(source.getURL()));
        }
    }

    private String createMediaUrl(String url) {
        String baseUrl=configurationService.getConfiguration().getString("media.host.base.url");
        return baseUrl+url;
    }
}
