package main.java.gossip;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.MessageDigest;
import java.security.interfaces.RSAPublicKey;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.interfaces.RSAPrivateKey;
import java.security.GeneralSecurityException;
import java.io.IOException;
import java.io.File;

import java.nio.charset.StandardCharsets;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

import java.io.InputStream;
import java.io.FileInputStream;
import java.util.Base64;
import java.io.DataInputStream;

import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.security.spec.*;

/**
 * CryptoUtil exposes APIs to easily sign 
 * and verify a Network message.
 *
 * @author  Vaibhav Singh
 * @version 1.0
 * @since   2019-02-26
 */
public class CryptoUtil {

    /**
     * getHash returns the SHA-256 Hash of an input message.
     *
     * @param message Message to be hashed
     * @return Hashed String
     * @exception NoSuchAlgorithmException never thrown
     * @see NoSuchAlgorithmException
     */
     private static String getHash(String message) throws NoSuchAlgorithmException {
       MessageDigest md = MessageDigest.getInstance("SHA-256");
 
       md.update(message.getBytes(StandardCharsets.UTF_8));
       byte[] digest = md.digest();
 
       return String.format("%064x", new BigInteger(1, digest));
     }   

     /** 
      * getSignature returns the signed message.
      *
      * @param message Message to be signed.
      * @param key PrivateKey object used to create the signature.
      * @return Base64 encoded message signature
      * @exception NoSuchAlgorithmException, UnsupportedEncodingException, SignatureException, InvalidKeyException
      * @see NoSuchAlgorithmException, UnsupportedEncodingException, SignatureException, InvalidKeyException
      */
     public static String getSignature(String message, PrivateKey key) throws NoSuchAlgorithmException, UnsupportedEncodingException, SignatureException, InvalidKeyException {
        Signature sig = Signature.getInstance("SHA1WithRSA");
        sig.initSign(key);
        sig.update(message.getBytes("UTF-8"));
        byte[] signatureBytes = sig.sign();
        return Base64.getEncoder().encodeToString(signatureBytes);
      }  

      /** 
       * verifySignature verifies the signature on the message.
       *
       * @param message Message to be verified.
       * @param sign signature of the Message
       * @param key PublicKey object used to verify the signature.
       * @return boolean whether the signature was correct
       * @exception NoSuchAlgorithmException, UnsupportedEncodingException, SignatureException, InvalidKeyException
       * @see NoSuchAlgorithmException, UnsupportedEncodingException, SignatureException, InvalidKeyException
       */
      public static boolean verifySignature(String message, String sign, PublicKey key) throws NoSuchAlgorithmException, UnsupportedEncodingException, SignatureException, InvalidKeyException {
        Signature sig = Signature.getInstance("SHA1WithRSA");
        sig.initVerify(key);
        sig.update(message.getBytes("UTF-8"));
        return sig.verify(Base64.getDecoder().decode(sign));
      }  

      /** 
       * getPrivate gets the Private Key from a specified location.
       *
       * @param filename location of the file 
       * @return private key
       * @exception Exception
       */
  public static PrivateKey getPrivate(String filename) throws Exception {

    byte[] keyBytes = Files.readAllBytes(Paths.get(filename));

    PKCS8EncodedKeySpec spec =
      new PKCS8EncodedKeySpec(keyBytes);
    KeyFactory kf = KeyFactory.getInstance("RSA");
    return kf.generatePrivate(spec);
  }

/** 
 * getPublic gets the public key.
 *
 * @param filename name of the Public Key file.
 * @return JCE PublicKey object representing the public key
 * @exception Exception
 */  
 public static PublicKey getPublic(String filename)
    throws Exception {

    byte[] keyBytes = Files.readAllBytes(Paths.get(filename));

    X509EncodedKeySpec spec =
      new X509EncodedKeySpec(keyBytes);
    KeyFactory kf = KeyFactory.getInstance("RSA");
    return kf.generatePublic(spec);
  }
  
    // Test method, refactor it later.
    public static void test(String[] args) throws Exception {

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(1024);
        KeyPair kp = kpg.genKeyPair();


  
        System.out.println(getHash("test"));

        String data = getSignature("test", kp.getPrivate());
        System.out.println(data);

        System.out.println(verifySignature("test", data,kp.getPublic()));
    }
}

