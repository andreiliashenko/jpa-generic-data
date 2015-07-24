package com.anli.generalization.data.factory;

import com.anli.generalization.data.ProviderFactory;
import com.anli.generalization.data.access.DataObjectProvider;
import com.anli.generalization.data.access.metadata.AttributeProvider;
import com.anli.generalization.data.access.metadata.ListEntryProvider;
import com.anli.generalization.data.access.metadata.ObjectTypeProvider;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class JpaProviderFactory implements ProviderFactory {

    private static final JpaProviderFactory singleInstance = new JpaProviderFactory();

    public static JpaProviderFactory getInstance() {
        return singleInstance;
    }

    private final ClassPathXmlApplicationContext springContext;

    private JpaProviderFactory() {
        springContext = new ClassPathXmlApplicationContext("spring/jpa-generic-data-app.xml");
    }

    @Override
    public DataObjectProvider getDataObjectManager() {
        return getBean(DataObjectProvider.class);
    }

    @Override
    public AttributeProvider getAttributeManager() {
        return getBean(AttributeProvider.class);
    }

    @Override
    public ObjectTypeProvider getObjectTypeManager() {
        return getBean(ObjectTypeProvider.class);
    }

    @Override
    public ListEntryProvider getListEntryManager() {
        return getBean(ListEntryProvider.class);
    }

    protected <T> T getBean(Class<T> clazz) {
        return springContext.getBean(clazz);
    }
}
