package com.agritainment.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class IndexInitializer {

    private final JdbcTemplate jdbc;

    private static final String[][] INDEXES = {
            {"idx_users_phone", "users(phone)"},
            {"idx_users_role", "users(role)"},
            {"idx_reservations_user_date", "table_reservations(user_id, reservation_date)"},
            {"idx_reservations_table_date", "table_reservations(table_id, reservation_date, time_slot)"},
            {"idx_orders_table_status", "`orders`(table_id, status)"},
            {"idx_coupons_user_status", "coupons(user_id, status)"},
            {"idx_coupons_code", "coupons(code)"},
            {"idx_plots_status", "plots(status)"},
            {"idx_journals_user", "journals(user_id)"},
    };

    @EventListener(ApplicationReadyEvent.class)
    public void createIndexes() {
        for (String[] idx : INDEXES) {
            String name = idx[0];
            String def = idx[1];
            try {
                Integer count = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND index_name = ?",
                        Integer.class, name);
                if (count != null && count == 0) {
                    jdbc.execute("CREATE INDEX " + name + " ON " + def);
                    log.info("Created index: {}", name);
                }
            } catch (Exception e) {
                log.warn("Skip index {}: {}", name, e.getMessage());
            }
        }
    }
}
