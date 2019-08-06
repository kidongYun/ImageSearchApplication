package com.kakaopay.kidongyun;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class YunImageRequest extends Thread {

    // Class : YunImageRequest.
    // Description : Kakao Image Search API를 활용하여 이미지 검색 데이터를 가져오는 클래스.

    public static boolean IS_LOCK = false;  // YunImageRequest 인스턴스 실행 시에 중복 실행을 방지하고 상호배제를 위한 boolean 변수
    public static int REQUEST_PAGE = 1;

    public final int NO_RESULT = -100;
    public final int RESULT = 100;

    public Handler handler;

    private String query;

    private final String APP_KEY = "c17ccc77e1404784cf078e75f5951eac";  // Kakao Rest API를 사용하기 위한 키
    private final String KAKAO_IMAGE_API_HOST = "https://dapi.kakao.com/v2/search/image";   // Image Search API를 사용하기 위한 호스트 주소

    public YunImageRequest(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void run() {
        super.run();
        IS_LOCK = true;
        request(query);
    }

    private void request(String query) {
        //  이미지를 요청해서 가져오는 전체적인 과정이 담긴 함수

        try {
            Log.d("YUN", "REQUEST_PAGE : " + REQUEST_PAGE);

            String urlStr = KAKAO_IMAGE_API_HOST + "?query=" + query + "?page=" + REQUEST_PAGE;
            JSONArray jsonArray = parseToJSON(requestStream(urlStr));

            if(jsonArray.size() == 0) {
                // 받아온 데이터가 없는 경우

                Message msg = new Message();
                msg.what = NO_RESULT;

                handler.sendMessage(msg);
                IS_LOCK = false;

            } else {
                // 받아온 데이터가 있는 경우

                for(int i=0; i<jsonArray.size(); i++) {
                    Message msg = new Message();
                    msg.what = RESULT;
                    msg.obj = parseToYunData((JSONObject)jsonArray.get(i));

                    handler.sendMessage(msg);

                    if(i >= jsonArray.size() - 1) {
                        IS_LOCK = false;
                    }
                }
            }
        } catch (IOException ie) {
            ie.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private InputStream requestStream(String urlStr) throws IOException {
        // url 기반 http 통신으로 데이터를 받아오는 함수.

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestProperty("Authorization", "KakaoAK " + APP_KEY);
        conn.connect();

        return conn.getInputStream();
    }

    private Bitmap requestBitmap(String urlStr) throws IOException {
        // url 기반 http 통신으로 Image 데이터를 bitmap 형식으로 가져오는 함수.

        Bitmap bitmap;

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();

        InputStream stream = conn.getInputStream();
        bitmap = BitmapFactory.decodeStream(stream);

        return bitmap;
    }

    private JSONArray parseToJSON(InputStream stream) throws Exception {
        // 받아온 데이터를 JSON TYPE으로 변환.

        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject)jsonParser.parse(new InputStreamReader(stream, "UTF-8"));
        JSONArray jsonArray = (JSONArray)jsonObject.get("documents");

        return jsonArray;
    }

    private YunData parseToYunData(JSONObject jsonItem) throws IOException {
        // JSON TYPE을 이 어플리케이션에서 사용하기 용이하도록 YunData TYPE으로 변환.

        YunData yunData = new YunData();

        yunData   .setDocUrl(jsonItem.get("doc_url").toString())
                .setDatetime(jsonItem.get("datetime").toString())
                .setDisplaySiteName(jsonItem.get("display_sitename").toString())
                .setImageUrl(jsonItem.get("image_url").toString())
                .setCollection(jsonItem.get("collection").toString())
                .setThumbnailUrl(requestBitmap(jsonItem.get("thumbnail_url").toString()))
                .setWidth(Integer.parseInt(jsonItem.get("width").toString()))
                .setHeight(Integer.parseInt(jsonItem.get("height").toString()));

        return yunData;
    }

    // Access Methods
    public YunImageRequest setQuery(String query) { this.query = query; return this; }
    public YunImageRequest nextPage() { REQUEST_PAGE++; return this; }
    public YunImageRequest intializePage() { REQUEST_PAGE = 1; return this; }

}
