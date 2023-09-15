package com.nuvoco.core.services.impl;

import com.nuvoco.core.constants.NuvocoCoreConstants;
import com.nuvoco.core.model.NuvocoCustomerModel;
import com.nuvoco.core.model.NuvocoUserModel;
import com.nuvoco.core.services.NuvocoCustomerService;
import de.hybris.platform.catalog.model.CatalogUnawareMediaModel;
import de.hybris.platform.core.model.media.MediaFolderModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.exceptions.ClassMismatchException;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public class NuvocoCustomerServiceImpl implements NuvocoCustomerService {

    private static final Logger LOG = Logger.getLogger(NuvocoCustomerServiceImpl.class);

    private static final String NOT_NUVOCO_USER_MESSAGE = "Current user is not an NUVOCO user";
    @Autowired
    private UserService userService;

    @Autowired
    private ModelService modelService;

    @Autowired
    private MediaService mediaService;

    @Autowired
    private FlexibleSearchService flexibleSearchService;
    /**
     * @param uid
     * @return
     */
    @Override
    public NuvocoCustomerModel getCustomerForUid(String uid) {
        NuvocoCustomerModel nuvocoCustomer = null;
        try {
            nuvocoCustomer = userService.getUserForUID(uid, NuvocoCustomerModel.class);
        }
        catch (final UnknownIdentifierException | ClassMismatchException e) {
            LOG.error("Failed to get Nuvoco Customer with uid : "+uid);
            LOG.error("Exception is : "+e.getMessage());
            throw new ModelNotFoundException("Failed to get Nuvoco Customer with uid : "+uid);
        }
        return nuvocoCustomer;
    }

    /**
     * @param file
     * @return
     */
    @Override
    public String saveProfilePicture(MultipartFile file) {
        final UserModel currentUser = userService.getCurrentUser();
        if(currentUser instanceof NuvocoUserModel || currentUser instanceof NuvocoCustomerModel){

            CatalogUnawareMediaModel profilePicture = createMediaFromFile(currentUser.getUid(),"ProfilePicture",file);
            profilePicture.setAltText("ProfilePicture");
            modelService.save(profilePicture);
            currentUser.setProfilePicture(profilePicture);
            modelService.save(currentUser);

            return currentUser.getProfilePicture().getURL();
        }
        else{
            throw new ModelNotFoundException(NOT_NUVOCO_USER_MESSAGE);
        }
    }

    /**
     * @param uid
     * @param documentType
     * @param file
     * @return
     */
    @Override
    public CatalogUnawareMediaModel createMediaFromFile(String uid, String documentType, MultipartFile file) {
        final String mediaCode = documentType.concat(NuvocoCoreConstants.UNDERSCORE_CHARACTER).concat(uid);

        final MediaFolderModel imageMediaFolder = mediaService.getFolder(NuvocoCoreConstants.IMAGE_MEDIA_FOLDER_NAME);
        CatalogUnawareMediaModel documentMedia = null;

        try{
            documentMedia = (CatalogUnawareMediaModel) mediaService.getMedia(mediaCode);
        }
        catch (AmbiguousIdentifierException ex){
            LOG.error("More than one media found with code : "+mediaCode);
            LOG.error("Removing duplicate media : "+mediaCode);
            CatalogUnawareMediaModel duplicateMedia = new CatalogUnawareMediaModel();
            duplicateMedia.setCode(mediaCode);
            List<CatalogUnawareMediaModel> duplicateMedias = flexibleSearchService.getModelsByExample(duplicateMedia);
            modelService.removeAll(duplicateMedias);
        }
        catch (UnknownIdentifierException uie){
            if(LOG.isDebugEnabled()){
                LOG.error("No Media found with code : "+mediaCode);
            }
        }
        finally {
            if(null == documentMedia){
                documentMedia = modelService.create(CatalogUnawareMediaModel.class);
                documentMedia.setCode(mediaCode);
            }
        }
        documentMedia.setFolder(imageMediaFolder);
        documentMedia.setMime(file.getContentType());
        documentMedia.setRealFileName(file.getName());
        modelService.save(documentMedia);
        try{
            mediaService.setStreamForMedia(documentMedia,file.getInputStream());
        }
        catch (IOException ioe){
            LOG.error("IO Exception occured while creating: "+documentType+ " ,for dealer with uid: "+uid);
        }

        return (CatalogUnawareMediaModel) mediaService.getMedia(mediaCode);
    }


}
