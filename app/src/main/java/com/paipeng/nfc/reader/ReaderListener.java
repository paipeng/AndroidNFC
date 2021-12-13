package com.paipeng.nfc.reader;

import com.paipeng.nfc.SPEC;

public interface ReaderListener {
    void onReadEvent(SPEC.EVENT event, Object... obj);
}
