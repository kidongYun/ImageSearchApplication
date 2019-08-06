package com.kakaopay.kidongyun;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class YunDetailActivity extends AppCompatActivity implements View.OnTouchListener, ActivityCompat.OnRequestPermissionsResultCallback {

    // Class : YunDetailActivity.
    //Description : 선택된 이미지의 상세내용을 표현하기 위한 Activity 클래스.

    int WRITE_STORAGE_PERMISSION_CODE = 0;

    // View 요소들
    PhotoView image;
    ImageButton prev;
    ImageButton link;
    ImageButton download;
    ImageButton share;
    ProgressBar progressBar;

    // Button Click Animation
    ValueAnimator prevAnimation;
    ValueAnimator linkAnimation;
    ValueAnimator downloadAnimation;
    ValueAnimator shareAnimation;

    YunData yunData;
    Bitmap imageOfBitmap;   // bitmap 형식의 image 데이터를 저장

    private long downloadID;    // 다운로드시 필요한 id값

    YunImageLoader yunImageLoader;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            imageOfBitmap = (Bitmap)msg.obj;
            image.setImageBitmap(imageOfBitmap);

            progressBar.setVisibility(View.GONE);
        }
    };

    // 다운로드 완료되었을 때 발생하는 이벤트
    private BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            if(downloadID == id) {
                Snackbar.make(getWindow().getDecorView().getRootView(), "다운로드가 완료되었습니다.", Snackbar.LENGTH_SHORT).show();
            }
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
        share = findViewById(R.id.share);
        progressBar = findViewById(R.id.progressBar);

        // 버튼들 Listener에 연결.
        prev.setOnTouchListener(this);
        link.setOnTouchListener(this);
        download.setOnTouchListener(this);
        share.setOnTouchListener(this);

        Intent intent = new Intent(this.getIntent());
        registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        yunData = new YunData();
        yunData.setDocUrl(intent.getStringExtra("docUrl")).setImageUrl(intent.getStringExtra("imageUrl"));

        yunImageLoader = new YunImageLoader(handler, yunData.getImageUrl());
        yunImageLoader.start();

        progressBar.setVisibility(View.VISIBLE);

        initButtonAnimation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onDownloadComplete);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        // 각 ImageButton 들을 눌렀을 때 이벤트 처리하는 함수.

        if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            ((ImageButton)view).setColorFilter(Color.parseColor("#edc779"), PorterDuff.Mode.MULTIPLY);

            switch (view.getId()) {
                case R.id.prev :
                    // 뒤로가기 버튼을 눌렀을 때 Detail Activity를 종료하고 이전 Activity로 돌아간다.
                    super.onBackPressed();
                    prevAnimation.start();
                    overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
                    break;
                case R.id.link :
                    // 링크 버튼을 눌렀을 때 브라우저를 통해 해당 웹페이지에 연결.
                    linkAnimation.start();
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(yunData.getDocUrl()));
                    startActivity(browserIntent);
                    break;
                case R.id.download :
                    // 다운로드 버튼을 눌렀을 때 이미지 파일 로컬로 다운로드.
                    downloadAnimation.start();
                    checkDownloadPermission();
                    break;
                case R.id.share :
                    shareAnimation.start();
                    share();
                    break;
            }

        }
        else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            ((ImageButton)view).setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.MULTIPLY);
        }

        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // 안드로이드 하단 화면에서 제공하는 뒤로가기 버튼 클릭 시 이벤트 처리하는 함수. 여기서는 애니메이션 구현.
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }

    private void download() {

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(yunData.getImageUrl()))
                .setTitle("ImageSearchApplication")
                .setDescription("Downloading")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,  getDate() + ".png")
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true);

        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        downloadID = downloadManager.enqueue(request);
    }

    private void checkDownloadPermission() {
        // 다운로드 시 저장소 접근 권한에 대한 예외처리.
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {

            Snackbar.make(getWindow().getDecorView().getRootView(), "다운로드를 위해 외부 저장소 접근 권한이 필요합니다.", Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    ActivityCompat.requestPermissions(YunDetailActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},0);
                }

            }).show();

        } else {
            download();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // WRITE_STORAGE PERMISSION 얻고 난 이후에 실행되는 콜백 함수. 다운로드를 다시 진행한다.
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == WRITE_STORAGE_PERMISSION_CODE) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                download();
            }
        }
    }

    private String getDate() {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddhhmmss");

        return simpleDateFormat.format(date);
    }

    private void share() {
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + getDate() + ".png";

        OutputStream outputStream;
        File file = new File(path);

        try {
            outputStream = new FileOutputStream(file);
            imageOfBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID, new File(path)));
        Intent chooser = Intent.createChooser(intent, "Share");
        startActivity(chooser);
    }

    private void initButtonAnimation() {
        int colorFrom = getResources().getColor(R.color.colorYellow);
        int colorTo = getResources().getColor(R.color.colorText);

        prevAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        linkAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        downloadAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        shareAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);

        prevAnimation.setDuration(250);
        linkAnimation.setDuration(250);
        downloadAnimation.setDuration(250);
        shareAnimation.setDuration(250);

        prevAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                prev.setColorFilter((int) valueAnimator.getAnimatedValue());
            }
        });

        linkAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                link.setColorFilter((int) valueAnimator.getAnimatedValue());
            }
        });

        downloadAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                download.setColorFilter((int) valueAnimator.getAnimatedValue());
            }
        });

        shareAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                share.setColorFilter((int) valueAnimator.getAnimatedValue());
            }
        });
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
