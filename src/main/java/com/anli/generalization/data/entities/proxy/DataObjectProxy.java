package com.anli.generalization.data.entities.proxy;

import com.anli.generalization.data.access.internal.beans.SecondaryEntitesFactory;
import com.anli.generalization.data.entities.DataObject;
import com.anli.generalization.data.entities.jpa.ChildrenGroup;
import com.anli.generalization.data.entities.jpa.JpaDataObject;
import com.anli.generalization.data.entities.metadata.Attribute;
import com.anli.generalization.data.entities.metadata.ObjectType;
import com.anli.generalization.data.entities.metadata.jpa.JpaAttribute;
import com.anli.generalization.data.entities.metadata.jpa.JpaObjectType;
import com.anli.generalization.data.entities.parameter.jpa.Parameter;
import com.anli.generalization.data.entities.parameter.jpa.ParameterValue;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import org.springframework.context.annotation.Scope;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.getFirst;

@Named
@Scope("prototype")
public class DataObjectProxy implements DataObject {

    protected final DataObjectProxyBuilder proxyBuilder;
    protected final SecondaryEntitesFactory secondaryEntitiesFactory;

    protected JpaDataObject proxiedObject;

    @Inject
    public DataObjectProxy(DataObjectProxyBuilder proxyBuilder,
            SecondaryEntitesFactory secondaryEntitiesFactory) {
        this.proxyBuilder = proxyBuilder;
        this.secondaryEntitiesFactory = secondaryEntitiesFactory;
    }

    public void setProxiedObject(JpaDataObject proxiedObject) {
        this.proxiedObject = proxiedObject;
    }

    public JpaDataObject getProxiedObject() {
        return proxiedObject;
    }

    @Override
    public BigInteger getId() {
        return proxiedObject.getId();
    }

    @Override
    public String getName() {
        return proxiedObject.getName();
    }

    @Override
    public void setName(String name) {
        proxiedObject.setName(name);
    }

    @Override
    public String getDescription() {
        return proxiedObject.getDescription();
    }

    @Override
    public void setDescription(String description) {
        proxiedObject.setDescription(description);
    }

    @Override
    public DataObject getParent() {
        return proxyBuilder.getProxy(proxiedObject.getParent());
    }

    @Override
    public Collection<DataObject> getChildren(ObjectType objectType, boolean hierarchically) {
        checkNotNull(objectType, "Object type for search cannot be null");
        List<ChildrenGroup> groups = new LinkedList<>();
        ObjectType currentType = objectType;
        while (currentType != null) {
            ChildrenGroup group = proxiedObject.getChildrenGroup((JpaObjectType) currentType);
            if (group != null) {
                groups.add(group);
            }
            if (!hierarchically) {
                break;
            }
            currentType = currentType.getParent();
        }
        return proxyBuilder.getProxyCollection(groups);
    }

    @Override
    public void addChild(DataObject child) {
        checkNotNull(child, "Added child cannot be null");
        JpaDataObject childObject = ((DataObjectProxy) child).getProxiedObject();
        JpaObjectType childType = childObject.getObjectType();
        ChildrenGroup targetGroup = proxiedObject.getChildrenGroup(childType);
        if (targetGroup == null) {
            targetGroup = secondaryEntitiesFactory.createChildrenGroup();
            proxiedObject.setChildrenGroup(childType, targetGroup);
        }
        targetGroup.getChildren().add(childObject);
    }

    @Override
    public void removeChild(DataObject child) {
        checkNotNull(child, "Removed child can not be null");
        JpaDataObject childObject = ((DataObjectProxy) child).getProxiedObject();
        JpaObjectType childType = childObject.getObjectType();
        ChildrenGroup targetGroup = proxiedObject.getChildrenGroup(childType);
        if (targetGroup != null) {
            targetGroup.getChildren().remove(childObject);
        }
    }

    @Override
    public <T> T getValue(Attribute attribute) {
        checkNotNull(attribute, "Attribute of value can not be null");
        Parameter parameter = proxiedObject.getParameter((JpaAttribute) attribute);
        if (parameter == null) {
            return null;
        }
        List<ParameterValue> values = parameter.getParameterValues();
        if (attribute.isMultiple()) {
            return (T) values;
        } else {
            return (T) getFirst(values, null);
        }
    }

    @Override
    public <T> void setSingleValue(Attribute attribute, T value) {
        checkNotNull(attribute, "Attribute of value can not be null");
        List<ParameterValue<T>> parameterValues = getParameterValuesForSet(attribute);
        ParameterValue<T> parameterValue = getFirst(parameterValues, null);
        if (parameterValue == null) {
            parameterValue = secondaryEntitiesFactory.createParameterValue(attribute.getType());
            parameterValues.add(parameterValue);
        }
        parameterValue.setValue(value);
        clearValuesTail(parameterValues.listIterator(1));
    }

    @Override
    public <T> void setMultipleValues(Attribute attribute, List<T> values) {
        checkNotNull(attribute, "Attribute of value can not be null");
        checkArgument(attribute.isMultiple(),
                "Attribute [%s, %s] is not multiple", attribute.getId(), attribute.getName());
        List<ParameterValue<T>> parameterValues = getParameterValuesForSet(attribute);
        Iterator<ParameterValue<T>> parameterIterator = parameterValues.iterator();
        Iterator<T> valueIterator = values.iterator();
        while (parameterIterator.hasNext() && valueIterator.hasNext()) {
            ParameterValue<T> parameterValue = parameterIterator.next();
            T value = valueIterator.next();
            parameterValue.setValue(value);
        }
        if (parameterIterator.hasNext()) {
            clearValuesTail(parameterIterator);
        }
        while (valueIterator.hasNext()) {
            ParameterValue<T> newValue =
                    secondaryEntitiesFactory.createParameterValue(attribute.getType());
            newValue.setValue(valueIterator.next());
            parameterValues.add(newValue);
        }
    }

    @Override
    public <T> void addMultipleValue(Attribute attribute, T value) {
        checkNotNull(attribute, "Attribute of value can not be null");
        checkArgument(attribute.isMultiple(),
                "Attribute [%s, %s] is not multiple", attribute.getId(), attribute.getName());
        List<ParameterValue<T>> parameterValues = getParameterValuesForSet(attribute);
        ParameterValue<T> parameterValue =
                secondaryEntitiesFactory.createParameterValue(attribute.getType());
        parameterValue.setValue(value);
        parameterValues.add(parameterValue);
    }

    protected <T> List<ParameterValue<T>> getParameterValuesForSet(Attribute attribute) {
        Parameter parameter = proxiedObject.getParameter((JpaAttribute) attribute);
        if (parameter == null) {
            parameter = secondaryEntitiesFactory.createParameter();
        }
        return parameter.getParameterValues();
    }

    protected <T> void clearValuesTail(Iterator<ParameterValue<T>> iterator) {
        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
    }
}
