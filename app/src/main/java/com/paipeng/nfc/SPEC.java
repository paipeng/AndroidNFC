package com.paipeng.nfc;

public final class SPEC {
    public enum PAGE {
        DEFAULT, INFO, ABOUT,
    }

    public enum EVENT {
        IDLE, ERROR, READING, FINISHED,
    }

    public enum PROP {
        ID(R.string.spec_prop_id),
        SERIAL(R.string.spec_prop_serial),
        PARAM(R.string.spec_prop_param),
        VERSION(R.string.spec_prop_version),
        DATE(R.string.spec_prop_date),
        COUNT(R.string.spec_prop_count),
        CURRENCY(R.string.spec_prop_currency),
        TLIMIT(R.string.spec_prop_tlimit),
        DLIMIT(R.string.spec_prop_dlimit),
        ECASH(R.string.spec_prop_ecash),
        BALANCE(R.string.spec_prop_balance),
        OLIMIT(R.string.spec_prop_olimit),
        TRANSLOG(R.string.spec_prop_translog),
        ACCESS(R.string.spec_prop_access),
        EXCEPTION(R.string.spec_prop_exception);

        public String toString() {
            return ThisApplication.getStringResource(resId);
        }

        private final int resId;

        private PROP(int resId) {
            this.resId = resId;
        }
    }

    public enum APP {
        UNKNOWN(R.string.spec_app_unknown),
        UNKNOWNCITY(R.string.spec_zip_unknown),
        SHENZHENTONG(R.string.spec_app_shenzhentong),
        QUICKPASS(R.string.spec_app_quickpass),
        OCTOPUS(R.string.spec_app_octopus_hk),
        BEIJINGMUNICIPAL(R.string.spec_app_beijing),
        WUHANTONG(R.string.spec_app_wuhantong),
        CHANGANTONG(R.string.spec_app_changantong),
        SHANGHAIGJ(R.string.spec_app_shanghai),
        DEBIT(R.string.spec_app_debit),
        CREDIT(R.string.spec_app_credit),
        QCREDIT(R.string.spec_app_qcredit),
        TUNIONEC(R.string.spec_app_tunion_ec),
        TUNIONEP(R.string.spec_app_tunion_ep),
        CITYUNION(R.string.spec_app_cityunion);

        public String toString() {
            return ThisApplication.getStringResource(resId);
        }

        private final int resId;

        private APP(int resId) {
            this.resId = resId;
        }
    }

    public enum CUR {
        UNKNOWN(R.string.spec_cur_unknown),
        USD(R.string.spec_cur_usd),
        CNY(R.string.spec_cur_cny),
        HKD(R.string.spec_cur_hkd);

        public String toString() {
            return ThisApplication.getStringResource(resId);
        }

        private final int resId;

        private CUR(int resId) {
            this.resId = resId;
        }
    }

    public static final String TAG_BLK = "div";
    public static final String TAG_TIP = "t_tip";
    public static final String TAG_ACT = "t_action";
    public static final String TAG_EM = "t_em";
    public static final String TAG_H1 = "t_head1";
    public static final String TAG_H2 = "t_head2";
    public static final String TAG_H3 = "t_head3";
    public static final String TAG_SP = "t_splitter";
    public static final String TAG_TEXT = "t_text";
    public static final String TAG_LAB = "t_label";
    public static final String TAG_PARAG = "t_parag";

    public static String getCityUnionCardNameByZipcode(String zip) {
        byte[] tree = zip2CityName;
        if (tree == null) {
            tree = ThisApplication.loadRawResource(R.raw.zip);
            zip2CityName = tree;
        }

        try {

            int pos = 0, i = 0;
            do {
                int k = zip.charAt(i++);
                if (k < '0' || k > '9')
                    pos = 0;
                else
                    pos = getInt24(tree, pos + 3 * (k - '0'));

            } while (pos != 0 && i < zip.length());

            if (pos != 0) {
                int len = tree[pos + 30] & 0xFF;
                if (len > 0) {
                    String name = new String(tree, pos + 31, len, "UTF-8");
                    return String.format(APP.CITYUNION.toString(), name);
                }
            }
        } catch (Exception e) {
        }

        return String.format(APP.CITYUNION.toString(), APP.UNKNOWNCITY.toString());
    }

    private static int getInt24(byte[] d, int i) {
        return (d[i] & 0xFF) << 16 | (d[i + 1] & 0xFF) << 8 | (d[i + 2] & 0xFF);
    }

    private static byte[] zip2CityName;
}
