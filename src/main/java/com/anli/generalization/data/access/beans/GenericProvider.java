package com.anli.generalization.data.access.beans;

import com.anli.generalization.data.entities.jpa.JpaObject;
import java.math.BigInteger;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Propagation.MANDATORY;

public abstract class GenericProvider<E extends JpaObject> extends JpaProvider {

    protected abstract Class<E> getEntityClass();

    protected abstract E getEntityInstance();

    @Transactional(propagation = MANDATORY)
    public E createEntity() {
        E entity = getEntityInstance();
        getManager().persist(entity);
        return entity;
    }

    @Transactional(propagation = MANDATORY)
    public E getEntityById(BigInteger id) {
        return getManager().find(getEntityClass(), id);
    }

    @Transactional(propagation = MANDATORY)
    public void removeEntity(E entity) {
        getManager().remove(entity);
    }
}
