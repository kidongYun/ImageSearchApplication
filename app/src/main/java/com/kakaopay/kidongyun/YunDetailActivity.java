package com.kakaopay.kidongyun;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class YunDetailActivity extends AppCompatActivity implements View.OnClickListener {

    // Class : YunDetailActivity.
    //Description : 선택된 이미지의 상세내용을 표현하기 위한 Activity 클래스.

    //View 요소들
    ImageView image;
    ImageButton prev;
    ImageButton link;
    ImageButton download;

    YunData yunData;

    YunImageLoader yunImageLoader;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            image.setImageBitmap((Bitmap)msg.obj);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        getSupportActionBar().setTitle("KIDONG YUN");

        // View 초기화.
        image = findViewById(R.id.image);
        prev = findViewById(R.id.prev);
        link = findViewById(R.id.link);
        download = findViewById(R.id.download);

        // 버튼들 Listener에 연결.
        prev.setOnClickListener(this);
        link.setOnClickListener(this);
        download.setOnClickListener(this);

        Intent intent = new Intent(this.getIntent());

        yunData = new YunData();
        yunData.setDocUrl(intent.getStringExtra("docUrl")).setImageUrl(intent.getStringExtra("imageUrl"));

        yunImageLoader = new YunImageLoader(handler, yunData.getImageUrl());
        yunImageLoader.start();
    }


    @Override
    public void onClick(View view) {
        // 각 ImageButton 들을 눌렀을 때 이벤트 처리하는 함수.
        switch (view.getId()) {
            case R.id.prev :
                // 뒤로가기 버튼을 눌렀을 때 Detail Activity를 종료하고 이전 Activity로 돌아간다.
                super.onBackPressed();
                overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
                break;
            case R.id.link :
                // 링크 버튼을 눌렀을 때 브라우저를 통해 해당 웹페이지에 연결.
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(yunData.getDocUrl()));
                startActivity(browserIntent);
                break;
            case R.id.download :
                // 다운로드 버튼을 눌렀을 때 이미지 파일 로컬로 다운로드.
                break;
        }
    }
}


class YunImageLoader extends Thread {

    // Class : YunImageLoader
    // Description : Url기반 http 통신으로 Image를 가져오는 클래스.

    String urlStr;
    Handler handler;

    public YunImageLoader(Handler handler, String urlStr) {
        this.handler = handler;
        this.urlStr = urlStr;
    }

    @Override
    public void run() {
        super.run();

        try {
            Message msg = new Message();
            msg.obj = requestBitmap(urlStr);
            handler.sendMessage(msg);

        } catch (IOException ie) {
            ie.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Bitmap requestBitmap(String urlStr) throws IOException {
        Bitmap bitmap;

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();

        InputStream stream = conn.getInputStream();
        bitmap = BitmapFactory.decodeStream(stream);

        return bitmap;
    }
}
