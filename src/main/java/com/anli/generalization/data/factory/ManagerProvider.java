package com.anli.generalization.data.factory;

import com.anli.generalization.data.access.DataObjectManager;
import com.anli.generalization.data.access.metadata.AttributeManager;
import com.anli.generalization.data.access.metadata.ListEntryManager;
import com.anli.generalization.data.access.metadata.ObjectTypeManager;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ManagerProvider implements com.anli.generalization.data.ManagerProvider {

    private static final ManagerProvider singleInstance = new ManagerProvider();

    public static ManagerProvider getInstance() {
        return singleInstance;
    }

    private final ClassPathXmlApplicationContext springContext;

    private ManagerProvider() {
        springContext = new ClassPathXmlApplicationContext("spring/jpa-generic-data-app.xml");
    }

    @Override
    public DataObjectManager getDataObjectManager() {
        return getBean(DataObjectManager.class);
    }

    @Override
    public AttributeManager getAttributeManager() {
        return getBean(AttributeManager.class);
    }

    @Override
    public ObjectTypeManager getObjectTypeManager() {
        return getBean(ObjectTypeManager.class);
    }

    @Override
    public ListEntryManager getListEntryManager() {
        return getBean(ListEntryManager.class);
    }

    protected <T> T getBean(Class<T> clazz) {
        return springContext.getBean(clazz);
    }
}
