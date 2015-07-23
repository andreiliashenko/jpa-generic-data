package com.anli.generalization.data.utils;

import com.anli.sqlexecution.execution.SqlExecutor;
import java.math.BigDecimal;
import java.math.BigInteger;
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
}
