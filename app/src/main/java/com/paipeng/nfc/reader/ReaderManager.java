package com.paipeng.nfc.reader;


import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.NfcF;
import android.os.AsyncTask;

import com.paipeng.nfc.SPEC;
import com.paipeng.nfc.Util;
import com.paipeng.nfc.bean.Card;
import com.paipeng.nfc.reader.pboc.StandardPboc;

public final class ReaderManager extends AsyncTask<Tag, SPEC.EVENT, Card> {

    public static void readCard(Tag tag, ReaderListener listener) {
        new ReaderManager(listener).execute(tag);
    }

    private ReaderListener realListener;

    private ReaderManager(ReaderListener listener) {
        realListener = listener;
    }

    @Override
    protected Card doInBackground(Tag... detectedTag) {
        return readCard(detectedTag[0]);
    }

    @Override
    protected void onProgressUpdate(SPEC.EVENT... events) {
        if (realListener != null)
            realListener.onReadEvent(events[0]);
    }

    @Override
    protected void onPostExecute(Card card) {
        if (realListener != null)
            realListener.onReadEvent(SPEC.EVENT.FINISHED, card);
    }

    private Card readCard(Tag tag) {

        final Card card = new Card();

        try {

            publishProgress(SPEC.EVENT.READING);

            card.setProperty(SPEC.PROP.ID, Util.toHexString(tag.getId()));

            final IsoDep isodep = IsoDep.get(tag);
            if (isodep != null)
                StandardPboc.readCard(isodep, card);

            final NfcF nfcf = NfcF.get(tag);
            if (nfcf != null)
                FelicaReader.readCard(nfcf, card);

            publishProgress(SPEC.EVENT.IDLE);

        } catch (Exception e) {
            card.setProperty(SPEC.PROP.EXCEPTION, e);
            publishProgress(SPEC.EVENT.ERROR);
        }

        return card;
    }
}
