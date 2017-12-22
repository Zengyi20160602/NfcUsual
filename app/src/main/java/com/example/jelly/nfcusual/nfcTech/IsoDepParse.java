package com.example.jelly.nfcusual.nfcTech;

import android.nfc.TagLostException;
import android.nfc.tech.IsoDep;

import com.example.jelly.nfcusual.common.NumeralParse;

import java.io.IOException;

import darks.log.Logger;

/**
 * Created by jelly on 2017/12/1.
 * 解析IsoDep协议的NFC卡
 */

public class IsoDepParse implements Runnable {
    private static Logger log = Logger.getLogger(IsoDepParse.class);

    public interface OnMessageReceived {
        void onMessage(byte[] message);
        void onError(Exception exception);
    }

    private IsoDep isoDep;
    private OnMessageReceived onMessageReceived;
    private byte[] readrecord_apdu;
    private byte[] not_apdu;

    public IsoDepParse(IsoDep isoDep, OnMessageReceived onMessageReceived) {

        this.readrecord_apdu = new byte[] {
                (byte) 0xB0, //CLA indicates the type of command, e.g. interindustry or proprietary
                (byte) 0xCA, //INS INS indicates the specific command, e.g. "write data"
                (byte) 0x00, //P1 P1-P2：参数字节。这些字节对 命令APDU提供进一步说明。
                (byte) 0x00, //P2 P1-P2：参数字节。这些字节对 命令APDU提供进一步说明。
                (byte) 0x00 //LE maximal number of bytes expected in result
        };
        this.not_apdu = new byte[] {
                (byte) 0x00,
        };

        this.isoDep = isoDep;
        this.onMessageReceived = onMessageReceived;

    }
    @Override
    public void run() {
        boolean errorfound = false;
        byte[] response;
        try {
            isoDep.close();
            isoDep.connect();
        }catch (IOException e){
            errorfound = true;
            onMessageReceived.onError(e);
        }
        if (!errorfound) {
            if (isoDep.isConnected()) {

                try {
                    isoDep.setTimeout(1200);
                    log.info("transceive before");
                    response = isoDep.transceive(not_apdu);
                    log.info("transceive after");

                    int status = ((0xff & response[response.length - 2]) << 8)
                            | (0xff & response[response.length - 1]);
                    if (status != 0x9000) {
                        log.error("retrieve data ,read failure");
                    }else {
                        log.info("retrieve data, read succ");
                        log.info("retrieve data, result=" + NumeralParse.toHex(response));
                    }
                    onMessageReceived.onMessage(response);
                }catch (TagLostException e) {
                    log.info("catch tag lost exception, tag isConnected=" + isoDep.isConnected());
                    onMessageReceived.onError(e);
                }
                catch (IOException e) {
                    log.info("catch IOException, isoDep isConnected=" + isoDep.isConnected());
                    onMessageReceived.onError(e);
                }
            }else {
                log.error("isoDep not connect");
            }
        }
    }
}
