package com.paipeng.nfc.reader.pboc;


import com.paipeng.nfc.SPEC;
import com.paipeng.nfc.Util;
import com.paipeng.nfc.bean.Application;
import com.paipeng.nfc.bean.Card;
import com.paipeng.nfc.tech.Iso7816;

import java.io.IOException;
import java.util.ArrayList;

final class WuhanTong extends StandardPboc {

    @Override
    protected SPEC.APP getApplicationId() {
        return SPEC.APP.WUHANTONG;
    }

    @Override
    protected byte[] getMainApplicationId() {
        return new byte[] { (byte) 0x41, (byte) 0x50, (byte) 0x31, (byte) 0x2E,
                (byte) 0x57, (byte) 0x48, (byte) 0x43, (byte) 0x54,
                (byte) 0x43, };
    }

    @SuppressWarnings("unchecked")
    @Override
    protected HINT readCard(Iso7816.StdTag tag, Card card) throws IOException {

        Iso7816.Response INFO, SERL, BALANCE;

        /*--------------------------------------------------------------*/
        // read card info file, binary (5, 10)
        /*--------------------------------------------------------------*/
        if (!(SERL = tag.readBinary(SFI_SERL)).isOkey())
            return HINT.GONEXT;

        if (!(INFO = tag.readBinary(SFI_INFO)).isOkey())
            return HINT.GONEXT;

        BALANCE = tag.getBalance(0, true);

        /*--------------------------------------------------------------*/
        // select Main Application
        /*--------------------------------------------------------------*/
        if (!tag.selectByName(getMainApplicationId()).isOkey())
            return HINT.RESETANDGONEXT;

        /*--------------------------------------------------------------*/
        // read balance
        /*--------------------------------------------------------------*/
        if (!BALANCE.isOkey())
            BALANCE = tag.getBalance(0, true);

        /*--------------------------------------------------------------*/
        // read log file, record (24)
        /*--------------------------------------------------------------*/
        ArrayList<byte[]> LOG = readLog24(tag, SFI_LOG);

        /*--------------------------------------------------------------*/
        // build result
        /*--------------------------------------------------------------*/
        final Application app = createApplication();

        parseBalance(app, BALANCE);

        parseInfo5(app, SERL, INFO);

        parseLog24(app, LOG);

        configApplication(app);

        card.addApplication(app);

        return HINT.STOP;
    }

    private final static int SFI_INFO = 5;
    private final static int SFI_SERL = 10;

    private void parseInfo5(Application app, Iso7816.Response sn,
                            Iso7816.Response info) {
        if (sn.size() < 27 || info.size() < 27) {
            return;
        }

        final byte[] d = info.getBytes();
        app.setProperty(SPEC.PROP.SERIAL, Util.toHexString(sn.getBytes(), 0, 5));
        app.setProperty(SPEC.PROP.VERSION, String.format("%02d", d[24]));
        app.setProperty(SPEC.PROP.DATE, String.format(
                "%02X%02X.%02X.%02X - %02X%02X.%02X.%02X", d[20], d[21], d[22],
                d[23], d[16], d[17], d[18], d[19]));
    }
}
