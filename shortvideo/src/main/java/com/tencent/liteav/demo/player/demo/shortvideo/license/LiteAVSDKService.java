package com.tencent.liteav.demo.player.demo.shortvideo.license;

import android.content.Context;
import com.tencent.rtmp.TXLiveBase;
import com.tencent.rtmp.TXLiveBaseListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class LiteAVSDKService {

    private static final String LICENCE_URL =
            "https://license.vod2.myqcloud.com/license/v2/1258247189_1/v_cube.license";
    private static final String LICENCE_KEY = "bf190ae874599b2299ac1220e500601f";

    private static final String XMAGIC_AUTH_KEY = "f5109b0ec0ba0027f809cb947d204c65";
    private static final String XMAGIC_AUTH_LICENCE_URL = "https://license.vod2.myqcloud.com/license/v2/1252463788_1/v_cube.license";
    /**
     * 初始化腾讯云相关sdk。
     * SDK 初始化过程中可能会读取手机型号等敏感信息，需要在用户同意隐私政策后，才能获取。
     */
    public static void init(Context appContext) {
        TXLiveBase.getInstance().setLicence(appContext, LICENCE_URL, LICENCE_KEY);

        TXLiveBase.setListener(new TXLiveBaseListener() {
            @Override
            public void onUpdateNetworkTime(int errCode, String errMsg) {
                if (errCode != 0) {
                    TXLiveBase.updateNetworkTime();
                }
            }
        });
        TXLiveBase.updateNetworkTime();
    }
}
