package com.kakaopay.kidongyun;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class YunListActivity extends AppCompatActivity {

    // Class : YunListActivity
    // Description : 이미지 검색 결과를 보여주기 위한 ListView Activity 클래스.



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        getSupportActionBar().setTitle("KIDONG YUN");
    }

}

class YunListViewAdapter extends BaseAdapter {

    // Class : YunListViewAdapter
    // Description : ListView 를 표현하기 위한 adapter 클래스

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
        year.setText(yunDatas.get(i).getYear());
        collection.setText(yunDatas.get(i).getCollection());

        return view;
    }
}
