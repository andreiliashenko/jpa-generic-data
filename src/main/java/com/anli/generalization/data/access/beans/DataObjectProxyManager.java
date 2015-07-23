package com.anli.generalization.data.access.beans;

import com.anli.generalization.data.access.internal.beans.DataObjectManager;
import com.anli.generalization.data.entities.DataObject;
import com.anli.generalization.data.entities.jpa.JpaDataObject;
import com.anli.generalization.data.entities.metadata.ObjectType;
import com.anli.generalization.data.entities.metadata.jpa.JpaObjectType;
import com.anli.generalization.data.entities.proxy.DataObjectProxy;
import com.anli.generalization.data.entities.proxy.DataObjectProxyBuilder;
import java.math.BigInteger;
import javax.inject.Inject;
import javax.inject.Named;

@Named
public class DataObjectProxyManager
        implements com.anli.generalization.data.access.DataObjectManager {

    protected final DataObjectProxyBuilder proxyBuilder;
    protected final DataObjectManager dataObjectManager;

    @Inject
    public DataObjectProxyManager(DataObjectProxyBuilder proxyBuilder,
            DataObjectManager dataObjectManager) {
        this.proxyBuilder = proxyBuilder;
        this.dataObjectManager = dataObjectManager;
    }

    @Override
    public DataObject create(ObjectType type) {
        JpaDataObject jpaObject = dataObjectManager.createEntity();
        jpaObject.setObjectType((JpaObjectType) type);
        return proxyBuilder.getProxy(jpaObject);
    }

    @Override
    public DataObject getById(BigInteger id) {
        return proxyBuilder.getProxy(dataObjectManager.getEntityById(id));
    }

    @Override
    public void remove(DataObject object) {
        DataObjectProxy proxy = (DataObjectProxy) object;
        JpaDataObject jpaObject = proxy.getProxiedObject();
        dataObjectManager.removeEntity(jpaObject);
    }
}
