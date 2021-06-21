package pg.project.bsk.Decryptor;

import pg.project.bsk.Controller.Controller;

import javax.crypto.Cipher;
import java.io.*;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Objects;

public class RSA {

    private static final String RSA = "RSA";

    private static PublicKey publicKey;
    private static PrivateKey privateKey;

    public static void generateRSAKeys() throws Exception {
        KeyPairGenerator factory;
        try {
            factory = KeyPairGenerator.getInstance(RSA);
            factory.initialize(2048);
        }
        catch (NoSuchAlgorithmException e) {
            throw new Exception("Cannot create KeyPairGenerator for provided algorithm");
        }

        KeyPair pair = factory.generateKeyPair();
        publicKey = pair.getPublic();
        privateKey = pair.getPrivate();
        saveKeysToFile();
    }

    public static PublicKey getPublicKey(){
        return publicKey;
    }

    public static PrivateKey getPrivateKey(){
        return privateKey;
    }

    public static String encryptWithPublicKey(String value){
        return encrypt(publicKey, value);
    }

    public static String decryptWithPublicKey(String value){
        return decrypt(publicKey, value);
    }

    public static String encryptWithPrivateKey(String value){
        return encrypt(privateKey, value);
    }

    public static String decryptWithPrivateKey(String value){
        return decrypt(privateKey, value);
    }

    public static String encryptWithKey(byte[] message, String key){
        return encryptSessionKey(key,message);
    }

    private static String encryptSessionKey(String key, byte[] message){
        try {

            byte[] encoded = Base64.getDecoder().decode(key);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey pubKey = keyFactory.generatePublic(keySpec);

            Cipher cipher = Cipher.getInstance(RSA);
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);
            byte[] encrypted = cipher.doFinal(message);
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String encrypt(Key encryptionKey, String value) {
        try {
            Cipher cipher = Cipher.getInstance(RSA);
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey);
            byte[] encrypted = cipher.doFinal(value.getBytes());
            return new String(Base64.getEncoder().encode(encrypted));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String decrypt(Key decryptionKey, String encrypted) {
        try {
            Cipher rsa = Cipher.getInstance(RSA);
            rsa.init(Cipher.DECRYPT_MODE, decryptionKey);
            byte[] utf8 = rsa.doFinal(Base64.getDecoder().decode(encrypted));
            return Base64.getEncoder().encodeToString(utf8);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void saveKeysToFile() throws Exception{

        File publicDir = new File(Controller.getPublicKeyDirectory());
        File privateDir = new File(Controller.getPrivateKeyDirectory());
        if(!publicDir.exists()) publicDir.mkdirs();
        if(!privateDir.exists()) privateDir.mkdirs();

        Base64.Encoder encoder = Base64.getEncoder();

        Writer out = new FileWriter( Controller.getPublicKeyDirectory() + "rsaPublicKey");
        out.write(encoder.encodeToString(publicKey.getEncoded()));
        out.close();

        out = new FileWriter( Controller.getPrivateKeyDirectory() + "rsaPrivateKey");
        out.write(encoder.encodeToString(privateKey.getEncoded()));
        out.close();
    }

    public static void removeKeysDirectories(){
        File publicDir = new File(Controller.getPublicKeyDirectory());
        File privateDir = new File(Controller.getPrivateKeyDirectory());
        if(publicDir.exists())
            for(File file: Objects.requireNonNull(publicDir.listFiles()))
                if (!file.isDirectory())
                    file.delete();
        if(privateDir.exists())
            for(File file: Objects.requireNonNull(privateDir.listFiles()))
                if (!file.isDirectory())
                    file.delete();
    }

}
