package com.anli.generalization.data.entities.jpa;

import java.io.Serializable;
import java.math.BigInteger;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.MappedSuperclass;
import javax.persistence.TableGenerator;

import static javax.persistence.GenerationType.TABLE;
import static javax.persistence.InheritanceType.TABLE_PER_CLASS;

@MappedSuperclass
@Inheritance(strategy = TABLE_PER_CLASS)
@TableGenerator(name = "id_generator", table = "id_generation_sequences", pkColumnName = "entity_set",
        pkColumnValue = "data", valueColumnName = "last_id", allocationSize = 1)
public abstract class JpaObject implements Serializable {

    @Id
    @GeneratedValue(generator = "id_generator", strategy = TABLE)
    protected BigInteger id;

    public BigInteger getId() {
        return id;
    }

    @Override
    public boolean equals(Object comparee) {
        if (getId() == null) {
            return false;
        }
        if (comparee == null) {
            return false;
        }
        if (!this.getClass().equals(comparee.getClass())) {
            return false;
        }
        JpaObject dataObjectComparee = (JpaObject) comparee;
        return this.getId().equals(dataObjectComparee.getId());
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 42 * hash + (this.getId() != null ? this.getId().hashCode() : 0);
        return hash;
    }
}
