package com.anli.generalization.data.entities.metadata.jpa;

import com.anli.generalization.data.entities.jpa.JpaObject;
import com.anli.generalization.data.entities.metadata.ListEntry;
import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity(name = "ListEntry")
@Table(name = "list_entries")
@AttributeOverride(name = "id", column = @Column(name = "list_entry_id"))
public class JpaListEntry extends JpaObject implements ListEntry {

    @Column(name = "entry_value")
    protected String entryValue;

    @Override
    public String getEntryValue() {
        return entryValue;
    }

    @Override
    public void setEntryValue(String entryValue) {
        this.entryValue = entryValue;
    }
}
