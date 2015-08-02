package com.anli.generalization.data.entities.jpa;

import java.util.Collection;
import java.util.HashSet;
import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import static javax.persistence.FetchType.LAZY;

@Entity(name = "ChildrenGroup")
@Table(name = "children_groups")
@AttributeOverride(name = "id", column = @Column(name = "group_id"))
public class ChildrenGroup extends JpaObject {

    @OneToMany(fetch = LAZY)
    @JoinColumn(name = "children_group_id", referencedColumnName = "group_id")
    protected Collection<JpaDataObject> children;

    public ChildrenGroup() {
        this.children = new HashSet<>();
    }

    public Collection<JpaDataObject> getChildren() {
        return children;
    }
}
