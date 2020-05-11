package cn.haseo.radar.util;

import android.annotation.SuppressLint;
import android.util.Base64;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/**
 * 该类用于对信息进行 DES 加密和解密
 */
public class DesUtil {
    // 定义密钥
    private final static String key = "哼！就不告诉你";


    /**
     * 该方法使用默认密钥加密数据
     * @param data：需要加密的数据
     * @return 密文
     * @throws Exception：异常
     */
    public static String encryptData(String data) throws Exception {
        byte[] bt = encrypt(data.getBytes("GBK"));
        byte[] buf = Base64.encode(bt, Base64.DEFAULT);
        return new String (buf, "GBK");
    }

    /**
     * 该方法根据键值进行加密
     * @param data：明文字节数组
     * @return 密文字节数组
     * @throws Exception：异常
     */
    @SuppressLint("GetInstance")
    private static byte[] encrypt(byte[] data) throws Exception {
        // 从原始密钥数据创建 DESKeySpec 对象
        DESKeySpec keySpec = new DESKeySpec(key.getBytes("GBK"));
        // 创建一个密钥工厂，然后用它把 DESKeySpec 转换成 SecretKey 对象
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey secretKey = keyFactory.generateSecret(keySpec);

        // Cipher 对象实际完成加密操作
        Cipher cipher = Cipher.getInstance("DES");

        // 生成一个可信任的随机数源
        SecureRandom random = new SecureRandom();
        // 用密钥初始化 Cipher 对象
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, random);

        return cipher.doFinal(data);
    }


    /**
     * 该方法使用默认密钥解密数据
     * @param data：需要解密的数据
     * @return 明文
     * @throws Exception：异常
     */
    public static String decryptData(String data) throws Exception {
        byte[] buf = Base64.decode(data, Base64.DEFAULT);
        byte[] bt = decrypt(buf);
        return new String(bt, "GBK");
    }

    /**
     * 该方法根据键值进行解密
     * @param data：密文字节数组
     * @return 明文字节数组
     * @throws Exception：异常
     */
    @SuppressLint("GetInstance")
    private static byte[] decrypt(byte[] data) throws Exception {
        // 从原始密钥数据创建 DESKeySpec 对象
        DESKeySpec keySpec = new DESKeySpec(key.getBytes("GBK"));
        // 创建一个密钥工厂，然后用它把 DESKeySpec 转换成 SecretKey 对象
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey secretKey = keyFactory.generateSecret(keySpec);

        // Cipher对象实际完成解密操作
        Cipher cipher = Cipher.getInstance("DES");
        // 生成一个可信任的随机数源
        SecureRandom random = new SecureRandom();
        // 用密钥初始化Cipher对象
        cipher.init(Cipher.DECRYPT_MODE, secretKey, random);

        return cipher.doFinal(data);
    }
}
