package pg.project.bsk.Decryptor;

import com.sun.org.apache.xml.internal.security.utils.Base64;
import pg.project.bsk.Controller.Controller;

import javax.crypto.Cipher;
import javax.naming.ldap.Control;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;
import java.util.Objects;

public class RSA {

    private static final String RSA = "RSA";

    private static PublicKey publicKey;
    private static PrivateKey privateKey;


    public static boolean generateRSAKeys() throws Exception {
        KeyPairGenerator factory;
        try {
            factory = KeyPairGenerator.getInstance(RSA);
        }
        catch (NoSuchAlgorithmException e) {
            throw new Exception("Cannot create KeyPairGenerator for provided algorithm");
        }

        KeyPair pair = factory.generateKeyPair();
        publicKey = pair.getPublic();
        privateKey = pair.getPrivate();

        saveKeysToFile();
        return true;
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


    private static String encrypt(Key pubkey, String value) {
        try {
            Cipher cipher = Cipher.getInstance(RSA);
            cipher.init(Cipher.ENCRYPT_MODE, pubkey);
            byte[] encrypted = cipher.doFinal(value.getBytes());
            return Base64.encode(encrypted);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String decrypt(Key decryptionKey, String encrypted) {
        try {
            Cipher rsa;
            rsa = Cipher.getInstance("RSA");
            rsa.init(Cipher.DECRYPT_MODE, decryptionKey);
            byte[] utf8 = rsa.doFinal(Base64.decode(encrypted));
            return new String(utf8, "UTF8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void saveKeysToFile() throws Exception{

        File publicDir = new File(Controller.getPublicKeyDirectory());
        File privateDir = new File(Controller.getPrivateKeyDirectory());
        if(publicDir.exists())
            publicDir.delete();
        publicDir.mkdirs();
        if(!privateDir.exists())
            privateDir.mkdirs();

        DataOutputStream dos = null;
        try {
            dos = new DataOutputStream(new FileOutputStream(
                    Controller.getPublicKeyDirectory() + "rsaPublicKey"
            ));
            dos.write(publicKey.getEncoded());
            dos.flush();
        }
        finally {
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            dos = new DataOutputStream(new FileOutputStream(
                    Controller.getPrivateKeyDirectory() + "rsaPrivateKey"
            ));
            dos.write(privateKey.getEncoded());
            dos.flush();
        }
        finally {
            if (dos != null)
                try {
                    dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
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
