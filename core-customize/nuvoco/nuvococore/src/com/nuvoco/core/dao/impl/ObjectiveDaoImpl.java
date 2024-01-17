package com.nuvoco.core.dao.impl;

import com.nuvoco.core.dao.ObjectiveDao;
import com.nuvoco.core.model.ObjectiveModel;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;

import java.util.Collections;
import java.util.List;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

public class ObjectiveDaoImpl extends DefaultGenericDao<ObjectiveModel> implements ObjectiveDao {

    public ObjectiveDaoImpl() {
        super(ObjectiveModel._TYPECODE);
    }


    /**
     * @param objectiveId
     * @return
     */
    @Override
    public ObjectiveModel findByObjectiveId(String objectiveId) {
        validateParameterNotNullStandardMessage("objectiveId", objectiveId);
        final List<ObjectiveModel> objectiveModelListe = this.find(Collections.singletonMap(ObjectiveModel.OBJECTIVEID, objectiveId));
        if (objectiveModelListe.size() > 1)
        {
            throw new AmbiguousIdentifierException(
                    String.format("Found %d objectives with the objectiveId value: '%s', which should be unique", objectiveModelListe.size(),
                            objectiveId));
        }
        else
        {
            return objectiveModelListe.isEmpty() ? null : objectiveModelListe.get(0);
        }
    }

    /**
     * @return
     */
    @Override
    public List<ObjectiveModel> findAllObjective() {
        final List<ObjectiveModel> objectiveModelList = this.find();
        return objectiveModelList;
    }
}
