package pg.project.bsk.Decryptor;




import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class AES {

    public enum AesType{
        AES("AES"),
        AES_CBC("AES/CBC/PKCS5Padding"),
        AES_ECB("AES/ECB/PKCS5Padding"),
        AES_OFB2("AES/OFB/PKCS5Padding");

        private String value;

        AesType(String _value){
            this.value = _value;
        }

        public String getValue(){return value;}
    }

    // passphrase: pawellukas
    // salt:       DEF8C2400A6D8225
    // key:        48EF43F24EF8EDA5
    // iv:         D92B535880442EFD

    private static final String key        = "48EF43F24EF8EDA5";
    private static final String initVector = "D92B535880442EFD";


    public static IvParameterSpec generateInitializationVector() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }



    public static byte[] encrypt(byte[] value, AesType method) {
        try {
            IvParameterSpec iv = new IvParameterSpec(
                    initVector.getBytes() // do zmiany //"UTF-8"
            );

            //IvParameterSpec x = generateInitializationVector();
            //SecretKey y = getRandomSecureKey(128);

            SecretKey skeySpec = new SecretKeySpec(
                    key.getBytes(), // do zmiany //"UTF-8"
                    AesType.AES.value);

            Cipher cipher = Cipher.getInstance(method.value);
            if(hasInitVector(method))
                cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            else
                cipher.init(Cipher.ENCRYPT_MODE, skeySpec);

            byte[] base64Bytes = Base64.getEncoder().encode(value);
            byte[] encrypted = cipher.doFinal(base64Bytes);
            return encrypted;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static byte[] decrypt (byte[] encrypted, AesType method) {
        try {
            IvParameterSpec iv = new IvParameterSpec(
                    initVector.getBytes()
            );
            SecretKey skeySpec = new SecretKeySpec(
                    key.getBytes(),
                    AesType.AES.value);

            Cipher cipher = Cipher.getInstance(method.value);
            if(hasInitVector(method))
                cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            else
                cipher.init(Cipher.DECRYPT_MODE, skeySpec);

            byte[] inBase64 = cipher.doFinal(encrypted);
            return Base64.getDecoder().decode(inBase64);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static SecretKey getRandomSecureKey(Integer keySize) {
        KeyGenerator generator;
        try {
            generator = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }

        generator.init(keySize);
        return generator.generateKey();
    }

    static private boolean hasInitVector(AesType aesType){
        if(aesType == AesType.AES_ECB) return false;
        return true;
    }


}