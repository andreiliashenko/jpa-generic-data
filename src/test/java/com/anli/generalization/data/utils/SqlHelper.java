package com.anli.generalization.data.utils;

import com.anli.sqlexecution.execution.SqlExecutor;
import com.anli.sqlexecution.handling.ResultSetHandler;
import com.anli.sqlexecution.handling.TransformingResultSet;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;
import javax.sql.DataSource;

public class SqlHelper {

    protected final SqlExecutor executor;

    public SqlHelper(DataSource dataSource) {
        executor = new SqlExecutor(dataSource, null);
    }

    protected BigInteger getBigInteger(BigDecimal bigDecimal) {
        return bigDecimal != null ? bigDecimal.toBigIntegerExact() : null;
    }

    protected Integer getInteger(BigDecimal bigDecimal) {
        return bigDecimal != null ? bigDecimal.intValueExact() : null;
    }

    protected BigInteger getTime(Timestamp timestamp) {
        return timestamp != null ? BigInteger.valueOf(timestamp.getTime()) : null;
    }

    protected class IdSelector implements ResultSetHandler<List<BigInteger>> {

        @Override
        public List<BigInteger> handle(TransformingResultSet resultSet) throws SQLException {
            List<BigInteger> ids = new LinkedList<>();
            while (resultSet.next()) {
                ids.add(getBigInteger(resultSet.getValue(1, BigDecimal.class)));
            }
            return ids;
        }
    }
}
