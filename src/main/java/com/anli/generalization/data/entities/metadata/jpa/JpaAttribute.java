package com.anli.generalization.data.entities.metadata.jpa;

import com.anli.generalization.data.entities.jpa.JpaObject;
import com.anli.generalization.data.entities.metadata.Attribute;
import com.anli.generalization.data.entities.metadata.AttributeType;
import com.anli.generalization.data.entities.metadata.ListEntry;
import com.anli.generalization.data.entities.metadata.ObjectType;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

import static javax.persistence.EnumType.ORDINAL;
import static javax.persistence.FetchType.LAZY;

@Entity(name = "Attribute")
@Table(name = "attributes")
@AttributeOverride(name = "id", column = @Column(name = "attribute_id"))
public class JpaAttribute extends JpaObject implements Attribute {

    @Enumerated(ORDINAL)
    @Column(name = "type")
    protected AttributeType type;

    @Column(name = "name")
    protected String name;

    @Column(name = "multiple")
    protected Boolean multiple;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "reference_type_id", referencedColumnName = "object_type_id")
    protected JpaObjectType referenceType;

    @OneToMany(fetch = LAZY)
    @JoinColumn(name = "attribute_id", referencedColumnName = "attribute_id")
    @OrderColumn(name = "attribute_order")
    protected List<JpaListEntry> listEntries;

    public JpaAttribute() {
        this.listEntries = new ArrayList<>();
    }

    @Override
    public AttributeType getType() {
        return type;
    }

    @Override
    public void setType(AttributeType type) {
        this.type = type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean isMultiple() {
        return Boolean.TRUE.equals(multiple);
    }

    @Override
    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }

    @Override
    public List<ListEntry> getListEntries() {
        return (List) listEntries;
    }

    @Override
    public ObjectType getReferenceType() {
        return referenceType;
    }

    @Override
    public void setReferenceType(ObjectType referenceType) {
        this.referenceType = (JpaObjectType) referenceType;
    }
}
