package io.basat.monitoring.live;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.security.Principal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class LiveTopicGuardTest {

    private static final UUID TENANT = UUID.randomUUID();
    private static final UUID SITE = UUID.randomUUID();

    private final LiveTopicGuard guard = new LiveTopicGuard();
    private final MessageChannel channel = mock(MessageChannel.class);

    @Test
    void allowsSubscribingToOwnSite() {
        Message<?> message = stomp(StompCommand.SUBSCRIBE,
                StompLiveReadingPublisher.destination(TENANT, SITE), userOf(TENANT));

        assertThat(guard.preSend(message, channel)).isSameAs(message);
    }

    @Test
    void rejectsSubscribingToAnotherTenantsSite() {
        Message<?> message = stomp(StompCommand.SUBSCRIBE,
                StompLiveReadingPublisher.destination(UUID.randomUUID(), SITE), userOf(TENANT));

        assertThatThrownBy(() -> guard.preSend(message, channel))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void rejectsSubscribingWithoutAuthentication() {
        Message<?> message = stomp(StompCommand.SUBSCRIBE,
                StompLiveReadingPublisher.destination(TENANT, SITE), null);

        assertThatThrownBy(() -> guard.preSend(message, channel))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Not authenticated");
    }

    @Test
    void rejectsSendFromClients() {
        Message<?> message = stomp(StompCommand.SEND,
                StompLiveReadingPublisher.destination(TENANT, SITE), userOf(TENANT));

        assertThatThrownBy(() -> guard.preSend(message, channel))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Clients cannot send messages");
    }

    @Test
    void letsOtherCommandsThrough() {
        Message<?> message = stomp(StompCommand.CONNECT, null, userOf(TENANT));

        assertThat(guard.preSend(message, channel)).isSameAs(message);
    }

    private static Message<?> stomp(StompCommand command, String destination, Principal user) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
        accessor.setDestination(destination);
        accessor.setUser(user);
        // inside Spring the headers stay mutable, which getAccessor() relies on
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private static JwtAuthenticationToken userOf(UUID tenantId) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject(UUID.randomUUID().toString())
                .claim("tenantId", tenantId.toString())
                .build();
        return new JwtAuthenticationToken(jwt);
    }
}
