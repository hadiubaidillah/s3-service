package com.hadiubaidillah.s3.util.preparation;

import com.hadiubaidillah.s3.util.AESUtil;

import java.util.Base64;

public class AESKeyGenerator {

    public static void main(String[] args) throws Exception {
        // Print the Base64-encoded key
        System.out.println("Base64-encoded AES key: " + Base64.getEncoder().encodeToString(AESUtil.generateKey().getEncoded()));
    }
}
