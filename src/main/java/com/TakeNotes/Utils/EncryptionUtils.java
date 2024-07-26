package com.TakeNotes.Utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class EncryptionUtils {
    private static final String AES = "AES";
    private static final String AES_GCM_NO_PADDING = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;
    private final SecretKey secretKey;

    public EncryptionUtils(String key) {
        this.secretKey = new SecretKeySpec(Base64.getDecoder().decode(key), AES);
    }

    public String encrypt(String data) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
        byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        byte[] encryptedIvAndData = ByteBuffer.allocate(iv.length + encryptedData.length)
                .put(iv)
                .put(encryptedData)
                .array();
        return Base64.getEncoder().encodeToString(encryptedIvAndData);
    }

    public String decrypt(String encryptedData) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
        ByteBuffer byteBuffer = ByteBuffer.wrap(Base64.getDecoder().decode(encryptedData));
        byte[] iv = new byte[GCM_IV_LENGTH];
        byteBuffer.get(iv);
        byte[] encryptedDataBytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(encryptedDataBytes);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
        byte[] decryptedData = cipher.doFinal(encryptedDataBytes);
        return new String(decryptedData, StandardCharsets.UTF_8);
    }
}
