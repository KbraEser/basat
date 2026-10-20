package io.basat.auth.token;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenTest {

    private final Instant now = Instant.now();

    @Test
    void isActive_returnsTrue_forANewToken() {
        RefreshToken token = new RefreshToken(null, "hash", now.plus(Duration.ofDays(7)));

        assertThat(token.isActive(now)).isTrue();
    }

    @Test
    void isActive_returnsFalse_afterRevoke() {
        RefreshToken token = new RefreshToken(null, "hash", now.plus(Duration.ofDays(7)));

        token.revoke();

        assertThat(token.isActive(now)).isFalse();
    }

    @Test
    void isActive_returnsFalse_whenExpired() {
        RefreshToken token = new RefreshToken(null, "hash", now.plus(Duration.ofDays(7)));

        // checking eight days later, without waiting a week
        assertThat(token.isActive(now.plus(Duration.ofDays(8)))).isFalse();
    }
}
