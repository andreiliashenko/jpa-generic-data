package com.anli.generalization.data.entities.metadata.jpa;

import com.anli.generalization.data.entities.jpa.JpaObject;
import com.anli.generalization.data.entities.metadata.Attribute;
import com.anli.generalization.data.entities.metadata.ObjectType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

import static javax.persistence.FetchType.LAZY;

@Entity(name = "ObjectType")
@Table(name = "object_types")
@AttributeOverride(name = "id", column = @Column(name = "object_type_id"))
public class JpaObjectType extends JpaObject implements ObjectType {

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "parent_type_id", referencedColumnName = "object_type_id")
    protected JpaObjectType parent;

    @OneToMany(fetch = LAZY, mappedBy = "parent")
    protected Collection<JpaObjectType> children;

    @Column(name = "name")
    protected String name;

    @OneToMany(fetch = LAZY)
    @JoinColumn(name = "object_type_id", referencedColumnName = "object_type_id")
    @OrderColumn(name = "object_type_order")
    protected List<JpaAttribute> attributes;

    public JpaObjectType() {
        this.children = new HashSet<>();
        this.attributes = new ArrayList<>();
    }

    @Override
    public ObjectType getParent() {
        return parent;
    }

    @Override
    public void setParent(ObjectType parent) {
        this.parent = (JpaObjectType) parent;
        if (parent != null) {
            parent.getChildren().add(this);
        }
    }

    @Override
    public Collection<ObjectType> getChildren() {
        return (Collection) children;
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
    public List<Attribute> getAttributes() {
        return (List) attributes;
    }
}
