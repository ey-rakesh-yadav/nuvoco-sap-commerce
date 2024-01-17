package com.nuvoco.core.services.impl;

import com.nuvoco.core.constants.NuvocoCoreConstants;
import com.nuvoco.core.model.NuvocoCustomerModel;
import com.nuvoco.core.model.NuvocoUserModel;
import com.nuvoco.core.services.NuvocoCustomerService;
import com.nuvoco.core.services.TerritoryManagementService;
import de.hybris.platform.catalog.model.CatalogUnawareMediaModel;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.core.model.media.MediaFolderModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.exceptions.ClassMismatchException;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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
    @Autowired
    private BaseSiteService baseSiteService;

    @Autowired
    private TerritoryManagementService territoryManagementService;
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
     * @param filteredAddressList
     * @return
     */
    @Override
    public List<AddressData> filterAddressByLpSource(List<AddressData> filteredAddressList) {
        if(filteredAddressList!=null && !filteredAddressList.isEmpty()) {
            List<String> stateDistrictTalukaList = filteredAddressList.stream()
                    .filter(data-> data.getState()!=null && data.getDistrict()!=null && data.getErpCity()!=null)
                    .map(data-> data.getState().toUpperCase() + data.getDistrict().toUpperCase() + data.getTaluka().toUpperCase() + data.getErpCity().toUpperCase())
                    .distinct()
                    .collect(Collectors.toList());
            List<String> lpStateDistrictTalukaList = filterAddress(stateDistrictTalukaList);
            if(lpStateDistrictTalukaList!=null && !lpStateDistrictTalukaList.isEmpty()) {
                filteredAddressList = filteredAddressList.stream()
                        .filter(data1-> {
                            if(data1.getState()!=null && data1.getDistrict()!=null && data1.getTaluka()!=null && data1.getErpCity()!=null) {
                                String s = data1.getState().toUpperCase() + data1.getDistrict().toUpperCase()  + data1.getTaluka().toUpperCase() + data1.getErpCity().toUpperCase();
                                if(lpStateDistrictTalukaList.contains(s))
                                    return true;
                                else
                                    return false;
                            }
                            else
                            {
                                return false;
                            }
                        } ).collect(Collectors.toList());
            }
            else {
                return Collections.emptyList();
            }
        }
        return filteredAddressList;
    }

    /**
     * @param addressId
     */
    @Override
    public void triggerShipToPartyAddress(String addressId) {
     //
    }

    /**
     * @param user
     * @return
     */
    @Override
    public List<NuvocoCustomerModel> getDealersList(NuvocoUserModel user) {
        List<NuvocoCustomerModel> dealerList = territoryManagementService.getDealersForSubArea();
        return dealerList;
    }


    public List<String> filterAddress(List<String> stateDistrictTalukaList) {
        List<String> output = new ArrayList<String>();
        if(stateDistrictTalukaList!=null && !stateDistrictTalukaList.isEmpty()) {
            final Map<String, Object> params = new HashMap<String, Object>();
            params.put("stateDistrictTaluka", stateDistrictTalukaList);
            params.put("brand", baseSiteService.getCurrentBaseSite());
            String searchQuery = "select CONCAT(Upper({l.destinationState}),upper({l.destinationDistrict}),upper({l.destinationTaluka}),upper({l.destinationCity})) from {DestinationSourceMaster as l} where {brand}=?brand and CONCAT(Upper({l.destinationState}),upper({l.destinationDistrict}),upper({l.destinationTaluka}),upper({l.destinationCity})) in (?stateDistrictTaluka) group by  CONCAT(Upper({l.destinationState}),upper({l.destinationDistrict}),upper({l.destinationTaluka}),upper({l.destinationCity})) ";
            final FlexibleSearchQuery query = new FlexibleSearchQuery(searchQuery);
            query.setResultClassList(Collections.singletonList(String.class));
            query.addQueryParameters(params);
            final SearchResult<String> searchResult = flexibleSearchService.search(query);
            if(searchResult.getResult()!=null ) {
                output = searchResult.getResult();
            }
        }
        return output;
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
