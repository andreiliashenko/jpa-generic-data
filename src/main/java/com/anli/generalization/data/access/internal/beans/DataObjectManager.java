package com.anli.generalization.data.access.internal.beans;

import com.anli.generalization.data.access.beans.GenericManager;
import com.anli.generalization.data.entities.jpa.JpaDataObject;
import javax.inject.Named;

@Named
public class DataObjectManager extends GenericManager<JpaDataObject> {

    @Override
    protected Class<JpaDataObject> getEntityClass() {
        return JpaDataObject.class;
    }

    @Override
    protected JpaDataObject getEntityInstance() {
        return new JpaDataObject();
    }
}
