package io.basat.auth.token;

import io.basat.auth.common.Hashing;
import io.basat.auth.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private final RefreshTokenRepository refreshTokenRepository;
    private final Duration refreshTokenTtl;

    public RefreshTokenServiceImpl(RefreshTokenRepository refreshTokenRepository,  @Value("${app.jwt.refresh-token-ttl}") Duration refreshTokenTtl) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenTtl = refreshTokenTtl;
    }

    @Override
    @Transactional
    public String create(User user) {
        String rawToken = generateRawToken();
        refreshTokenRepository.save(new RefreshToken(user, Hashing.sha256Hex(rawToken), Instant.now().plus(refreshTokenTtl)));
        return rawToken;
    }

    @Override
    @Transactional
    public RotationResult rotate(String rawToken) {
        RefreshToken current = refreshTokenRepository.findByTokenHash(Hashing.sha256Hex(rawToken))
                .filter(token -> token.isActive(Instant.now()))
                .orElseThrow(InvalidRefreshTokenException::new);

        current.revoke();
        User user = current.getUser();
        return new RotationResult(user, create(user));
    }

    @Override
    @Transactional
    public void revoke(String rawToken) {
        refreshTokenRepository.findByTokenHash(Hashing.sha256Hex(rawToken))
                .ifPresent(RefreshToken::revoke);

    }

    private static String generateRawToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }



}
