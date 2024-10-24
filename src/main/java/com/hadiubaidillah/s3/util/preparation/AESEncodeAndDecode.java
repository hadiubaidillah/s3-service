package com.hadiubaidillah.s3.util.preparation;

import com.hadiubaidillah.s3.util.AESUtil;

import javax.crypto.SecretKey;

public class AESEncodeAndDecode {

    public static void main(String[] args) throws Exception {
        // get the key from AESKeyGenerator
        String base64Key = "USTEnty7Yomn8QVKwGNHuCDl+lc01IpybFkcPLNXO9k=";
        SecretKey secretKey = AESUtil.decodeKey(base64Key);

        // encrypt code or message
        String encrypted = AESUtil.encrypt("pemerintahan", secretKey);
        System.out.println("Encrypted: " + encrypted);

        // decrypt code or message
        String decrypted = AESUtil.decrypt(encrypted, secretKey);
        System.out.println("Decrypted: " + decrypted);
    }
}


// 2Pm+jRuOFnqhqz0F5J8O8A== (pemerintahan)
// 5u9Q3pGXA8R3cIRZoVUjkw== (perusahaan)
// sqOLOGP9m/aQOByb4z5ihg== (sekolah)