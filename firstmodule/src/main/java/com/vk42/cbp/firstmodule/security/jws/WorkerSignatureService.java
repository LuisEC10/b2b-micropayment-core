package com.vk42.cbp.firstmodule.security.jws;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.ParseException;

@Service
public class WorkerSignatureService {
    private final RSAKey jwk;

    public WorkerSignatureService(@Value("${worker.crypto.private-key}") String privateKey) {
        {
            try {
                jwk = RSAKey.parse(privateKey);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public String generateJwsForPayment(String paymentId, String newState) {
        String jsonString = String.format("{\"payment_id\": \"%s\", \"status\": \"%s\"}", paymentId, newState);
        JWSObject jwsObject = new JWSObject(
                new JWSHeader(JWSAlgorithm.RS256), new Payload(jsonString)
        );

        try {
            RSASSASigner rsassaSigner = new RSASSASigner(jwk.toRSAPrivateKey());
            jwsObject.sign(rsassaSigner);
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("ERROR: Bad sign on payment payload: ", e);
        }
    }
}
