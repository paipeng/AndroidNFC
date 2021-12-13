package com.paipeng.nfc.reader;


import android.nfc.tech.NfcF;

import com.paipeng.nfc.SPEC;
import com.paipeng.nfc.Util;
import com.paipeng.nfc.bean.Application;
import com.paipeng.nfc.bean.Card;
import com.paipeng.nfc.tech.FeliCa;

import java.io.IOException;

final class FelicaReader {

    static void readCard(NfcF tech, Card card) throws IOException {

        final FeliCa.Tag tag = new FeliCa.Tag(tech);

        tag.connect();

		/*
		 *
		FeliCa.SystemCode systems[] = tag.getSystemCodeList();
		if (systems.length == 0) {
			systems = new FeliCa.SystemCode[] { new FeliCa.SystemCode(
					tag.getSystemCodeByte()) };
		}
		for (final FeliCa.SystemCode sys : systems)
			card.addApplication(readApplication(tag, sys.toInt()));
		*/

        // better old card compatibility
        card.addApplication(readApplication(tag, SYS_OCTOPUS));

        try {
            card.addApplication(readApplication(tag, SYS_SZT));
        } catch (IOException e) {
            // for early version of OCTOPUS which will throw shit
        }

        tag.close();
    }

    private static final int SYS_SZT = 0x8005;
    private static final int SYS_OCTOPUS = 0x8008;

    private static final int SRV_SZT = 0x0118;
    private static final int SRV_OCTOPUS = 0x0117;

    private static Application readApplication(FeliCa.Tag tag, int system)
            throws IOException {

        final FeliCa.ServiceCode scode;
        final Application app;
        if (system == SYS_OCTOPUS) {
            app = new Application();
            app.setProperty(SPEC.PROP.ID, SPEC.APP.OCTOPUS);
            app.setProperty(SPEC.PROP.CURRENCY, SPEC.CUR.HKD);
            scode = new FeliCa.ServiceCode(SRV_OCTOPUS);
        } else if (system == SYS_SZT) {
            app = new Application();
            app.setProperty(SPEC.PROP.ID, SPEC.APP.SHENZHENTONG);
            app.setProperty(SPEC.PROP.CURRENCY, SPEC.CUR.CNY);
            scode = new FeliCa.ServiceCode(SRV_SZT);
        } else {
            return null;
        }

        app.setProperty(SPEC.PROP.SERIAL, tag.getIDm().toString());
        app.setProperty(SPEC.PROP.PARAM, tag.getPMm().toString());

        tag.polling(system);

        final float[] data = new float[] { 0, 0, 0 };

        int p = 0;
        for (byte i = 0; p < data.length; ++i) {
            final FeliCa.ReadResponse r = tag.readWithoutEncryption(scode, i);
            if (!r.isOkey())
                break;

            data[p++] = (Util.toInt(r.getBlockData(), 0, 4) - 350) / 10.0f;
        }

        if (p != 0)
            app.setProperty(SPEC.PROP.BALANCE, parseBalance(data));
        else
            app.setProperty(SPEC.PROP.BALANCE, Float.NaN);

        return app;
    }

    private static float parseBalance(float[] value) {
        float balance = 0f;

        for (float v : value)
            balance += v;

        return balance;
    }
}
