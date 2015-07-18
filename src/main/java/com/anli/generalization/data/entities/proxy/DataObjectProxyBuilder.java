package com.anli.generalization.data.entities.proxy;

import com.anli.generalization.data.entities.DataObject;
import com.anli.generalization.data.entities.jpa.ChildrenGroup;
import com.anli.generalization.data.entities.jpa.JpaDataObject;
import java.util.Collection;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Collections.emptyList;
import static org.springframework.transaction.annotation.Propagation.MANDATORY;

@Named
public class DataObjectProxyBuilder {

    protected final Provider<DataObjectProxy> proxyProvider;
    protected final Provider<ChildrenProxyCollection> proxyCollectionProvider;

    @Inject
    public DataObjectProxyBuilder(Provider<DataObjectProxy> proxyProvider,
            Provider<ChildrenProxyCollection> proxyCollectionProvider) {
        this.proxyProvider = proxyProvider;
        this.proxyCollectionProvider = proxyCollectionProvider;
    }

    @Transactional(propagation = MANDATORY)
    public DataObjectProxy getProxy(JpaDataObject dataObject) {
        if (dataObject == null) {
            return null;
        }
        DataObjectProxy proxy = proxyProvider.get();
        proxy.setProxiedObject(dataObject);
        return proxy;
    }

    @Transactional(propagation = MANDATORY)
    public Collection<DataObject> getProxyCollection(Collection<ChildrenGroup> groups) {
        if (groups == null || groups.isEmpty()) {
            return emptyList();
        }
        ChildrenProxyCollection collection = proxyCollectionProvider.get();
        for (ChildrenGroup group : groups) {
            collection.addChildrenGroup(group);
        }
        return collection;
    }
}
