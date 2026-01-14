package fakehunters.backend.global.mybatis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.*;

public class JsonNodeTypeHandler extends BaseTypeHandler<JsonNode> {
    private static final ObjectMapper om = new ObjectMapper();

    @Override
    public void setNonNullParameter(
            PreparedStatement ps, int i, JsonNode parameter, JdbcType jdbcType
    ) throws SQLException {
        ps.setObject(i, parameter.toString(), Types.OTHER);
    }

    @Override
    public JsonNode getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return read(rs.getString(columnName));
    }

    @Override
    public JsonNode getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return read(rs.getString(columnIndex));
    }

    @Override
    public JsonNode getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return read(cs.getString(columnIndex));
    }

    private JsonNode read(String json) throws SQLException {
        if (json == null) return null;
        try {
            return om.readTree(json);
        } catch (Exception e) {
            throw new SQLException("Invalid JSON", e);
        }
    }
}
