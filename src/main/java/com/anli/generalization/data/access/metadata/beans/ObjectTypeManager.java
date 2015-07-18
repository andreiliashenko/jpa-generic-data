package com.anli.generalization.data.access.metadata.beans;

import com.anli.generalization.data.access.beans.GenericManager;
import com.anli.generalization.data.entities.metadata.ObjectType;
import com.anli.generalization.data.entities.metadata.jpa.JpaObjectType;
import java.math.BigInteger;
import javax.inject.Named;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Propagation.MANDATORY;

@Named
public class ObjectTypeManager extends GenericManager<JpaObjectType>
        implements com.anli.generalization.data.access.metadata.ObjectTypeManager {

    @Override
    protected Class<JpaObjectType> getEntityClass() {
        return JpaObjectType.class;
    }

    @Override
    protected JpaObjectType getEntityInstance() {
        return new JpaObjectType();
    }

    @Override
    @Transactional(propagation = MANDATORY)
    public ObjectType create(ObjectType parentType) {
        JpaObjectType type = createEntity();
        if (parentType != null) {
            parentType.getChildren().add(type);
        }
        return type;
    }

    @Override
    @Transactional(propagation = MANDATORY)
    public ObjectType getById(BigInteger id) {
        return getEntityById(id);
    }

    @Override
    @Transactional(propagation = MANDATORY)
    public void remove(ObjectType type) {
        removeEntity((JpaObjectType) type);
    }
}
