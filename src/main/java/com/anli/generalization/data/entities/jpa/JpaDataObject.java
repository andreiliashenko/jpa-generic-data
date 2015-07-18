package com.anli.generalization.data.entities.jpa;

import com.anli.generalization.data.entities.parameter.jpa.Parameter;
import com.anli.generalization.data.entities.metadata.jpa.JpaObjectType;
import com.anli.generalization.data.entities.metadata.jpa.JpaAttribute;
import java.util.Map;
import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import static javax.persistence.FetchType.LAZY;

@Entity(name = "DataObject")
@Table(name = "data_objects")
@AttributeOverride(name = "id", column = @Column(name = "object_id"))
public class JpaDataObject extends JpaObject {

    @Column(name = "name")
    protected String name;

    @Column(name = "description")
    protected String description;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "object_type_id", referencedColumnName = "object_type_id")
    protected JpaObjectType objectType;

    @OneToMany(fetch = LAZY)
    @JoinColumn(name = "object_id", referencedColumnName = "object_id")
    @MapKeyJoinColumn(name = "attribute_id", referencedColumnName = "attribute_id")
    protected Map<JpaAttribute, Parameter> parameters;

    @OneToMany(fetch = LAZY)
    @JoinColumn(name = "parent_id", referencedColumnName = "object_id")
    @MapKeyJoinColumn(name = "object_type_id", referencedColumnName = "object_type_id")
    protected Map<JpaObjectType, ChildrenGroup> childrenGroups;

    @ManyToOne(fetch = LAZY)
    @JoinTable(name = "children_groups",
            joinColumns = @JoinColumn(name = "children_group_id", referencedColumnName = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "parent_id", referencedColumnName = "object_id"))
    protected JpaDataObject parent;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public JpaObjectType getObjectType() {
        return objectType;
    }

    public void setObjectType(JpaObjectType objectType) {
        this.objectType = objectType;
    }

    public Map<JpaAttribute, Parameter> getParameters() {
        return parameters;
    }

    public Parameter getParameter(JpaAttribute attribute) {
        return getParameters().get(attribute);
    }

    public void setParameter(JpaAttribute attribute, Parameter parameter) {
        getParameters().put(attribute, parameter);
    }

    public Map<JpaObjectType, ChildrenGroup> getChildrenGroups() {
        return childrenGroups;
    }

    public ChildrenGroup getChildrenGroup(JpaObjectType objectType) {
        return getChildrenGroups().get(objectType);
    }

    public void setChildrenGroup(JpaObjectType objectType, ChildrenGroup childrenGroup) {
        getChildrenGroups().put(objectType, childrenGroup);
    }

    public JpaDataObject getParent() {
        return parent;
    }
}
