package com.example.jelly.nfcusual.common;

import java.util.ArrayList;

import darks.log.Logger;

/**
 * Created by jelly on 2017/11/30.
 * 进制转换
 */

public class NumeralParse {

    private static Logger log = Logger.getLogger(NumeralParse.class);

    private NumeralParse() {}

    public static String toReversedHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = bytes.length - 1; i >= 0; --i) {
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
            if (i > 0) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    public static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; ++i) {
            if (i > 0) {
                sb.append(" ");
            }
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
        }
        return sb.toString();
    }

    public static long toDec(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = 0; i < bytes.length; ++i) {
            long value = bytes[i] & 0xffl;
            result += value * factor;
            factor *= 256l;
        }
        return result;
    }

    public static long toReversedDec(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = bytes.length - 1; i >= 0; --i) {
            long value = bytes[i] & 0xffl;
            result += value * factor;
            factor *= 256l;
        }
        return result;
    }

    public static ArrayList<byte[]> parseRecords(byte[] Records) {
        int max = (int) Math.ceil((double) Records.length / (double) 16);
        log.info("分割记录有" + max + "条");
        ArrayList<byte[]> res = new ArrayList<>();

        for (int i = 0; i < max; i++) {
            byte[] aRecord;
            if (i == max-1) {
                aRecord = new byte[Records.length - i*16];
            }else {
                aRecord = new byte[16];
            }

            for (int j = 16 * i, k = 0; j < 16 * (i + 1); j++, k++) {
                if (j <= Records.length - 1) {
                    aRecord[k] = Records[j];
                }else {
                    log.info("j=" + j);
                    break;
                }
            }
            res.add(aRecord);
        }
        for (byte[] bs : res) {
            log.info("分割记录有byte[]" + bs); // 有数据。解析正确。
        }
        return res;
    }
}
