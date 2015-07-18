package com.anli.generalization.data.entities.parameter.jpa;

import com.anli.generalization.data.entities.jpa.JpaObject;
import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.Table;

import static javax.persistence.DiscriminatorType.INTEGER;
import static javax.persistence.InheritanceType.SINGLE_TABLE;

@Entity(name = "ParameterValue")
@Inheritance(strategy = SINGLE_TABLE)
@Table(name = "parameter_values")
@DiscriminatorColumn(name = "type", discriminatorType = INTEGER)
@AttributeOverride(name = "id", column = @Column(name = "value_id"))
public abstract class ParameterValue<T> extends JpaObject {

    public abstract T getValue();

    public abstract void setValue(T value);
}
