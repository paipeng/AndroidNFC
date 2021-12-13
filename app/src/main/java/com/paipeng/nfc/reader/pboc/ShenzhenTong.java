package com.paipeng.nfc.reader.pboc;

import com.paipeng.nfc.SPEC;

final class ShenzhenTong extends StandardPboc {

    @Override
    protected SPEC.APP getApplicationId() {
        return SPEC.APP.SHENZHENTONG;
    }
}
