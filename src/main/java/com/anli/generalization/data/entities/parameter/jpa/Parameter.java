package com.anli.generalization.data.entities.parameter.jpa;

import com.anli.generalization.data.entities.jpa.JpaObject;
import java.util.List;
import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

import static javax.persistence.FetchType.EAGER;

@Entity(name = "Parameter")
@Table(name = "parameters")
@AttributeOverride(name = "id", column = @Column(name = "parameter_id"))
public class Parameter extends JpaObject {

    @OneToMany(fetch = EAGER)
    @JoinColumn(name = "parameter_id", referencedColumnName = "parameter_id")
    @OrderColumn(name = "parameter_order")
    protected List<ParameterValue> parameterValues;

    public <T extends ParameterValue> List<T> getParameterValues() {
        return (List) parameterValues;
    }
}
