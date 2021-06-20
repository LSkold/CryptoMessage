package pg.project.bsk.Decryptor;


import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
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

    private static String key = "aesEncryptionKey";
    private static String initVector = "encryptionIntVec";


    public static void setKey(String _key){
        key = _key;

    }



    public static String encrypt(byte[] value, AesType method) {
        try {
            IvParameterSpec iv = new IvParameterSpec(
                    initVector.getBytes("UTF-8") // do zmiany
            );
            SecretKeySpec skeySpec = new SecretKeySpec(
                    key.getBytes("UTF-8"), // do zmiany
                    AesType.AES.value);

            Cipher cipher = Cipher.getInstance(method.value);
            if(hasInitVector(method)) cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            else cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            byte[] decrypted = cipher.doFinal(value);
            return Base64.getEncoder().encodeToString(decrypted);//.encode(encrypted);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static byte[] decrypt(byte[] encrypted, AesType method) {
        try {
            IvParameterSpec iv = new IvParameterSpec(
                    initVector.getBytes("UTF-8")
            );
            SecretKeySpec skeySpec = new SecretKeySpec(
                    key.getBytes("UTF-8"),
                    AesType.AES.value);

            Cipher cipher = Cipher.getInstance(method.value);
            if(hasInitVector(method)) cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            else cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            return cipher.doFinal(Base64.getDecoder().decode(encrypted));
            //return new String(original);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    static private boolean hasInitVector(AesType aesType){
        if(aesType == AesType.AES_ECB) return false;
        return true;
    }


}