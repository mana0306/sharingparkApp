package com.lzj.its.sharingpark.util;

import android.util.Base64;

/**
 * 使用Base64来保存和获取密码数据
 */
public class Base64Utils {


    /**
     * BASE64解密
     *
     * @param key 待解密的串
     * @return 解密后的string串
     */
    public static String decryptBASE64(String key) {
        int decodeTime = 5;//压缩和解压的次数，防止被简单破解
        key = key.trim().replace(" ", "");//去掉空格
        while (decodeTime > 0) {
            key = new String(Base64.decode(key, Base64.DEFAULT));
            decodeTime--;
        }
        return key;//如果出现乱码可以改成： String(bt, "utf-8")或 gbk
    }

    /**
     * BASE64加密
     *
     * @param key 待加密的串
     * @return 加密后的串
     *
     */
    public static String encryptBASE64(String key) {
        int decodeTime = 5;//压缩和解压的次数，防止被简单破解
        key = key.trim().replace(" ", "");//去掉空格
        while (decodeTime > 0) {
            key = Base64.encodeToString(key.getBytes(), Base64.DEFAULT);
            decodeTime--;
        }
        return key;
    }
}
