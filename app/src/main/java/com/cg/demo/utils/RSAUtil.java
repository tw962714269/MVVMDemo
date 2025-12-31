package com.cg.demo.utils;

import android.util.Base64;

import com.blankj.utilcode.util.ActivityUtils;
import com.cg.demo.R;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

/**
 * @author:lee
 * @Date:2025/3/25 9:15
 * @Describe: RSA加密工具类
 */
public class RSAUtil {

    public static PublicKey getPublicKeyFromBase64(String base64PublicKey) throws Exception {
        // 移除可能的换行/空格干扰
        String publicKeyPEM = base64PublicKey
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] keyBytes = Base64.decode(publicKeyPEM, Base64.DEFAULT);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    public static String encryptWithPublicKey(String plainText, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance(ActivityUtils.getTopActivity().getString(R.string.login_rsa_ecb)); // 必须与公钥生成时的填充方式一致
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        // 分段加密（针对长文本）
        byte[] inputBytes = plainText.getBytes(StandardCharsets.UTF_8);
        int maxBlockSize = 2048 / 8 - 11; // 2048位密钥，PKCS1Padding 的块大小为 245字节
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        int offset = 0;
        while (offset < inputBytes.length) {
            int length = Math.min(inputBytes.length - offset, maxBlockSize);
            byte[] encryptedBlock = cipher.doFinal(inputBytes, offset, length);
            outputStream.write(encryptedBlock);
            offset += length;
        }

        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP);
    }

    public static String encory(String plainText) {
        try {
            // 转换公钥
            PublicKey publicKey = getPublicKeyFromBase64(ActivityUtils.getTopActivity().getString(R.string.rsa_public_key));
            // 加密数据
            return encryptWithPublicKey(plainText, publicKey);
        } catch (Exception e) {
             com.blankj.utilcode.util.LogUtils.e(e);
        }
        return "";
    }
}
