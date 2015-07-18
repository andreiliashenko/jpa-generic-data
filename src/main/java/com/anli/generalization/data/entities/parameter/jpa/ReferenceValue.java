package com.anli.generalization.data.entities.parameter.jpa;

import com.anli.generalization.data.entities.jpa.JpaDataObject;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import static com.anli.generalization.data.entities.metadata.jpa.JpaAttrTypes.REFERENCE_DISCRIMINATOR;
import static javax.persistence.FetchType.EAGER;

@Entity(name = "ReferenceValue")
@DiscriminatorValue(REFERENCE_DISCRIMINATOR)
public class ReferenceValue extends ParameterValue<JpaDataObject> {

    @ManyToOne(fetch = EAGER)
    @JoinColumn(name = "reference_id", referencedColumnName = "object_id")
    protected JpaDataObject value;

    @Override
    public JpaDataObject getValue() {
        return value;
    }

    @Override
    public void setValue(JpaDataObject value) {
        this.value = value;
    }
}
