package com.anli.generalization.data.entities.parameter.jpa;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import static com.anli.generalization.data.entities.metadata.jpa.JpaAttrTypes.TEXT_DISCRIMINATOR;

@Entity(name = "TextValue")
@DiscriminatorValue(TEXT_DISCRIMINATOR)
public class TextValue extends ParameterValue<String> {

    @Column(name = "text_value")
    protected String value;

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }
}
