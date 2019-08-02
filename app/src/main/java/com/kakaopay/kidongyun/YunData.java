package com.kakaopay.kidongyun;

import android.graphics.Bitmap;
import android.util.Log;

// Class : YunData.
// Description : Kakao Rest API에서 이미지 검색 결과를 받아올 때 JSON 객체를 저장 및 표현하기 위한 자료구조 객체

public class YunData {
    private String docUrl;
    private String datetime;
    private String displaySiteName;
    private String imageUrl;
    private String collection;
    private Bitmap thumbnailUrl;
    private int width;
    private int height;

    public YunData() { }

    public String getDocUrl() { return docUrl; }
    public YunData setDocUrl(String docUrl) { this.docUrl = docUrl; return this; }

    public String getDatetime() { return datetime; }
    public YunData setDatetime(String datetime) { this.datetime = datetime; return this; }

    public String getDisplaySiteName() { return displaySiteName; }
    public YunData setDisplaySiteName(String displaySiteName) { this.displaySiteName = displaySiteName; return this; }

    public String getImageUrl() { return imageUrl; }
    public YunData setImageUrl(String imageUrl) { this.imageUrl = imageUrl; return this; }

    public String getCollection() { return collection; }
    public YunData setCollection(String collection) { this.collection = collection; return this; }

    public Bitmap getThumbnailUrl() { return thumbnailUrl; }
    public YunData setThumbnailUrl(Bitmap thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; return this; }

    public int getWidth() { return width; }
    public YunData setWidth(int width) { this.width = width; return this; }

    public int getHeight() { return height; }
    public YunData setHeight(int height) { this.height = height; return this; }

    public void log() {
        Log.d("KakaoIntern", "docUrl : " + getDocUrl() + ", datetime : " + getDatetime() + ", displaySitename : " + getDisplaySiteName() + ", imageUrl : " + getImageUrl() + ", collection : " + getCollection() + ", thumbnail : " + getThumbnailUrl() + ", width :" + getWidth() + ", height : " +  getHeight());
    }

    public String getYear() {
        return getDatetime().substring(0, 4);
    }
}
