package com.anli.generalization.data.access.internal.beans;

import com.anli.generalization.data.access.beans.GenericProvider;
import com.anli.generalization.data.entities.jpa.JpaDataObject;
import javax.inject.Named;

@Named
public class DataObjectProvider extends GenericProvider<JpaDataObject> {

    @Override
    protected Class<JpaDataObject> getEntityClass() {
        return JpaDataObject.class;
    }

    @Override
    protected JpaDataObject getEntityInstance() {
        return new JpaDataObject();
    }
}
