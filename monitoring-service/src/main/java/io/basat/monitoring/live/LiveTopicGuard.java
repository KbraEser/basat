package io.basat.monitoring.live;

import io.basat.monitoring.common.TokenClaims;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Lets a user subscribe only to the live topics of their own company. */
@Component
public class LiveTopicGuard implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }
        // the broker would forward a client's SEND to every subscriber, so clients may only listen
        if (StompCommand.SEND.equals(accessor.getCommand())) {
            throw new AccessDeniedException("Clients cannot send messages");
        }
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            if (!(accessor.getUser() instanceof JwtAuthenticationToken authentication)) {
                throw new AccessDeniedException("Not authenticated");
            }
            UUID tenantId = TokenClaims.tenantId(authentication.getToken());
            if (!StompLiveReadingPublisher.isTopicOf(tenantId, accessor.getDestination())) {
                throw new AccessDeniedException("Cannot subscribe to " + accessor.getDestination());
            }
        }
        return message;
    }
}