package com.anli.generalization.data.access.metadata.beans;

import com.anli.generalization.data.access.beans.GenericManager;
import com.anli.generalization.data.entities.metadata.ListEntry;
import com.anli.generalization.data.entities.metadata.jpa.JpaListEntry;
import java.math.BigInteger;
import javax.inject.Named;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Propagation.MANDATORY;

@Named
public class ListEntryManager extends GenericManager<JpaListEntry>
        implements com.anli.generalization.data.access.metadata.ListEntryManager {

    @Override
    protected Class<JpaListEntry> getEntityClass() {
        return JpaListEntry.class;
    }

    @Override
    protected JpaListEntry getEntityInstance() {
        return new JpaListEntry();
    }

    @Override
    @Transactional(propagation = MANDATORY)
    public ListEntry create() {
        return createEntity();
    }

    @Override
    @Transactional(propagation = MANDATORY)
    public ListEntry getById(BigInteger id) {
        return getEntityById(id);
    }

    @Override
    @Transactional(propagation = MANDATORY)
    public void remove(ListEntry entry) {
        removeEntity((JpaListEntry) entry);
    }
}
