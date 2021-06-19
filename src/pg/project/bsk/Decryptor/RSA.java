package pg.project.bsk.Decryptor;


import javax.crypto.Cipher;
import java.security.Key;
import java.util.Base64;

public class RSA {

    private static final String RSA = "RSA";

    private static byte[] encrypt(Key pubkey, String value) {
        try {
            Cipher cipher = Cipher.getInstance(RSA);
            cipher.init(Cipher.ENCRYPT_MODE, pubkey);
            byte[] encrypted = cipher.doFinal(value.getBytes());
            return Base64.getEncoder().encode(encrypted);
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
            byte[] utf8 = rsa.doFinal(Base64.getDecoder().decode(encrypted));
            return new String(utf8, "UTF8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
