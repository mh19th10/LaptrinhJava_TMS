package db.migration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

/**
 * V2: Cho phép NULL ở teacher_registrations.class_id và thiết lập FK ON DELETE SET NULL.
 * - Tự động dò tên FK hiện có (nếu có) rồi DROP
 * - ALTER COLUMN -> NULL
 * - ADD FK lại với ON DELETE SET NULL, ON UPDATE CASCADE
 */
public class V2__Make_class_id_nullable extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        try (Connection conn = context.getConnection()) {
            conn.setAutoCommit(false);

            String table = "teacher_registrations";
            String col   = "class_id";
            String refTable = "teaching_class";
            String refCol   = "id";

            // 1) Tìm tên FK hiện có trên (table.col)
            String fkName = findForeignKeyName(conn, table, col);

            // 2) Drop FK nếu tồn tại
            if (fkName != null) {
                exec(conn, "ALTER TABLE " + table + " DROP FOREIGN KEY " + fkName);
            }

            // 3) Đổi cột sang NULL (idempotent: chạy nhiều lần cũng ok)
            // Bạn có thể chỉnh BIGINT UNSIGNED nếu DB của bạn dùng UNSIGNED
            exec(conn, "ALTER TABLE " + table + " MODIFY COLUMN " + col + " BIGINT NULL");

            // 4) Thêm FK mới với ON DELETE SET NULL, ON UPDATE CASCADE
            // Đặt tên FK mới ổn định để lần chạy sau có thể drop chính xác
            String newFkName = "fk_" + table + "_" + col;
            exec(conn,
                "ALTER TABLE " + table +
                " ADD CONSTRAINT " + newFkName +
                " FOREIGN KEY (" + col + ") REFERENCES " + refTable + "(" + refCol + ")" +
                " ON DELETE SET NULL ON UPDATE CASCADE"
            );

            conn.commit();
        }
    }

    private static void exec(Connection conn, String sql) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute(sql);
        }
    }

    /**
     * Tìm tên FK trên bảng/column trong information_schema.
     * Trả về null nếu không tìm thấy.
     */
    private static String findForeignKeyName(Connection conn, String table, String column) throws SQLException {
        String sql =
            "SELECT CONSTRAINT_NAME " +
            "FROM information_schema.KEY_COLUMN_USAGE " +
            "WHERE TABLE_SCHEMA = DATABASE() " +
            "  AND TABLE_NAME = ? " +
            "  AND COLUMN_NAME = ? " +
            "  AND REFERENCED_TABLE_NAME IS NOT NULL " +
            "LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, table);
            ps.setString(2, column);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        }
        return null;
    }
}
