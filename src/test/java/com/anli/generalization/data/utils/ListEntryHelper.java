package com.anli.generalization.data.utils;

import com.anli.sqlexecution.handling.ResultSetHandler;
import com.anli.sqlexecution.handling.TransformingResultSet;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

import static java.util.Arrays.asList;

public class ListEntryHelper extends SqlHelper {

    public ListEntryHelper(DataSource dataSource) {
        super(dataSource);
    }

    private static final String INSERT_LIST_ENTRY = "insert into list_entries "
            + "(list_entry_id, entry_value) values (?, ?)";
    private static final String LINK_LIST_ENTRY_TO_ATTRIBUTE = "updata list_entries "
            + "set attribute_id = ?, attribute_order = ? where list_entry_id = ?";
    private static final String SELECT_LIST_ENTRY = "select list_entry_id, entry_value, "
            + "attribute_id, attribute_order from list_entries where list_entry_id = ?";

    public void createListEntry(long id, String entryValue) {
        executor.executeUpdate(INSERT_LIST_ENTRY, asList(id, entryValue));
    }

    public void linkListEntriesToAttribute(long attributeId, long... entryIds) {
        for (int i = 0; i < entryIds.length; i++) {
            executor.executeUpdate(LINK_LIST_ENTRY_TO_ATTRIBUTE, asList(attributeId, i, entryIds[i]));
        }
    }

    public Map<String, Object> readListEntry(BigInteger id) {
        return executor.executeSelect(SELECT_LIST_ENTRY, asList(new BigDecimal(id)), new ListEntryReader());
    }

    protected class ListEntryReader implements ResultSetHandler<Map<String, Object>> {

        @Override
        public Map<String, Object> handle(TransformingResultSet resultSet) throws SQLException {
            Map<String, Object> result = new HashMap<>();
            if (resultSet.next()) {
                result.put("id", getBigInteger(resultSet.getValue("list_entry_id", BigDecimal.class)));
                result.put("entryValue", resultSet.getValue("entry_value", String.class));
                result.put("attribute", getBigInteger(resultSet.getValue("attribute_id", BigDecimal.class)));
                result.put("order", getInteger(resultSet.getValue("attribute_order", BigDecimal.class)));
            } else {
                return null;
            }
            if (resultSet.next()) {
                throw new RuntimeException("More than 1 record with this primaryKey");
            }
            return result;
        }
    }
}
