package io.basat.auth.token;

import io.basat.auth.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.support.JettyHeadersAdapter;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class JwtTokenService implements TokenService {

    private final JwtEncoder jwtEncoder;
    private final String issuer;
    private final Duration accessTokenTtl;

    public JwtTokenService(JwtEncoder jwtEncoder, @Value("${app.jwt.issuer}") String issuer, @Value("${app.jwt.access-token-ttl}")  Duration accessTokenTtl) {
        this.jwtEncoder = jwtEncoder;
        this.issuer = issuer;
        this.accessTokenTtl = accessTokenTtl;
    }

    @Override
    public String createAccessToken(User user) {
        Instant now =Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiresAt(now.plus(accessTokenTtl))
                .claim("tenantId",user.getTenant().getId().toString())
                .claim("role",user.getRole().name())
                .build();

        JwsHeader header =JwsHeader.with(SignatureAlgorithm.RS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header,claims)).getTokenValue();
    }
}
