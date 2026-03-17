package com.hzm.yuchao.simple.enc;

import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Formatter;

public class DeviceUtils {


    /**
     * MAC Address: 6E:CC:B4:C0:AF:5C, name: llw0
     * MAC Address: 6E:CC:B4:C0:AF:5C, name: awdl0
     * MAC Address: 2C:F0:EE:1A:73:22, name: en0
     * 6E:CC:B4:C0:AF:5C6E:CC:B4:C0:AF:5C2C:F0:EE:1A:73:22
     * MAC Address: 6E:CC:B4:C0:AF:5C, name: llw0
     * MAC Address: 6E:CC:B4:C0:AF:5C, name: awdl0
     * MAC Address: 2C:F0:EE:1A:73:22, name: en0
     * 43a895a03d357f7c4ea4f581673c1e9f
     * @param args
     */
    public static void main(String[] args) {
        System.out.println(getMacAddress());
        System.out.println(getInfo());
    }

    public static String getInfo() {
        return HashUtil.md5(getMacAddress());
    }

    private static String getMacAddress() {
        StringBuilder sb = new StringBuilder();
        try {
            // 获取所有网络接口
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                // 确保网络接口已经启用并且不是虚拟接口
                if (networkInterface.isUp() && !networkInterface.isLoopback()) {
                    byte[] mac = networkInterface.getHardwareAddress();
                    if (mac != null) {
                        Formatter formatter = new Formatter();
                        for (byte b : mac) {
                            formatter.format("%02X:", b);
                        }
                        String macAddress = formatter.toString();
                        // 移除最后一个冒号
                        macAddress = macAddress.substring(0, macAddress.length() - 1);
                        sb.append(macAddress);
//                        System.out.println("MAC Address: " + macAddress + ", name: " + networkInterface.getDisplayName());
                        formatter.close();
                    } else {
//                        System.out.println("MAC Address is null for: " + networkInterface.getDisplayName());
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("获取设备信息失败");
        }

        return sb.toString();
    }
}
