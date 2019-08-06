package com.kakaopay.kidongyun;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class YunListActivity extends AppCompatActivity {

    // Class : YunListActivity.
    // Description : 이미지 검색 결과를 보여주기 위한 ListView Activity 클래스.

    private final long FINISH_INTERVAL_TIME = 2000;     // 어플 종료를 위한 시간 값
    private long   backPressedTime = 0;

    private boolean REQUEST_LOCK = false;

    // View 요소들
    ConstraintLayout yunListLayout;
    ConstraintLayout searchContainer;
    ListView listView;
    ImageView noResultImage;
    ImageButton searchBtn;
    EditText searchEditText;
    ProgressBar progressBar;

    YunImageRequest yunImageRequest;
    ArrayList<YunData> yunDatas;

    YunListViewAdapter yunListViewAdapter;

    YunAnimation yunAnimation;
    YunScrollAnimationController yunScrollAnimationController;

    String searchString = null;     // 검색 키워드를 가지고 있는 변수

    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            if(msg.what == yunImageRequest.NO_RESULT) {
                Log.d("YUN", "handleMessage - NO_RESULT ... ");

                if(yunListViewAdapter.isEmpty()) {
                    noResultImage.setVisibility(View.VISIBLE);
                    listView.setVisibility(View.INVISIBLE);
                }

                progressBar.setVisibility(View.GONE);
                Snackbar.make(getWindow().getDecorView().getRootView(), "검색 결과가 없습니다.", Snackbar.LENGTH_SHORT).show();

                REQUEST_LOCK = true;

            } else {
                noResultImage.setVisibility(View.INVISIBLE);
                listView.setVisibility(View.VISIBLE);

                // 가져온 이미지 데이터를 YunData 타입의 배열리스트에 저장.
                yunDatas.add((YunData)msg.obj);
                yunListViewAdapter.notifyDataSetChanged();

                if(!YunImageRequest.IS_LOCK) {
                    // 이미지 가져오는 작업이 끝나게 되면 progressBar를 숨김.
                    progressBar.setVisibility(View.GONE);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        getSupportActionBar().setTitle("KIDONG YUN");

        // View들 초기화.
        yunListLayout = findViewById(R.id.yunListLayout);
        searchContainer = findViewById(R.id.searchContainer);
        searchBtn = findViewById(R.id.searchBtn);
        searchEditText = findViewById(R.id.searchEditText);
        progressBar = findViewById(R.id.progressBar);
        listView = findViewById(R.id.listView);
        noResultImage = findViewById(R.id.noResultImage);

        yunImageRequest = new YunImageRequest(handler);
        yunDatas = new ArrayList<>();

        yunListViewAdapter = new YunListViewAdapter(getApplicationContext(), R.layout.list_item, yunDatas);
        listView.setAdapter(yunListViewAdapter);

        yunScrollAnimationController = new YunScrollAnimationController(listView);
        yunScrollAnimationController.addScrollUpAnimation(new YunScrollAnimation(searchContainer, this, 50, 1));
        yunScrollAnimationController.addScrollDownAnimation(new YunScrollAnimation(searchContainer, this, 1, 50));
        yunScrollAnimationController.start();

        // Listener 함수들 초기화.
        initSearchListeners();
        initListViewListners();
    }

    @Override
    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;

        if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime)
        {
            ActivityCompat.finishAffinity(this);
        }
        else
        {
            backPressedTime = tempTime;
            Snackbar.make(getWindow().getDecorView().getRootView(), "한번 더 누르면 종료됩니다.", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void initSearchListeners() {

        // 검색 버튼을 클릭했을 때 애니메이션 작업과 이미지 검색 요청을 위한 부분.
        searchBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    ((ImageButton)view).setColorFilter(Color.parseColor("#BDBDBD"), PorterDuff.Mode.MULTIPLY);

                    btnEffect();
                    offKeyboard(view);
                    search();
                }

                return false;
            }
        });

        // Keyboard 각 버튼들이 눌렸을 때 이벤트를 처리할 수 있는 함수.
        searchEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {

                // 엔터키가 눌렸을 때 키보드를 내리고 이미지 검색을 요청 하는 함수.
                if((keyEvent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    offKeyboard(view);
                    search();
                }
                return false;
            }
        });
    }

    private void btnEffect() {
        int colorFrom = getResources().getColor(R.color.colorYellow);
        int colorTo = getResources().getColor(R.color.colorText);
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(250); // milliseconds
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                searchBtn.setColorFilter((int) animator.getAnimatedValue());
            }

        });

        colorAnimation.start();
    }

    private void initListViewListners() {

        // listView에서 Scroll 이벤트를 위한 함수
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                // listview의 내용물이 마지막까지 도달했을 때 새로운 데이터를 갱신하는 부분.
                if(firstVisibleItem + visibleItemCount >= totalItemCount && totalItemCount > 0) {
                    if(!YunImageRequest.IS_LOCK && !REQUEST_LOCK) {
                        progressBar.setVisibility(View.VISIBLE);

                        yunImageRequest = new YunImageRequest(handler);
                        yunImageRequest.setQuery(searchString).nextPage();
                        yunImageRequest.start();


                    }
                }
            }

            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) { }
        });

        // listView에서 각 list들을 클릭했을 때 액티비티 전환하는 부분.
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent=new Intent(YunListActivity.this, YunDetailActivity.class);
                intent.putExtra("docUrl", yunDatas.get(position).getDocUrl());
                intent.putExtra("imageUrl", yunDatas.get(position).getImageUrl());

                startActivity(intent);
                overridePendingTransition(R.anim.enter, R.anim.exit);
            }
        });
    }

    // 이미지 검색을 실시하는 함수. 완료되면 YunData타입의 데이터들이 배열리스트에 저장된다.
    private void search() {

        // 이미지 검색이 먼저 실시되고 있는지 확인하는 부분.
        if(!YunImageRequest.IS_LOCK && checkSearchText()) {
            REQUEST_LOCK = false;

            yunAnimation = new YunAnimation(searchContainer, getApplicationContext(),600);
            searchContainer.startAnimation(yunAnimation);

            yunDatas.clear();
            yunImageRequest.intializePage();

            progressBar.setVisibility(View.VISIBLE);
            searchString = searchEditText.getText().toString();

            yunImageRequest = new YunImageRequest(handler);
            yunImageRequest.setQuery(searchString);
            yunImageRequest.start();
        }
    }

    // 소프트 키보드 내리는 기능을 함수.
    private void offKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    // 검색어 예외처리 함수
    private boolean checkSearchText() {
        if(searchEditText.getText().toString().equals("")) {
            Snackbar.make(getWindow().getDecorView().getRootView(), "검색어를 입력해 주세요.", Snackbar.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

}

class YunListViewAdapter extends BaseAdapter {

    // Class : YunListViewAdapter.
    // Description : ListView 를 표현하기 위한 adapter 클래스.

    private LayoutInflater layoutInflater;
    private int layout;
    private ArrayList<YunData> yunDatas;

    public YunListViewAdapter(Context context, int layout, ArrayList<YunData> yunDatas) {
        this.layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.layout = layout;
        this.yunDatas = yunDatas;
    }

    @Override
    public int getCount() { return yunDatas.size(); }

    @Override
    public Object getItem(int position) {
        return yunDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        // ListView에 존재하는 각 list들의 data들을 넣는 작업을 진행.

        if (view == null)
            view = layoutInflater.inflate(layout, viewGroup, false);

        ImageView thumbnail = view.findViewById(R.id.thumbnail);
        TextView width = view.findViewById(R.id.width);
        TextView height = view.findViewById(R.id.height);
        TextView displaySitename = view.findViewById(R.id.displaySiteName);
        TextView year = view.findViewById(R.id.year);
        TextView collection = view.findViewById(R.id.collection);

        thumbnail.setImageBitmap(yunDatas.get(i).getThumbnailUrl());
        width.setText(yunDatas.get(i).getWidth() + "");
        height.setText(yunDatas.get(i).getHeight() + "");
        displaySitename.setText(yunDatas.get(i).getDisplaySiteName());
        year.setText(yunDatas.get(i).getDate());
        collection.setText(yunDatas.get(i).getCollection());

        return view;
    }
}
