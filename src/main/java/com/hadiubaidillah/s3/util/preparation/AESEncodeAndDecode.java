package com.hadiubaidillah.s3.util.preparation;

import com.hadiubaidillah.s3.util.AESUtil;

import javax.crypto.SecretKey;

public class AESEncodeAndDecode {

    public static void main(String[] args) throws Exception {
        // get the key from AESKeyGenerator
        String base64Key = "USTEnty7Yomn8QVKwGNHuCDl+lc01IpybFkcPLNXO9k=";
        SecretKey secretKey = AESUtil.decodeKey(base64Key);

        // encrypt code or message
        String encrypted = AESUtil.encrypt("pemerintahan|image/jpeg,image/png,image/gif", secretKey);
        System.out.println("Encrypted: " + encrypted);

        // decrypt code or message
        String decrypted = AESUtil.decrypt(encrypted, secretKey);
        System.out.println("Decrypted: " + decrypted);


        String data = null;
        try {
            data = "data1".split("\\|")[1];
        } catch (ArrayIndexOutOfBoundsException e) {
            // data remains null
        }

        System.out.println(getData());
    }

    private static boolean getData() {
        return getData(false);
    }

    private static boolean getData(Boolean isTrue) {
        return isTrue;
    }
}


// 2Pm+jRuOFnqhqz0F5J8O8A== (pemerintahan)
// 5u9Q3pGXA8R3cIRZoVUjkw== (perusahaan)
// sqOLOGP9m/aQOByb4z5ihg== (sekolah)

// ZLt2tpnnUUqrnh16vEvzpTDjtWabm/4REZSvXwWs851ULvOpWGYd425qPrG6eu8H (pemerintahan|image/jpeg,image/png,image/gif)