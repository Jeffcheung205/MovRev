package com.example.movrev;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;

public class CommentAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<MoreInformation.Comment> comments;

    public CommentAdapter(Context context, ArrayList<MoreInformation.Comment> comments) {
        this.context = context;
        this.comments = comments;
    }

    @Override
    public int getCount() {
        return comments.size();
    }

    @Override
    public Object getItem(int position) {
        return comments.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.comment, parent, false);
        }


        MoreInformation.Comment comment = comments.get(position);

        TextView userTextView = convertView.findViewById(R.id.textViewUser1);
        TextView timeTextView = convertView.findViewById(R.id.textViewTime1);
        TextView contentTextView = convertView.findViewById(R.id.textViewContent1);
        RatingBar ratingBar = convertView.findViewById(R.id.commentRating);

        userTextView.setText(comment.getUserName());
        timeTextView.setText(comment.getTime());
        contentTextView.setText(comment.getContent());
        ratingBar.setRating(comment.getRating());

        return convertView;
    }
}
