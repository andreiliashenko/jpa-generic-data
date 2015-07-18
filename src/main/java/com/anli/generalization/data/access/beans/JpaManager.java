package com.anli.generalization.data.access.beans;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class JpaManager {

    @PersistenceContext(unitName = "generalization")
    protected EntityManager manager;

    protected EntityManager getManager() {
        return manager;
    }
}
