package lk.tideline.cleanup.config.dialect;

import org.hibernate.dialect.MySQLDialect;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;

/** MySQL without enum check constraints, for the same reason as {@link TidelineH2Dialect}. */
public class TidelineMySQLDialect extends MySQLDialect {

    public TidelineMySQLDialect() {
        super();
    }

    public TidelineMySQLDialect(DialectResolutionInfo info) {
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
