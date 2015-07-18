package com.anli.generalization.data.entities.parameter.jpa;

import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import org.joda.time.DateTime;

import static com.anli.generalization.data.entities.metadata.jpa.JpaAttrTypes.DATE_DISCRIMINATOR;

@Entity(name = "DateValue")
@DiscriminatorValue(DATE_DISCRIMINATOR)
public class DateValue extends ParameterValue<DateTime> {

    @Column(name = "date_value")
    protected Timestamp timestamp;

    @Transient
    protected DateTime value;

    protected Timestamp getTimestamp() {
        return timestamp;
    }

    protected void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public DateTime getValue() {
        Timestamp stamp = getTimestamp();
        if (stamp != null) {
            return new DateTime(stamp.getTime());
        }
        return null;
    }

    @Override
    public void setValue(DateTime value) {
        Timestamp stamp = null;
        if (value != null) {
            stamp = new Timestamp(value.getMillis());
        }
        setTimestamp(stamp);
    }
}
