package com.example.movrev;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ForumCommentAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<ForumActivity.ForumComment> forumcomments;

    public ForumCommentAdapter(Context context, ArrayList<ForumActivity.ForumComment> forumcomments) {
        this.context = context;
        this.forumcomments = forumcomments;
    }

    @Override
    public int getCount() {
        return forumcomments.size();
    }

    @Override
    public Object getItem(int position) {
        return forumcomments.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.forumlistview, parent, false);
        }


        ForumActivity.ForumComment fourmcomment = forumcomments.get(position);

        TextView userTextView = convertView.findViewById(R.id.textViewUser1);
        TextView timeTextView = convertView.findViewById(R.id.textViewTime1);
        TextView contentTextView = convertView.findViewById(R.id.textViewContent1);

        userTextView.setText(fourmcomment.getUserName());
        timeTextView.setText(fourmcomment.getTime());
        contentTextView.setText(fourmcomment.getComment());

        return convertView;
    }
}
