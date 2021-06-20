package pg.project.bsk.Decryptor;


import com.sun.org.apache.xml.internal.security.utils.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

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

    private static String key = "";
    private static String initVector = "encryptionIntVec";


    public static void setKey(String _key){
        key = _key;

    }



    public static String encrypt(String value, AesType method) {
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
            byte[] encrypted = cipher.doFinal(value.getBytes());
            return Base64.encode(encrypted);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String decrypt(String encrypted, AesType method) {
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
            byte[] original = cipher.doFinal(Base64.decode(encrypted));
            return new String(original);
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