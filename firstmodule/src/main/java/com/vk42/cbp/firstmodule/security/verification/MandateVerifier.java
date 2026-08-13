package com.vk42.cbp.firstmodule.security.verification;

import com.nimbusds.jose.JOSEException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.crypto.RSASSAVerifier;

import java.text.ParseException;

@Service
public class MandateVerifier {
    private final RSAKey jwk;
    public MandateVerifier(@Value("${worker.crypto.public-key}") String publicKey) {
        if (publicKey == null || publicKey.isBlank()) {
            throw new RuntimeException("ERROR: No public key is given");
        }

        try {
            jwk = RSAKey.parse(publicKey);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean verifyPaymentToken(String token) {
        try {
            JWSObject cryptoObject = JWSObject.parse(token);
            RSASSAVerifier verifier = new RSASSAVerifier(jwk.toRSAPublicKey());
            boolean isValid = cryptoObject.verify(verifier);
            if (isValid) {
                // extract information
                String payload = cryptoObject.getPayload().toString();
                System.out.println("payload: " + payload);
            }

            return isValid;
        } catch (ParseException | JOSEException e) {
            throw new RuntimeException(e);
        }
    }
}
