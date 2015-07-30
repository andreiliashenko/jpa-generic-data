package com.anli.generalization.data.utils;

import com.anli.generalization.data.entities.metadata.AttributeType;
import com.anli.sqlexecution.handling.ResultSetHandler;
import com.anli.sqlexecution.handling.TransformingResultSet;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

import static java.util.Arrays.asList;

public class AttributeHelper extends SqlHelper {

    private static final String INSERT_ATTRIBUTE = "insert into attributes "
            + "(attribute_id, type, name, multiple, reference_type_id) "
            + "values (?, ?, ?, ?, ?)";
    private static final String LINK_ATTRIBUTE_TO_OBJECT_TYPE = "update attributes "
            + "set object_type_id = ?, object_type_order = ? where attribute_id = ?";
    private static final String SELECT_ATTRIBUTE = "select attribute_id, type, "
            + "name, multiple, reference_type_id, object_type_id, object_type_order "
            + "from attributes where attribute_id = ?";
    private static final String SELECT_ATTR_IDS_BY_OBJECT_TYPE = "select attribute_id "
            + "from attributes where object_type_id = ? order by object_type_order";

    public AttributeHelper(DataSource dataSource) {
        super(dataSource);
    }

    public void createAttribute(long id, AttributeType type, String name, Boolean multiple,
            BigInteger referenceTypeId) {
        Integer integerType = type != null ? type.ordinal() : null;
        Integer integerMultiple = multiple != null ? (multiple ? 1 : 0) : null;
        BigDecimal decimalReferenceTypeId = referenceTypeId != null
                ? new BigDecimal(referenceTypeId) : null;
        executor.executeUpdate(INSERT_ATTRIBUTE, asList(id, integerType, name, integerMultiple,
                decimalReferenceTypeId));
    }

    public void linkAttributesToObjectType(long objectTypeId, long... attributeIds) {
        for (int i = 0; i < attributeIds.length; i++) {
            executor.executeUpdate(LINK_ATTRIBUTE_TO_OBJECT_TYPE,
                    asList(objectTypeId, i, attributeIds[i]));
        }
    }

    public Map<String, Object> readAttribute(BigInteger id) {
        return executor.executeSelect(SELECT_ATTRIBUTE, asList(new BigDecimal(id)), new AttributeReader());
    }

    public List<BigInteger> readAttributesByObjectType(BigInteger objectTypeId) {
        return executor.executeSelect(SELECT_ATTR_IDS_BY_OBJECT_TYPE, asList(new BigDecimal(objectTypeId)),
                new IdSelector());
    }

    protected class AttributeReader implements ResultSetHandler<Map<String, Object>> {

        @Override
        public Map<String, Object> handle(TransformingResultSet resultSet) throws SQLException {
            Map<String, Object> result = new HashMap<>();
            if (resultSet.next()) {
                result.put("id", getBigInteger(resultSet.getValue("attribute_id", BigDecimal.class)));
                result.put("type", getInteger(resultSet.getValue("type", BigDecimal.class)));
                result.put("name", resultSet.getValue("name", String.class));
                result.put("multiple", getInteger(resultSet.getValue("multiple", BigDecimal.class)));
                result.put("referenceType",
                        getBigInteger(resultSet.getValue("reference_type_id", BigDecimal.class)));
                result.put("objectType",
                        getBigInteger(resultSet.getValue("object_type_id", BigDecimal.class)));
                result.put("order", getInteger(resultSet.getValue("object_type_order", BigDecimal.class)));
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
