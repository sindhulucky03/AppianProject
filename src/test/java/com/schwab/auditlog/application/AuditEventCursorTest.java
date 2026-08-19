package com.schwab.auditlog.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class AuditEventCursorTest {

    @Test
    void roundTripsAnOpaqueCursor() {
        AuditEventCursor cursor = new AuditEventCursor(Instant.parse("2026-08-18T10:15:30Z"), 42);

        assertThat(AuditEventCursor.decode(cursor.encode())).isEqualTo(cursor);
    }

    @Test
    void rejectsMalformedCursor() {
        assertThatThrownBy(() -> AuditEventCursor.decode("not-a-cursor"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid cursor");
    }
}
