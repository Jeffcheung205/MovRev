package com.example.movrev;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;

import java.util.ArrayList;

public class ForumAdapter extends ArrayAdapter<ArrayList<MoreInformation.Forum>> {

    public ForumAdapter(Context context, ArrayList<ArrayList<MoreInformation.Forum>> forums) {
        super(context, 0, forums);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.forumselectlistview, parent, false);
        }

        Button button1 = convertView.findViewById(R.id.forumButton1);
        Button button2 = convertView.findViewById(R.id.forumButton2);
        Button button3 = convertView.findViewById(R.id.forumButton3);

        ArrayList<MoreInformation.Forum> three_forum = getItem(position);

        // 每行显示最多三个论坛
        int startIndex = 0;
        if (startIndex < three_forum.size()) {
            MoreInformation.Forum forum = three_forum.get(startIndex);
            button1.setText(forum.getFoname());
            button1.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), ForumActivity.class);
                intent.putExtra("foid", forum.getFoid());
                intent.putExtra("foname", forum.getFoid());
                getContext().startActivity(intent);
            });
        } else {
            button1.setText("");
            button1.setOnClickListener(null);
        }

        if (startIndex + 1 < three_forum.size()) {
            MoreInformation.Forum forum = three_forum.get(startIndex + 1);
            button2.setText(forum.getFoname());
            button2.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), ForumActivity.class);
                intent.putExtra("foid", forum.getFoid());
                intent.putExtra("foname", forum.getFoid());
                getContext().startActivity(intent);
            });
        } else {
            button2.setText("");
            button2.setOnClickListener(null);
        }

        if (startIndex + 2 < three_forum.size()) {
            MoreInformation.Forum forum = three_forum.get(startIndex + 2);
            button3.setText(forum.getFoname());
            button3.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), ForumActivity.class);
                intent.putExtra("foid", forum.getFoid());
                intent.putExtra("foname", forum.getFoid());
                getContext().startActivity(intent);
            });
        } else {
            button3.setText("");
            button3.setOnClickListener(null);
        }

        return convertView;
    }

//    @Override
//    public int getCount() {
//        // 每行三个论坛，向上取整
//        return (int) Math.ceil(super.getCount() / 3.0);
//    }
}