package com.anli.generalization.data.access.metadata.beans;

import com.anli.generalization.data.access.beans.GenericProvider;
import com.anli.generalization.data.entities.metadata.Attribute;
import com.anli.generalization.data.entities.metadata.jpa.JpaAttribute;
import java.math.BigInteger;
import javax.inject.Named;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Propagation.MANDATORY;

@Named
public class AttributeProvider extends GenericProvider<JpaAttribute>
        implements com.anli.generalization.data.access.metadata.AttributeProvider {

    @Override
    protected Class<JpaAttribute> getEntityClass() {
        return JpaAttribute.class;
    }

    @Override
    protected JpaAttribute getEntityInstance() {
        return new JpaAttribute();
    }

    @Override
    @Transactional(propagation = MANDATORY)
    public Attribute getById(BigInteger id) {
        return getEntityById(id);
    }

    @Override
    @Transactional(propagation = MANDATORY)
    public Attribute create() {
        return createEntity();
    }

    @Override
    @Transactional(propagation = MANDATORY)
    public void remove(Attribute attribute) {
        removeEntity((JpaAttribute) attribute);
    }
}
