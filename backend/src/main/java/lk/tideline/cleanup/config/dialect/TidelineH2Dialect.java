package lk.tideline.cleanup.config.dialect;

import org.hibernate.dialect.H2Dialect;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;

/**
 * H2 without enum check constraints. Hibernate otherwise writes {@code CHECK (status IN (...))} for
 * every enum column, and {@code ddl-auto: update} never widens it — so adding a status or alert type
 * would break inserts into the saved local database.
 */
public class TidelineH2Dialect extends H2Dialect {

    public TidelineH2Dialect() {
        super();
    }

    public TidelineH2Dialect(DialectResolutionInfo info) {
        super(info);
    }

    @Override
    public String getCheckCondition(String columnName, String[] values) {
        return null;
    }

    @Override
    public String getCheckCondition(String columnName, Class<? extends Enum<?>> enumType) {
        return null;
    }
}
