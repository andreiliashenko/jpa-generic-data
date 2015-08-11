package com.anli.generalization.data.utils;

import com.anli.sqlexecution.handling.ResultSetHandler;
import com.anli.sqlexecution.handling.TransformingResultSet;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

import static java.util.Arrays.asList;

public class DataObjectHelper extends SqlHelper {

    private static final String INSERT_OBJECT = "insert into data_objects "
            + "(object_id, name, description, object_type_id, children_group_id) "
            + "values (?, ?, ?, ?, ?)";
    private static final String SELECT_OBJECT = "select object_id, name, "
            + "description, object_type_id, children_group_id "
            + "from data_objects where object_id = ?";
    private static final String INSERT_CHILDREN_GROUP = "insert into children_groups "
            + "(group_id, parent_id, object_type_id) values (?, ?, ?)";
    private static final String SELECT_CHILDREN_GROUP = "select group_id, parent_id, object_type_id "
            + "from children_groups where group_id = ?";
    private static final String SELECT_CHILDREN_GROUPS_BY_PARENT = "select group_id "
            + "from children_groups where parent_id = ?";
    private static final String INSERT_PARAMETER = "insert into parameters (parameter_id, "
            + "attribute_id, object_id) values (?, ?, ?)";
    private static final String SELECT_PARAMETER = "select parameter_id, attribute_id, object_id "
            + "from parameters where parameter_id = ?";
    private static final String SELECT_PARAMETERS_BY_OBJECT = "select parameter_id from parameters "
            + "where object_id = ?";
    private static final String INSERT_VALUE = "insert into parameter_values (value_id, "
            + "type, text_value, date_value, reference_id, list_entry_id) values (?, ?, ?, ?, ?, ?)";
    private static final String SELECT_VALUE = "select value_id, type, text_value, date_value, "
            + "reference_id, list_entry_id, parameter_id, parameter_order from parameter_values "
            + "where value_id = ?";
    private static final String SELECT_VALUES_BY_PARAMETER = "select value_id from parameter_values "
            + "where parameter_id = ? order by parameter_order";
    private static final String LINK_VALUE_TO_PARAMETER = "update parameter_values set parameter_id = ?, "
            + "parameter_order = ? where value_id = ?";

    public DataObjectHelper(DataSource dataSource) {
        super(dataSource);
    }

    public void createObject(long id, String name, String description, BigInteger objectType,
            BigInteger childrenGroup) {
        BigDecimal decimalGroupId = childrenGroup != null
                ? new BigDecimal(childrenGroup) : null;
        executor.executeUpdate(INSERT_OBJECT, asList(id, name, description, new BigDecimal(objectType),
                decimalGroupId));
    }

    public Map<String, Object> readObject(BigInteger id) {
        return executor.executeSelect(SELECT_OBJECT, asList(new BigDecimal(id)), new ObjectReader());
    }

    public void createChildrenGroup(long id, BigInteger parent, BigInteger objectType) {
        executor.executeUpdate(INSERT_CHILDREN_GROUP, asList(id, new BigDecimal(parent),
                new BigDecimal(objectType)));
    }

    public Map<String, Object> readChildrenGroup(BigInteger id) {
        return executor.executeSelect(SELECT_CHILDREN_GROUP, asList(new BigDecimal(id)), new GroupReader());
    }

    public Collection<BigInteger> readGroupsByParent(BigInteger parentId) {
        return executor.executeSelect(SELECT_CHILDREN_GROUPS_BY_PARENT,
                asList(new BigDecimal(parentId)), new IdSelector());
    }

    public void createParameter(long id, BigInteger attribute, BigInteger object) {
        executor.executeUpdate(INSERT_PARAMETER, asList(new BigDecimal(id), new BigDecimal(attribute),
                new BigDecimal(object)));
    }

    public Map<String, Object> readParameter(BigInteger id) {
        return executor.executeSelect(SELECT_PARAMETER, asList(new BigDecimal(id)),
                new ParameterReader());
    }

    public Collection<BigInteger> readParametersByObject(BigInteger object) {
        return executor.executeSelect(SELECT_PARAMETERS_BY_OBJECT, asList(new BigDecimal(object)),
                new IdSelector());
    }

    protected void createValue(long id, int type, String text, Long date, BigInteger reference,
            BigInteger listEntry) {
        Timestamp tsDate = date != null ? new Timestamp(date) : null;
        BigDecimal decimalRef = reference != null ? new BigDecimal(reference) : null;
        BigDecimal decimalEntry = listEntry != null ? new BigDecimal(listEntry) : null;
        executor.executeUpdate(INSERT_VALUE, asList(new BigDecimal(id), new BigDecimal(type),
                text, tsDate, decimalRef, decimalEntry));
    }

    public void createTextValue(long id, String text) {
        createValue(id, 0, text, null, null, null);
    }

    public void createDateValue(long id, Long date) {
        createValue(id, 1, null, date, null, null);
    }

    public void createReferenceValue(long id, BigInteger reference) {
        createValue(id, 2, null, null, reference, null);
    }

    public void createListValue(long id, BigInteger entry) {
        createValue(id, 3, null, null, null, entry);
    }

    public void linkValuesToParameter(long parameter, long... values) {
        for (int i = 0; i < values.length; i++) {
            executor.executeUpdate(LINK_VALUE_TO_PARAMETER, asList(parameter, i, values[i]));
        }
    }

    public Map<String, Object> readValue(BigInteger id) {
        return executor.executeSelect(SELECT_VALUE, asList(new BigDecimal(id)), new ValueReader());
    }

    public List<BigInteger> readValuesByParameter(BigInteger parameter) {
        return executor.executeSelect(SELECT_VALUES_BY_PARAMETER, asList(new BigDecimal(parameter)),
                new IdSelector());
    }

    protected class ObjectReader implements ResultSetHandler<Map<String, Object>> {

        @Override
        public Map<String, Object> handle(TransformingResultSet resultSet) throws SQLException {
            Map<String, Object> result = new HashMap<>();
            if (resultSet.next()) {
                result.put("id", getBigInteger(resultSet.getValue("object_id", BigDecimal.class)));
                result.put("name", resultSet.getValue("name", String.class));
                result.put("description", resultSet.getValue("description", String.class));
                result.put("objectType",
                        getBigInteger(resultSet.getValue("object_type_id", BigDecimal.class)));
                result.put("group",
                        getBigInteger(resultSet.getValue("children_group_id", BigDecimal.class)));
            } else {
                return null;
            }
            if (resultSet.next()) {
                throw new RuntimeException("More than 1 record with this primaryKey");
            }
            return result;
        }
    }

    protected class GroupReader implements ResultSetHandler<Map<String, Object>> {

        @Override
        public Map<String, Object> handle(TransformingResultSet resultSet) throws SQLException {
            Map<String, Object> result = new HashMap<>();
            if (resultSet.next()) {
                result.put("id", getBigInteger(resultSet.getValue("group_id", BigDecimal.class)));
                result.put("parent", getBigInteger(resultSet.getValue("parent_id", BigDecimal.class)));
                result.put("objectType",
                        getBigInteger(resultSet.getValue("object_type_id", BigDecimal.class)));
            } else {
                return null;
            }
            if (resultSet.next()) {
                throw new RuntimeException("More than 1 record with this primaryKey");
            }
            return result;
        }
    }

    protected class ParameterReader implements ResultSetHandler<Map<String, Object>> {

        @Override
        public Map<String, Object> handle(TransformingResultSet resultSet) throws SQLException {
            Map<String, Object> result = new HashMap<>();
            if (resultSet.next()) {
                result.put("id", getBigInteger(resultSet.getValue("parameter_id", BigDecimal.class)));
                result.put("attribute", getBigInteger(resultSet.getValue("attribute_id", BigDecimal.class)));
                result.put("object", getBigInteger(resultSet.getValue("object_id", BigDecimal.class)));
            } else {
                return null;
            }
            if (resultSet.next()) {
                throw new RuntimeException("More than 1 record with this primaryKey");
            }
            return result;
        }
    }

    protected class ValueReader implements ResultSetHandler<Map<String, Object>> {

        @Override
        public Map<String, Object> handle(TransformingResultSet resultSet) throws SQLException {
            Map<String, Object> result = new HashMap<>();
            if (resultSet.next()) {
                result.put("id", getBigInteger(resultSet.getValue("value_id", BigDecimal.class)));
                result.put("type", getInteger(resultSet.getValue("type", BigDecimal.class)));
                result.put("text", resultSet.getValue("text_value", String.class));
                result.put("date", getTime(resultSet.getValue("date_value", Timestamp.class)));
                result.put("reference",
                        getBigInteger(resultSet.getValue("reference_id", BigDecimal.class)));
                result.put("listEntry",
                        getBigInteger(resultSet.getValue("list_entry_id", BigDecimal.class)));
                result.put("parameter",
                        getBigInteger(resultSet.getValue("parameter_id", BigDecimal.class)));
                result.put("order", getInteger(resultSet.getValue("parameter_order", BigDecimal.class)));
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
