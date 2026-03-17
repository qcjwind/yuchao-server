package com.hzm.yuchao.simple.utils;

import java.nio.ByteBuffer;

public class ByteUtils {

    public static void main(String[] args) {

        System.out.println(ByteBuffer.wrap(new byte[]{0, 0x0C}).getShort());

        System.out.println(byteArrayToShort(new byte[]{0, 0x0C}, 0));
        byte[] bytes = {72, 101, 108, 108, 111};

        String asciiString = byteToAsciiString(bytes);
        System.out.println(asciiString); // 输出: Hello
    }

    /**
     * 不包含to
     *
     * @param bytes
     * @param from
     * @return
     */
    public static short byteArrayToShort(byte[] bytes, int from) {
//        return (int) (((src[offset] & 0xFF)<<24)
//                |((src[offset+1] & 0xFF)<<16)
//                |((src[offset+2] & 0xFF)<<8)
//                |(src[offset+3] & 0xFF));

        return (short) (((bytes[from] & 0xFF) << 8) | (bytes[from + 1] & 0xFF));

//        return ByteBuffer.wrap(Arrays.copyOfRange(bytes, from, from + length)).getShort();
    }

    /**
     * 不包含to
     *
     * @param bytes
     * @param from
     * @return
     */
    public static String byteToAsciiString(byte[] bytes, int from, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < from + length; i++) {
            char c = (char) bytes[i];
            sb.append(c);
        }
        return sb.toString();
    }

    public static String byteToHexString(byte[] bytes, int from, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < from + length; i++) {
            sb.append(byteToHex(bytes[i]));
        }
        return sb.toString();
    }

    public static String byteToAsciiString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            char c = (char) b;
            sb.append(c);
        }
        return sb.toString();
    }

    public static boolean isImei(byte[] bytes) {
        if (bytes.length != 15) {
            return false;
        }

        for (byte b : bytes) {
            // 48 -> 0; 57 -> 9
            if (b > 57 || b < 48) {
                return false;
            }
        }
        return true;
    }

    public static boolean isDevice(String deviceId) {
        if (deviceId.length() < 10) {
            return false;
        }

        for (byte b : deviceId.getBytes()) {
            // 48 -> 0; 57 -> 9
            if (b > 122 || b < 30) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasOk(byte[] bytes) {

        for (int i = 0; i < bytes.length - 1; i++) {
            // 48 -> 0; 57 -> 9
            if (bytes[i] == 0x4F && bytes[i + 1] == 0x4B) {
                return true;
            }
        }
        return false;
    }

    public static String byteToHex(byte b) {

        String hex = Integer.toHexString(b & 0xFF);

        if (hex.length() < 2) {
            return "0" + hex;
        }
        return hex;
    }
}