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

public class ObjectTypeHelper extends SqlHelper {

    private static final String INSERT_OBJECT_TYPE = "insert into object_types "
            + "(object_type_id, name) values (?, ?)";
    private static final String LINK_OBJECT_TYPE_TO_PARENT = "update object_types "
            + "set parent_type_id = ? where object_type_id = ?";
    private static final String SELECT_OBJECT_TYPE = "select object_type_id, name, parent_type_id "
            + "from object_types where object_type_id = ?";
    private static final String SELECT_OBJECT_TYPE_IDS_BY_PARENT = "select object_type_id from object_types "
            + "where parent_type_id = ? order by object_type_id";

    public ObjectTypeHelper(DataSource dataSource) {
        super(dataSource);
    }

    public void createObjectType(long id, String name) {
        executor.executeUpdate(INSERT_OBJECT_TYPE, asList(id, name));
    }

    public void linkObjectTypesToParent(long parentId, long... typeIds) {
        for (long typeId : typeIds) {
            executor.executeUpdate(LINK_OBJECT_TYPE_TO_PARENT, asList(parentId, typeId));
        }
    }

    public Map<String, Object> readObjectType(BigInteger id) {
        return executor.executeSelect(SELECT_OBJECT_TYPE, asList(new BigDecimal(id)), new ObjectTypeReader());
    }

    public Collection<BigInteger> readObjectTypesByParent(BigInteger parentId) {
        return executor.executeSelect(SELECT_OBJECT_TYPE_IDS_BY_PARENT, asList(new BigDecimal(parentId)),
                new IdSelector());
    }

    protected class ObjectTypeReader implements ResultSetHandler<Map<String, Object>> {

        @Override
        public Map<String, Object> handle(TransformingResultSet resultSet) throws SQLException {
            Map<String, Object> result = new HashMap<>();
            if (resultSet.next()) {
                result.put("id", getBigInteger(resultSet.getValue("object_type_id", BigDecimal.class)));
                result.put("name", resultSet.getValue("name", String.class));
                result.put("parentType",
                        getBigInteger(resultSet.getValue("parent_type_id", BigDecimal.class)));
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
