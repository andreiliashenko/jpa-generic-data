package com.anli.generalization.data.access.internal.beans;

import com.anli.generalization.data.access.beans.JpaProvider;
import com.anli.generalization.data.entities.jpa.ChildrenGroup;
import com.anli.generalization.data.entities.metadata.AttributeType;
import com.anli.generalization.data.entities.parameter.jpa.DateValue;
import com.anli.generalization.data.entities.parameter.jpa.ListValue;
import com.anli.generalization.data.entities.parameter.jpa.Parameter;
import com.anli.generalization.data.entities.parameter.jpa.ParameterValue;
import com.anli.generalization.data.entities.parameter.jpa.ReferenceValue;
import com.anli.generalization.data.entities.parameter.jpa.TextValue;
import javax.inject.Named;
import org.springframework.transaction.annotation.Transactional;

import static com.anli.generalization.data.entities.metadata.AttributeType.DATE;
import static com.anli.generalization.data.entities.metadata.AttributeType.LIST;
import static com.anli.generalization.data.entities.metadata.AttributeType.REFERENCE;
import static com.anli.generalization.data.entities.metadata.AttributeType.TEXT;
import static org.springframework.transaction.annotation.Propagation.MANDATORY;

@Named
public class SecondaryEntitesFactory extends JpaProvider {

    @Transactional(propagation = MANDATORY)
    public ParameterValue createParameterValue(AttributeType type) {
        ParameterValue parameterValue = getParameterValueByType(type);
        getManager().persist(parameterValue);
        return parameterValue;
    }

    @Transactional(propagation = MANDATORY)
    public Parameter createParameter() {
        Parameter parameter = new Parameter();
        getManager().persist(parameter);
        return parameter;
    }

    @Transactional(propagation = MANDATORY)
    public ChildrenGroup createChildrenGroup() {
        ChildrenGroup group = new ChildrenGroup();
        getManager().persist(group);
        return group;
    }

    protected ParameterValue getParameterValueByType(AttributeType type) {
        if (type == TEXT) {
            return new TextValue();
        } else if (type == DATE) {
            return new DateValue();
        } else if (type == LIST) {
            return new ListValue();
        } else if (type == REFERENCE) {
            return new ReferenceValue();
        } else {
            throw new IllegalArgumentException("Incorrect attribute type: " + type);
        }
    }
}
