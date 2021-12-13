package com.paipeng.nfc.reader.pboc;


import com.paipeng.nfc.SPEC;
import com.paipeng.nfc.Util;
import com.paipeng.nfc.bean.Application;
import com.paipeng.nfc.bean.Card;
import com.paipeng.nfc.tech.Iso7816;

import java.io.IOException;
import java.util.ArrayList;

final class TUnion extends StandardPboc {

    @Override
    protected SPEC.APP getApplicationId() {
        return SPEC.APP.TUNIONEP;
    }

    protected boolean resetTag(Iso7816.StdTag tag) throws IOException {
        if (!tag.selectByID(DFI_MF).isOkey())
            tag.selectByName(DFN_PSE).isOkey();

        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected HINT readCard(Iso7816.StdTag tag, Card card) throws IOException {

        /*--------------------------------------------------------------*/
        // select Main Application
        /*--------------------------------------------------------------*/
        if (!selectMainApplication(tag))
            return HINT.GONEXT;

        Iso7816.Response INFO, BALANCE, OVER, OVER_LIMIT;

        /*--------------------------------------------------------------*/
        // read card info file, binary (21)
        /*--------------------------------------------------------------*/
        INFO = tag.readBinary(SFI_EXTRA);

        /*--------------------------------------------------------------*/
        // read balance
        /*--------------------------------------------------------------*/
        BALANCE = tag.getBalance(0x03, true);
        OVER = tag.getBalance(0x02, true);
        OVER_LIMIT = tag.getBalance(0x01, true);

        /*--------------------------------------------------------------*/
        // read log file, record (24)
        /*--------------------------------------------------------------*/
        ArrayList<byte[]> LOG = readLog24(tag, SFI_LOG);

        /*--------------------------------------------------------------*/
        // build result
        /*--------------------------------------------------------------*/
        final Application app = createApplication();

        parseBalance(app, BALANCE, OVER, OVER_LIMIT);

        parseInfo21(app, INFO, 4, true);

        parseLog24(app, LOG);

        configApplication(app);

        card.addApplication(app);

        return HINT.STOP;
    }

    @Override
    protected byte[] getMainApplicationId() {
        return new byte[] { (byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x06, (byte) 0x32,
                (byte) 0x01, (byte) 0x01, (byte) 0x05 };
    }

    @Override
    protected void parseInfo21(Application app, Iso7816.Response data, int dec, boolean bigEndian) {
        if (!data.isOkey() || data.size() < 30) {
            return;
        }

        final byte[] d = data.getBytes();
        String pan = Util.toHexString(d, 10, 10);
        app.setProperty(SPEC.PROP.SERIAL, pan.substring(1));

        if (d[9] != 0)
            app.setProperty(SPEC.PROP.VERSION, String.valueOf(d[9]));

        app.setProperty(SPEC.PROP.DATE, String.format("%02X%02X.%02X.%02X - %02X%02X.%02X.%02X",
                d[20], d[21], d[22], d[23], d[24], d[25], d[26], d[27]));
    }

    @Override
    protected void parseBalance(Application app, Iso7816.Response... data) {

        float balance = parseBalance(data[0]);
        if (balance < 0.01f) {
            float over = parseBalance(data[1]);
            if (over > 0.01f) {
                balance -= over;
                app.setProperty(SPEC.PROP.OLIMIT, parseBalance(data[2]));
            }
        }

        app.setProperty(SPEC.PROP.BALANCE, balance);
    }
}
