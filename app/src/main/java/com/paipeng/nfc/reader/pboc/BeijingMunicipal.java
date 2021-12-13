package com.paipeng.nfc.reader.pboc;


import com.paipeng.nfc.SPEC;
import com.paipeng.nfc.Util;
import com.paipeng.nfc.bean.Application;
import com.paipeng.nfc.bean.Card;
import com.paipeng.nfc.tech.Iso7816;

import java.io.IOException;
import java.util.ArrayList;

final class BeijingMunicipal extends StandardPboc {

    @Override
    protected SPEC.APP getApplicationId() {
        return SPEC.APP.BEIJINGMUNICIPAL;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected HINT readCard(Iso7816.StdTag tag, Card card) throws IOException {

        Iso7816.Response INFO, CNT, BALANCE;

        /*--------------------------------------------------------------*/
        // read card info file, binary (4)
        /*--------------------------------------------------------------*/
        INFO = tag.readBinary(SFI_EXTRA_LOG);
        if (!INFO.isOkey())
            return HINT.GONEXT;

        /*--------------------------------------------------------------*/
        // read card operation file, binary (5)
        /*--------------------------------------------------------------*/
        CNT = tag.readBinary(SFI_EXTRA_CNT);

        /*--------------------------------------------------------------*/
        // select Main Application
        /*--------------------------------------------------------------*/
        if (!tag.selectByID(DFI_EP).isOkey())
            return HINT.RESETANDGONEXT;

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

        parseInfo4(app, INFO, CNT);

        parseLog24(app, LOG);

        configApplication(app);

        card.addApplication(app);

        return HINT.STOP;
    }

    private final static int SFI_EXTRA_LOG = 4;
    private final static int SFI_EXTRA_CNT = 5;

    private void parseInfo4(Application app, Iso7816.Response info,
                            Iso7816.Response cnt) {

        if (!info.isOkey() || info.size() < 32) {
            return;
        }

        final byte[] d = info.getBytes();
        app.setProperty(SPEC.PROP.SERIAL, Util.toHexString(d, 0, 8));
        app.setProperty(SPEC.PROP.VERSION,
                String.format("%02X.%02X%02X", d[8], d[9], d[10]));
        app.setProperty(SPEC.PROP.DATE, String.format(
                "%02X%02X.%02X.%02X - %02X%02X.%02X.%02X", d[24], d[25], d[26],
                d[27], d[28], d[29], d[30], d[31]));

        if (cnt != null && cnt.isOkey() && cnt.size() > 4) {
            byte[] e = cnt.getBytes();
            final int n = Util.toInt(e, 1, 4);
            if (e[0] == 0)
                app.setProperty(SPEC.PROP.COUNT, String.format("%d", n));
            else
                app.setProperty(SPEC.PROP.COUNT, String.format("%d*", n));
        }
    }
}
