package com.hu.hcy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PopupMenuView.PopupItemClickListener, MyAdapter.OnItemLongClickListener {

    RecyclerView mRecyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        setContentView(R.layout.activity_main2);
        initView2();
//        initView();
    }

    private void initView2() {
        List<String> mDataList = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            mDataList.add(i + "");
        }
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        MyAdapter adapter = new MyAdapter(this, mDataList);
        adapter.setOnItemLongClickListener(this);
        mRecyclerView.setAdapter(adapter);
    }

    private void initView() {
        findViewById(R.id.tv).setOnClickListener(this::showPopMenuView);
        findViewById(R.id.left_top).setOnClickListener(this::showPopMenuView);
        findViewById(R.id.left_bottom).setOnClickListener(this::showPopMenuView);
        findViewById(R.id.right_top).setOnClickListener(this::showPopMenuView);
        findViewById(R.id.right_bottom).setOnClickListener(this::showPopMenuView);
    }


    private void showPopMenuView(View anchor) {
        String[] labels = new String[]{getString(R.string.copy), getString(R.string.delete),
                getString(R.string.reply), getString(R.string.favor),
                getString(R.string.revoke), getString(R.string.save)};
        int[] icons = new int[]{R.drawable.pop_icon_copy, R.drawable.pop_icon_delete,
                R.drawable.pop_icon_reply, R.drawable.pop_icon_favor,
                R.drawable.pop_icon_revocation, R.drawable.pop_icon_save};
        PopupMenuView popupMenuView = new PopupMenuView.Builder(this, labels, anchor)
                .setIcons(icons)
                .setItemClickListener(this)
                .build();
        popupMenuView.show();
    }

    @Override
    public void onItemClick(View contextView, String label) {
        Toast.makeText(this, label, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemLongClick(View view, int position) {
        showPopMenuView(view);
    }
}