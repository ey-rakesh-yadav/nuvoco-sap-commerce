package com.nuvoco.controllers;


import com.nuvoco.facades.NuvocoCustomerFacade;
import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.webservicescommons.errors.exceptions.WebserviceValidationException;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdAndUserIdParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping(value = "/{baseSiteId}/users")
@ApiVersion("v2")
@Tag(name = "Nuvoco Users Management")
public class NuvocoUserController {

    protected static final String FILE_EMPTY_ERROR =  "File can not be empty";
    protected static final String INVALID_FILE_TYPE_ERROR =  "Please upload valid .png/.jpeg /.jpg /.pdf file only";
    protected static final String DOC_SIZE_MAX_UPLOAD_SIZE_ERROR  = "Please upload a file with size less than 10 MB";
    protected static final String PNG_MIME_TYPE    =  "image/png";
    protected static final String JPEG_MIME_TYPE   =  "image/jpeg";
    protected static final String JPG_MIME_TYPE    =  "image/jpg";
    protected static final long   FIVE_MB_IN_BYTES =   5242880;


@Autowired
private NuvocoCustomerFacade nuvocoCustomerFacade;

    @RequestMapping(value = "/{userId}/setProfilePicture", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.ACCEPTED)
    @Operation(operationId = "setProfilePicture", summary = "Set Profile Picture", description = "Set Profile Picture")
    @ApiBaseSiteIdAndUserIdParam
    @ResponseBody
    public ResponseEntity<String> setProfilePicture(@RequestParam("file") MultipartFile file ) {
        validateDocument(file);
        String url = nuvocoCustomerFacade.setProfilePicture(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(url);
    }


    private void validateDocument(final MultipartFile file)
    {
        final Map<String, String> params = new HashMap<>();
        params.put("FILE_EMPTY_ERROR",FILE_EMPTY_ERROR);
        params.put("INVALID_FILE_TYPE_ERROR",INVALID_FILE_TYPE_ERROR);
        params.put("DOC_SIZE_MAX_UPLOAD_SIZE_ERROR",DOC_SIZE_MAX_UPLOAD_SIZE_ERROR);

        final Errors errors = new MapBindingResult(params, "params");

        if(file.isEmpty()){
            errors.rejectValue(params.get("FILE_EMPTY_ERROR"),FILE_EMPTY_ERROR);
        }
        else if(!(PNG_MIME_TYPE.equalsIgnoreCase(file.getContentType())
                ||  JPEG_MIME_TYPE.equalsIgnoreCase(file.getContentType())
                || JPG_MIME_TYPE.equalsIgnoreCase(file.getContentType())
        )){

            errors.rejectValue(params.get("INVALID_FILE_TYPE_ERROR"),INVALID_FILE_TYPE_ERROR);

        }
        else if(file.getSize() > FIVE_MB_IN_BYTES){
            errors.rejectValue(params.get("DOC_SIZE_MAX_UPLOAD_SIZE_ERROR"),DOC_SIZE_MAX_UPLOAD_SIZE_ERROR);
        }
        if(errors.hasErrors()){
            throw new WebserviceValidationException(errors);
        }
    }

}
