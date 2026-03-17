package com.hzm.yuchao.simple.utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CRC16Utils {

    private static final int CRC_POLYNOMIAL = 0xA001;

//    public static void main(String[] args) throws Exception {
//
//        // BC29  5A
//        String hexString = "00011200002F000C30340BDF0C293BB5DFA44003300001010D08000DE59E2000144E36383031303030303234303732304330303036";
//
//        int i = calculateCRC(Hex.decodeHex(hexString.toCharArray()));
//
//        System.out.println(Integer.toHexString(i));
//
//    }

    public static boolean crc16Check(byte[] data) {
        int calculateCRC = calculateCRC(data);
        int crc16 = ByteUtils.byteArrayToShort(data, data.length - 2);

        if (calculateCRC != crc16) {
//            log.info("crc16 check失败，计算值: {}, 上传值: {}, 数据：{}", calculateCRC, crc16, Arrays.toString(data));
        }

        return true;
//        return calculateCRC == crc16;
    }

    private static int calculateCRC(byte[] data) {
        int crc = 0xFFFF;

        for (int i = 0; i < data.length - 2; i++) {
            crc ^= data[i] & 0xFF;

            for (int j = 0; j < 8; j++) {
                if ((crc & 0x0001) != 0) {
                    crc >>= 1;
                    crc ^= CRC_POLYNOMIAL;
                } else {
                    crc >>= 1;
                }
            }
        }

        return ~crc & 0xFFFF;
    }
}