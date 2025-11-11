package vn.edu.uth.quanlidaythem.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.DataSource;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Chạy lúc app start:
 * - Nếu teacher_registrations.class_id đang NOT NULL -> tự động:
 *   + drop FK hiện có (nếu có)
 *   + MODIFY class_id -> NULL
 *   + ADD FK lại với ON DELETE SET NULL, ON UPDATE CASCADE
 * - Nếu đã NULL thì bỏ qua.
 */
@Configuration
public class DbFixRunner {

    @Bean
    ApplicationRunner makeClassIdNullable(DataSource ds) {
        return args -> {
            try (Connection conn = ds.getConnection()) {
                conn.setAutoCommit(false);

                final String table   = "teacher_registrations";
                final String col     = "class_id";
                final String refTbl  = "teaching_class";
                final String refCol  = "id";

                // 1) Kiểm tra nullability hiện tại
                boolean isNotNull = false;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT IS_NULLABLE FROM information_schema.COLUMNS " +
                        "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME=? AND COLUMN_NAME=?")) {
                    ps.setString(1, table);
                    ps.setString(2, col);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            isNotNull = "NO".equalsIgnoreCase(rs.getString(1));
                        }
                    }
                }

                if (!isNotNull) {
                    // Đã NULL sẵn -> không làm gì
                    conn.rollback();
                    return;
                }

                // 2) Tìm tên FK để drop nếu có
                String fkName = null;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT CONSTRAINT_NAME " +
                        "FROM information_schema.KEY_COLUMN_USAGE " +
                        "WHERE TABLE_SCHEMA = DATABASE() " +
                        "  AND TABLE_NAME=? AND COLUMN_NAME=? " +
                        "  AND REFERENCED_TABLE_NAME IS NOT NULL LIMIT 1")) {
                    ps.setString(1, table);
                    ps.setString(2, col);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) fkName = rs.getString(1);
                    }
                }
                if (fkName != null) {
                    try (Statement st = conn.createStatement()) {
                        st.execute("ALTER TABLE " + table + " DROP FOREIGN KEY " + fkName);
                    }
                }

                // 3) Đổi cột sang NULL
                try (Statement st = conn.createStatement()) {
                    st.execute("ALTER TABLE " + table + " MODIFY COLUMN " + col + " BIGINT NULL");
                }

                // 4) Thêm lại FK với ON DELETE SET NULL
                String newFk = "fk_" + table + "_" + col;
                try (Statement st = conn.createStatement()) {
                    st.execute("ALTER TABLE " + table +
                            " ADD CONSTRAINT " + newFk +
                            " FOREIGN KEY (" + col + ") REFERENCES " + refTbl + "(" + refCol + ") " +
                            " ON DELETE SET NULL ON UPDATE CASCADE");
                }

                conn.commit();
            } catch (Exception e) {
                // log lỗi nếu cần (không nên chặn app khởi động)
                e.printStackTrace();
            }
        };
    }
}
