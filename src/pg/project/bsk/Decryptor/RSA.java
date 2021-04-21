package pg.project.bsk.Decryptor;

import com.sun.org.apache.xml.internal.security.utils.Base64;

import javax.crypto.Cipher;
import java.security.Key;

public class RSA {

    private static final String RSA = "RSA";

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
}
