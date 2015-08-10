package com.anli.generalization.data.utils;

import com.anli.sqlexecution.handling.ResultSetHandler;
import com.anli.sqlexecution.handling.TransformingResultSet;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
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
}
