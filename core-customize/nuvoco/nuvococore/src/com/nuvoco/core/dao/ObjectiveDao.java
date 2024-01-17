package com.nuvoco.core.dao;

import com.nuvoco.core.model.ObjectiveModel;

import java.util.List;

public interface ObjectiveDao {

    ObjectiveModel findByObjectiveId(String objectiveId);

    List<ObjectiveModel> findAllObjective();
}
