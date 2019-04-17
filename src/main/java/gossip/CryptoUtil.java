import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.MessageDigest;

import java.nio.charset.StandardCharsets;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;


import sun.misc.BASE64Encoder;

public class CryptoUtil {
     private static String getHash(String message) throws NoSuchAlgorithmException {
       MessageDigest md = MessageDigest.getInstance("SHA-256");
 
       md.update(message.getBytes(StandardCharsets.UTF_8));
       byte[] digest = md.digest();
 
       return String.format("%064x", new BigInteger(1, digest));
     }   

     private static String getSignature(String message, PrivateKey key) throws NoSuchAlgorithmException, UnsupportedEncodingException, SignatureException, InvalidKeyException {
       Signature sig = Signature.getInstance("SHA1WithRSA");
       sig.initSign(key);
       sig.update(message.getBytes("UTF-8"));
       byte[] signatureBytes = sig.sign();
       return new BASE64Encoder().encode(signatureBytes);
     }

    // Test method, refactor it later.
    public static void main(String[] args) throws Exception {

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(1024);
        KeyPair kp = kpg.genKeyPair();


  
        System.out.println(getHash("test"));

        String data = getSignature("test", kp.getPrivate());
        System.out.println(data);
    }
}
