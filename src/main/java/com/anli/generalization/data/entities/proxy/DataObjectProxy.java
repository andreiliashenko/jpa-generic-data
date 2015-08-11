package com.anli.generalization.data.entities.proxy;

import com.anli.generalization.data.access.internal.beans.SecondaryEntitesFactory;
import com.anli.generalization.data.entities.DataObject;
import com.anli.generalization.data.entities.jpa.ChildrenGroup;
import com.anli.generalization.data.entities.jpa.JpaDataObject;
import com.anli.generalization.data.entities.metadata.Attribute;
import com.anli.generalization.data.entities.metadata.AttributeType;
import com.anli.generalization.data.entities.metadata.ObjectType;
import com.anli.generalization.data.entities.metadata.jpa.JpaAttribute;
import com.anli.generalization.data.entities.metadata.jpa.JpaListEntry;
import com.anli.generalization.data.entities.metadata.jpa.JpaObjectType;
import com.anli.generalization.data.entities.parameter.jpa.ListValue;
import com.anli.generalization.data.entities.parameter.jpa.Parameter;
import com.anli.generalization.data.entities.parameter.jpa.ParameterValue;
import com.anli.generalization.data.entities.parameter.jpa.ReferenceValue;
import com.google.common.base.Function;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import org.springframework.context.annotation.Scope;

import static com.anli.generalization.data.entities.metadata.AttributeType.LIST;
import static com.anli.generalization.data.entities.metadata.AttributeType.REFERENCE;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Iterables.getFirst;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;

@Named
@Scope("prototype")
public class DataObjectProxy implements DataObject {

    protected static final ValueExtractor VALUE_EXTRACTOR = new ValueExtractor();

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
    public ObjectType getObjectType() {
        return proxiedObject.getObjectType();
    }

    @Override
    public DataObject getParent() {
        ChildrenGroup parentGroup = proxiedObject.getParentGroup();
        return parentGroup != null ? proxyBuilder.getProxy(parentGroup.getParent()) : null;
    }

    @Override
    public Collection<DataObject> getChildren(ObjectType objectType, boolean hierarchically) {
        checkArgument(objectType != null, "Object type for search cannot be null");
        List<ChildrenGroup> groups = new LinkedList<>();
        collectTypeGroups(objectType, groups, hierarchically);
        return proxyBuilder.getProxyCollection(groups);
    }

    protected void collectTypeGroups(ObjectType type, List<ChildrenGroup> groups,
            boolean hierarchically) {
        ChildrenGroup group = proxiedObject.getChildrenGroup((JpaObjectType) type);
        if (group != null) {
            groups.add(group);
        }
        if (hierarchically) {
            for (ObjectType childType : type.getChildren()) {
                collectTypeGroups(childType, groups, true);
            }
        }
    }

    @Override
    public void addChild(DataObject child) {
        checkArgument(child != null, "Added child cannot be null");
        JpaDataObject childObject = ((DataObjectProxy) child).getProxiedObject();
        JpaObjectType childType = childObject.getObjectType();
        ChildrenGroup targetGroup = proxiedObject.getChildrenGroup(childType);
        if (targetGroup == null) {
            targetGroup = secondaryEntitiesFactory.createChildrenGroup();
            proxiedObject.setChildrenGroup(childType, targetGroup);
            targetGroup.setParent(proxiedObject);
        }
        targetGroup.getChildren().add(childObject);
        childObject.setParentGroup(targetGroup);
    }

    @Override
    public void removeChild(DataObject child) {
        checkArgument(child != null, "Removed child can not be null");
        JpaDataObject childObject = ((DataObjectProxy) child).getProxiedObject();
        JpaObjectType childType = childObject.getObjectType();
        ChildrenGroup targetGroup = proxiedObject.getChildrenGroup(childType);
        if (targetGroup != null) {
            targetGroup.getChildren().remove(childObject);
            childObject.setParentGroup(null);
            if (targetGroup.getChildren().isEmpty()) {
                proxiedObject.getChildrenGroups().remove(childType);
                targetGroup.setParent(null);
            }
        }
    }

    @Override
    public <T> T getValue(Attribute attribute) {
        checkArgument(attribute != null, "Attribute of value can not be null");
        Parameter parameter = proxiedObject.getParameter((JpaAttribute) attribute);
        if (parameter == null) {
            return (T) (attribute.isMultiple() ? emptyList() : null);
        }
        List<ParameterValue> values = parameter.getParameterValues();
        if (attribute.isMultiple()) {
            return (T) newArrayList(transform(values, VALUE_EXTRACTOR));
        } else {
            return (T) VALUE_EXTRACTOR.apply(getFirst(values, null));
        }
    }

    @Override
    public <T> void setSingleValue(Attribute attribute, T value) {
        checkArgument(attribute != null, "Attribute of value can not be null");
        if (value == null) {
            JpaAttribute jpaAttribute = (JpaAttribute) attribute;
            proxiedObject.getParameters().remove(jpaAttribute);
            return;
        }
        List<ParameterValue<T>> parameterValues = getParameterValuesForSet(attribute);
        ParameterValue<T> parameterValue = getFirst(parameterValues, null);
        if (parameterValue == null) {
            parameterValue = secondaryEntitiesFactory.createParameterValue(attribute.getType());
            parameterValues.add(parameterValue);
        }
        setParameterValue(attribute, parameterValue, value);
        clearValuesTail(parameterValues.listIterator(1));
    }

    protected <T> void setParameterValue(Attribute attribute, ParameterValue<T> parameterValue, T value) {
        AttributeType attributeType = attribute.getType();
        if (REFERENCE.equals(attributeType)) {
            setReferenceValue(attribute, (ReferenceValue) parameterValue,
                    ((DataObjectProxy) value).getProxiedObject());
        } else if (LIST.equals(attributeType)) {
            setListValue(attribute, (ListValue) parameterValue, (JpaListEntry) value);
        } else {
            parameterValue.setValue(value);
        }
    }

    protected void setReferenceValue(Attribute attribute, ReferenceValue referenceValue,
            JpaDataObject reference) {
        ObjectType type = reference.getObjectType();
        ObjectType expectedType = attribute.getReferenceType();
        if (expectedType != null) {
            boolean match = false;
            while (!match && type != null) {
                if (expectedType.equals(type)) {
                    match = true;
                }
                type = type.getParent();
            }
            checkArgument(match, "Referenced object does not belong to attribute reference type");
        }
        referenceValue.setValue(reference);
    }

    protected void setListValue(Attribute attribute, ListValue listValue, JpaListEntry listEntry) {
        checkArgument(attribute.getListEntries().contains(listEntry),
                "List entry does not belong to attribute");
        listValue.setValue(listEntry);
    }

    @Override
    public <T> void setMultipleValues(Attribute attribute, List<T> values) {
        checkArgument(attribute != null, "Attribute of value can not be null");
        checkArgument(attribute.isMultiple(),
                "Attribute [%s, %s] is not multiple", attribute.getId(), attribute.getName());
        if (values == null || values.isEmpty()) {
            JpaAttribute jpaAttribute = (JpaAttribute) attribute;
            proxiedObject.getParameters().remove(jpaAttribute);
            return;
        }
        List<ParameterValue<T>> parameterValues = getParameterValuesForSet(attribute);
        Iterator<ParameterValue<T>> parameterIterator = parameterValues.iterator();
        Iterator<T> valueIterator = values.iterator();
        while (parameterIterator.hasNext() && valueIterator.hasNext()) {
            ParameterValue<T> parameterValue = parameterIterator.next();
            T value = valueIterator.next();
            setParameterValue(attribute, parameterValue, value);
        }
        if (parameterIterator.hasNext()) {
            clearValuesTail(parameterIterator);
        }
        while (valueIterator.hasNext()) {
            ParameterValue<T> newValue =
                    secondaryEntitiesFactory.createParameterValue(attribute.getType());
            setParameterValue(attribute, newValue, valueIterator.next());
            parameterValues.add(newValue);
        }
    }

    @Override
    public <T> void addMultipleValue(Attribute attribute, T value) {
        checkArgument(attribute != null, "Attribute of value can not be null");
        checkArgument(attribute.isMultiple(),
                "Attribute [%s, %s] is not multiple", attribute.getId(), attribute.getName());
        checkArgument(value != null, "Value for addition can not be null");
        List<ParameterValue<T>> parameterValues = getParameterValuesForSet(attribute);
        ParameterValue<T> parameterValue =
                secondaryEntitiesFactory.createParameterValue(attribute.getType());
        setParameterValue(attribute, parameterValue, value);
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

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DataObjectProxy)) {
            return false;
        }
        return proxiedObject.equals(((DataObjectProxy) obj).proxiedObject);
    }

    @Override
    public int hashCode() {
        return proxiedObject.hashCode();
    }

    protected static class ValueExtractor implements Function<ParameterValue, Object> {

        @Override
        public Object apply(ParameterValue input) {
            if (input == null) {
                return null;
            }
            return input.getValue();
        }
    }
}
