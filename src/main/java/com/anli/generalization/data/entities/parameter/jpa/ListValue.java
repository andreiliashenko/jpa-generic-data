package com.anli.generalization.data.entities.parameter.jpa;

import com.anli.generalization.data.entities.metadata.jpa.JpaListEntry;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import static com.anli.generalization.data.entities.metadata.jpa.JpaAttrTypes.LIST_DISCRIMINATOR;
import static javax.persistence.FetchType.EAGER;

@Entity(name = "ListValue")
@DiscriminatorValue(LIST_DISCRIMINATOR)
public class ListValue extends ParameterValue<JpaListEntry> {

    @ManyToOne(fetch = EAGER)
    @JoinColumn(name = "list_entry_id", referencedColumnName = "list_entry_id")
    protected JpaListEntry value;

    @Override
    public JpaListEntry getValue() {
        return value;
    }

    @Override
    public void setValue(JpaListEntry value) {
        this.value = value;
    }
}
