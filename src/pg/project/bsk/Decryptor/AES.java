package pg.project.bsk.Decryptor;


import com.sun.org.apache.xml.internal.security.utils.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES {
    private static final String key = "aesEncryptionKey";
    private static final String initVector = "encryptionIntVec";

    public static final String AES = "AES";
    public static final String AES_CBC = "AES/CBC/PKCS5Padding";
    public static final String AES_EBC = "AES/EBC/PKCS5Padding";
    public static final String AES_OFB2 = "AES/OFB2/PKCS5Padding";

    public static String encrypt(String value, String method) {
        try {
            IvParameterSpec iv = new IvParameterSpec(
                    initVector.getBytes("UTF-8") // do zmiany
            );
            SecretKeySpec skeySpec = new SecretKeySpec(
                    key.getBytes("UTF-8"), // do zmiany
                    AES);

            Cipher cipher = Cipher.getInstance(method);
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(value.getBytes());
            return Base64.encode(encrypted);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String decrypt(String encrypted, String method) {
        try {
            IvParameterSpec iv = new IvParameterSpec(
                    initVector.getBytes("UTF-8")
            );
            SecretKeySpec skeySpec = new SecretKeySpec(
                    key.getBytes("UTF-8"),
                    AES);

            Cipher cipher = Cipher.getInstance(method);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] original = cipher.doFinal(Base64.decode(encrypted));

            return new String(original);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

}