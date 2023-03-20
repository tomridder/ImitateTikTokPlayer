package com.tencent.liteav.demo.vodcommon.entity;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;



import com.tencent.liteav.demo.vodcommon.R;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by liyuejiao on 2018/7/3.
 * 获取点播信息
 */

public class SuperVodListLoader {

    private static final String                M3U8_SUFFIX = ".m3u8";
    private static final String                TAG       = "SuperVodListLoader";
    private              Context               mContext;
    private              Handler               mHandler;
    private              HandlerThread         mHandlerThread;
    private              boolean               mIsHttps  = true;
    private final        String                BASE_URL  = "http://playvideo.qcloud.com/getplayinfo/v4";
    private final        String                BASE_URLS = "https://playvideo.qcloud.com/getplayinfo/v4";
    private              OnVodInfoLoadListener mOnVodInfoLoadListener;
    private              OkHttpClient          mHttpClient;
    private              int                   mAppId    = 1500005830;
    private              Object mLock = new Object();


    public SuperVodListLoader(Context context) {
        mHandlerThread = new HandlerThread("SuperVodListLoader");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
        mContext = context;
        mHttpClient = new OkHttpClient();
        mHttpClient.newBuilder().connectTimeout(5, TimeUnit.SECONDS);
    }

    public void getVideoListInfo(final ArrayList<VideoModel> videoModels, final boolean isCacheModel,
                                 final OnVodListLoadListener listener) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                final int loadSize = videoModels.size();
                final AtomicInteger integer = new AtomicInteger(0);
                for (VideoModel model : videoModels) {
                    getVodByFileId(model, new OnVodInfoLoadListener() {
                        @Override
                        public void onSuccess(VideoModel videoModel) {
                            synchronized (mLock) {
                                integer.getAndAdd(1);
                                if (integer.get() == loadSize) {
                                    VideoListModel videoListModel = new VideoListModel();
                                    videoListModel.videoModelList = videoModels;
                                    videoListModel.isEnableDownload = isCacheModel;
                                    listener.onSuccess(videoListModel);
                                }
                            }
                        }

                        @Override
                        public void onFail(int errCode) {
                            listener.onFail(-1);
                        }
                    });
                }
            }
        });
    }

    public void getVodByFileId(final VideoModel model, final OnVodInfoLoadListener listener) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                String urlStr = makeUrlString(model.appid, model.fileid, model.pSign);
                Request request = new Request.Builder().url(urlStr).build();
                Call call = mHttpClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        //获取请求信息失败
                        listener.onFail(-1);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String content = response.body().string();
                        parseJson(model, content, listener);
                    }
                });
            }
        });
    }



    private void parseJson(VideoModel videoModel, String content, OnVodInfoLoadListener listener) {
        if (TextUtils.isEmpty(content)) {
            Log.e(TAG, "parseJson err, content is empty!");
            return;
        }

        try {
            JSONObject jsonObject = new JSONObject(content);
            int code = jsonObject.getInt("code");
            if (code != 0) {
                String message = jsonObject.getString("message");
                listener.onFail(-1);
                Log.e(TAG, message);
                return;
            }

            int version = jsonObject.getInt("version");

            if (version == 4) {
                videoModel.videoURL = jsonObject.optJSONObject("media").optJSONObject("streamingInfo").optJSONObject("plainOutput").optString("url");
                String title = jsonObject.optJSONObject("media").optJSONObject("basicInfo").optString("name");
                upDataTitle(videoModel, title);
                videoModel.placeholderImage = jsonObject.optJSONObject("media").optJSONObject("basicInfo").optString("coverUrl");
                videoModel.duration = jsonObject.optJSONObject("media").optJSONObject("basicInfo").optInt("duration");
            }
            videoModel.title = getTitleByFileId(videoModel);
            listener.onSuccess(videoModel);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据视频ID 获取视频标题
     *
     * @param model
     * @return
     */
    private String getTitleByFileId(VideoModel model) {
        String fileId = model.fileid;
        String title = "";
        switch (fileId) {
            case "387702299774251236":
                title = mContext.getString(R.string.tencent_cloud_audio_and_video_achievements_title);
                break;
            case "387702299774544650":
                title = mContext.getString(R.string.tencent_cloud_audio_and_video_steady_title);
                break;
            case "387702299774644824":
                title = mContext.getString(R.string.tencent_cloud_audio_and_video_real_title);
                break;
            case "387702299774211080":
                title = mContext.getString(R.string.tencent_cloud_audio_and_video_complete_title);
                break;
            case "387702299774545556":
                title = mContext.getString(R.string.tencent_cloud_business_introduction_title);
                break;
            case "387702299774574470":
                title = mContext.getString(R.string.what_are_numbers_title);
                break;
            case "387702299774253670":
                title = mContext.getString(R.string.simplify_complexity_and_build_big_from_small_title);
                break;
            case "387702299773851453":
                title = String.format(mContext.getString(R.string.super_player_cache_video_title),1);
                break;
            case "387702299774155981":
                title = String.format(mContext.getString(R.string.super_player_cache_video_title),2);
                break;
            case "387702299773830943":
                title = String.format(mContext.getString(R.string.super_player_cache_video_title),3);
                break;
            case "387702299773823860":
                title = String.format(mContext.getString(R.string.super_player_cache_video_title),4);
                break;
            case "387702299774156604":
                title = String.format(mContext.getString(R.string.super_player_cache_video_title),5);
                break;
            default:
                title = model.title;
                break;
        }
        return title;
    }

    private void upDataTitle(VideoModel videoModel, String newTitle) {
        if (TextUtils.isEmpty(videoModel.title)) {
            videoModel.title = newTitle;
        }
    }

    /**
     * 拼装协议请求url
     *
     * @return 协议请求url字符串
     */
    private String makeUrlString(int appId, String fileId, String pSign) {
        String urlStr;
        if (mIsHttps) {
            // 默认用https
            urlStr = String.format("%s/%d/%s", BASE_URLS, appId, fileId);
        } else {
            urlStr = String.format("%s/%d/%s", BASE_URL, appId, fileId);
        }
        String query = makeQueryString(null, pSign, null);
        if (query != null) {
            urlStr = urlStr + "?" + query;
        }
        return urlStr;
    }

    /**
     * 拼装协议请求url中的query字段
     *
     * @return query字段字符串
     */
    private String makeQueryString(String pcfg, String psign, String content) {
        StringBuilder str = new StringBuilder();
        if (!TextUtils.isEmpty(pcfg)) {
            str.append("pcfg=" + pcfg + "&");
        }

        if (!TextUtils.isEmpty(psign)) {
            str.append("psign=" + psign + "&");
        }

        if (!TextUtils.isEmpty(content)) {
            str.append("context=" + content + "&");
        }
        if (str.length() > 1) {
            str.deleteCharAt(str.length() - 1);
        }
        return str.toString();
    }

    public interface OnVodInfoLoadListener {
        void onSuccess(VideoModel videoModel);

        void onFail(int errCode);
    }


    public interface OnVodListLoadListener {
        void onSuccess(VideoListModel videoListModel);

        void onFail(int errCode);
    }
}
