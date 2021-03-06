package com.fourjs.jwt;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author user
 */
public class JWebToken {

    private static final String SECRET_KEY = "ThisIsMySpecialSecretKey:OU812!";
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    private static final String ISSUER = "jwt.fourjs.com";
    private static final String JWT_HEADER = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
    private JSONObject payload = new JSONObject();
    private String signature;
    private String encodedHeader;
    private String secretKey;

    public static JWebToken CreateToken(String subject, String[] audience, int mins, String secretKey) {

        JSONArray jsonArray = new JSONArray();
        if (audience != null) {
           for (int x = 0; x<audience.length; x++) {
              jsonArray.put(audience[x]);
           }
        }

        long current = ZonedDateTime.now(ZoneOffset.UTC).toInstant().getEpochSecond();
        current += mins * 60;

        return new JWebToken(subject, jsonArray, current, secretKey);

    } 

    private JWebToken() {
        encodedHeader = encode(new JSONObject(JWT_HEADER));
    }

    public JWebToken(JSONObject payload) {
        this(payload.getString("sub"), payload.getJSONArray("aud"), payload.getLong("exp"));
    }

    public JWebToken(String sub, JSONArray aud, long expires, String secretKey) {
        this();
        this.secretKey = secretKey;
        payload.put("sub", sub);
        payload.put("aud", aud);
        payload.put("exp", expires);
        payload.put("iat", ZonedDateTime.now(ZoneOffset.UTC).toInstant().getEpochSecond());
        payload.put("iss", ISSUER);
        payload.put("jti", UUID.randomUUID().toString()); //how do we use this?
        signature = hmacSha256(encodedHeader + "." + encode(payload), getSecretKey());
    }

    public JWebToken(String sub, JSONArray aud, long expires) {
        this(sub, aud, expires, SECRET_KEY);
    } 

    /**
     * For verification
     *
     * @param token
     * @param secretKey
     * @throws java.security.NoSuchAlgorithmException
     */
    public JWebToken(String token, String secretKey) throws NoSuchAlgorithmException {
        this();
        this.secretKey = secretKey;
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid Token format");
        }
        if (encodedHeader.equals(parts[0])) {
            encodedHeader = parts[0];
        } else {
            throw new NoSuchAlgorithmException("JWT Header is Incorrect: " + parts[0]);
        }

        payload = new JSONObject(decode(parts[1]));
        if (payload.isEmpty()) {
            throw new JSONException("Payload is Empty: ");
        }
        if (!payload.has("exp")) {
            throw new JSONException("Payload doesn't contain expiry " + payload);
        }
        signature = parts[2];
    }

    /**
     * For verification
     *
     * @param token
     * @throws java.security.NoSuchAlgorithmException
     */
    public JWebToken(String token) throws NoSuchAlgorithmException {
       this(token, SECRET_KEY);  
    }

    @Override
    public String toString() {
        return encodedHeader + "." + encode(payload) + "." + signature;
    }

    public boolean isValid() {
        return payload.getLong("exp") > (ZonedDateTime.now(ZoneOffset.UTC).toInstant().getEpochSecond()) //token not expired
                && signature.equals(hmacSha256(encodedHeader + "." + encode(payload), getSecretKey())); //signature matched
    }

    public String getSubject() {
        return payload.getString("sub");
    }

    public List<String> getAudience() {
        JSONArray arr = payload.getJSONArray("aud");
        List<String> list = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            list.add(arr.getString(i));
        }
        return list;
    }

    private String getSecretKey() {
       if (secretKey == null) return SECRET_KEY;
       return secretKey;
    }

    private static String encode(JSONObject obj) {
        return encode(obj.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static String encode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String decode(String encodedString) {
        return new String(Base64.getUrlDecoder().decode(encodedString));
    }

    /**
     * Sign with HMAC SHA256 (HS256)
     *
     * @param data
     * @return
     * @throws Exception
     */
    private String hmacSha256(String data, String secret) {
        try {

            //MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = secret.getBytes(StandardCharsets.UTF_8);//digest.digest(secret.getBytes(StandardCharsets.UTF_8));

            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(hash, "HmacSHA256");
            sha256Hmac.init(secretKey);

            byte[] signedBytes = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return encode(signedBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            Logger.getLogger(JWebToken.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return null;
        }
    }

}
