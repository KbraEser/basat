package io.basat.auth.token;

import com.nimbusds.jose.jwk.JWKSet;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Publishes the public signing key so other services can verify access tokens. */
@RestController
@RequiredArgsConstructor
public class JwksController {
    private final JWKSet jwkSet;

    @GetMapping("/.well-known/jwks.json")
    public Map<String,Object> keys(){
        // toJSONObject() includes only public key parameters
        return jwkSet.toJSONObject();
    }
}
