package com.paipeng.nfc.utils;

import android.app.Activity;
import android.app.PendingIntent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

public class M1CardUtil {
    private static final String TAG = M1CardUtil.class.getSimpleName();
    private static PendingIntent pendingIntent;

    public static PendingIntent getPendingIntent() {
        return pendingIntent;
    }

    public static void setPendingIntent(PendingIntent pendingIntent) {
        M1CardUtil.pendingIntent = pendingIntent;
    }

    /**
     * 判断是否支持NFC
     *
     * @return
     */
    public static NfcAdapter isNfcAble(Activity mContext) {
        NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(mContext);
        if (mNfcAdapter == null) {
            Toast.makeText(mContext, "设备不支持NFC！", Toast.LENGTH_LONG).show();
        } else if (!mNfcAdapter.isEnabled()) {
            Toast.makeText(mContext, "请在系统设置中先启用NFC功能！", Toast.LENGTH_LONG).show();
        }

        return mNfcAdapter;
    }

    /**
     * 监测是否支持cardType类型卡
     *
     * @param tag
     * @param activity
     * @param cardType
     * @return
     */
    public static boolean hasCardType(Tag tag, Activity activity, String cardType) {
        Log.d(TAG, "hasCardType: " + tag.toString());
        if (tag == null) {
            Toast.makeText(activity, "请贴卡", Toast.LENGTH_LONG).show();
            return false;
        }

        String[] techList = tag.getTechList();

        boolean hasCardType = false;
        for (String tech : techList) {
            Log.e("TagTech", tech);
            if (tech.contains(cardType)) {
                hasCardType = true;
                break;
            }
        }

        if (!hasCardType) {
            Toast.makeText(activity, "不支持" + cardType + "卡", Toast.LENGTH_LONG).show();
        }

        return hasCardType;
    }

    /**
     * CPU卡信息读取
     *
     * @param tag
     * @return
     * @throws IOException
     */
    public static String readIsoCard(Tag tag) throws IOException {
        Log.d(TAG, "readIsoCard");
        IsoDep isoDep = IsoDep.get(tag);
        if (!isoDep.isConnected()) {
            isoDep.connect();
        }

        String result = StringUtil.bytesToHexString(isoDep.transceive(StringUtil.hex2Bytes("00A40000023F00")));
        Log.e("readIsoCard", result);
        result = StringUtil.bytesToHexString(isoDep.transceive(StringUtil.hex2Bytes("00A40000020005")));
        Log.e("readIsoCard", result);
        result = StringUtil.bytesToHexString(isoDep.transceive(StringUtil.hex2Bytes("00B0000016")));
        Log.e("readIsoCard", result);
        isoDep.close();
        return result;
    }

    /**
     * M1读取卡片信息
     *
     * @return
     */
    public static String[][] readCard(Tag tag) throws IOException {
        MifareClassic mifareClassic = MifareClassic.get(tag);
        try {
            mifareClassic.connect();
            String[][] metaInfo = new String[16][4];
            // 获取TAG中包含的扇区数
            int sectorCount = mifareClassic.getSectorCount();
            for (int j = 0; j < sectorCount; j++) {
                int bCount;//当前扇区的块数
                int bIndex;//当前扇区第一块
                if (m1Auth(mifareClassic, j)) {
                    bCount = mifareClassic.getBlockCountInSector(j);
                    bIndex = mifareClassic.sectorToBlock(j);
                    for (int i = 0; i < bCount; i++) {
                        byte[] data = mifareClassic.readBlock(bIndex);
                        String dataString = bytesToHexString(data);
                        metaInfo[j][i] = dataString;
                        Log.e("获取到信息", dataString);
                        bIndex++;
                    }
                } else {
                    Log.e("readCard", "密码校验失败");
                }
            }
            return metaInfo;
        } catch (IOException e) {
            throw new IOException(e);
        } finally {
            try {
                mifareClassic.close();
            } catch (IOException e) {
                throw new IOException(e);
            }
        }
    }

    /**
     * 改写数据
     *
     * @param block
     * @param blockbyte
     */
    public static boolean writeBlock(Tag tag, int block, byte[] blockbyte) throws IOException {
        MifareClassic mifareClassic = MifareClassic.get(tag);
        try {
            mifareClassic.connect();
            if (m1Auth(mifareClassic, block / 4)) {
                mifareClassic.writeBlock(block, blockbyte);
                Log.e("writeBlock", "写入成功");
            } else {
                Log.e("密码是", "没有找到密码");
                return false;
            }
        } catch (IOException e) {
            throw new IOException(e);
        } finally {
            try {
                mifareClassic.close();
            } catch (IOException e) {
                throw new IOException(e);
            }
        }
        return true;

    }

    /**
     * 密码校验
     *
     * @param mTag
     * @param position
     * @return
     * @throws IOException
     */
    public static boolean m1Auth(MifareClassic mTag, int position) throws IOException {
        if (mTag.authenticateSectorWithKeyA(position, MifareClassic.KEY_DEFAULT)) {
            return true;
        } else if (mTag.authenticateSectorWithKeyB(position, MifareClassic.KEY_DEFAULT)) {
            return true;
        }
        return false;
    }

    private static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        char[] buffer = new char[2];
        for (int i = 0; i < src.length; i++) {
            buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
            System.out.println(buffer);
            stringBuilder.append(buffer);
        }
        return stringBuilder.toString();
    }
}
