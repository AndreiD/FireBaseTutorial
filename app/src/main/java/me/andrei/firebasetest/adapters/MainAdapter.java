package me.andrei.firebasetest.adapters;

import android.content.Context;
import android.text.Html;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import me.andrei.firebasetest.R;
import me.andrei.firebasetest.models.Message;

public class MainAdapter extends BaseAdapter {

  private final ArrayList<Message> data;
  LayoutInflater inflater;
  Context context;

  public MainAdapter(Context context, ArrayList<Message> data) {
    this.data = data;
    this.context = context;
    inflater = LayoutInflater.from(context);
  }

  @Override public int getCount() {
    return data.size();
  }

  @Override public Message getItem(int position) {
    return data.get(position);
  }

  @Override public long getItemId(int position) {
    return position;
  }

  @Override public View getView(int position, View convertView, ViewGroup parent) {
    MyViewHolder mViewHolder;

    if (convertView == null) {
      convertView = inflater.inflate(R.layout.row_message, parent, false);
      mViewHolder = new MyViewHolder(convertView);
      convertView.setTag(mViewHolder);
    } else {
      mViewHolder = (MyViewHolder) convertView.getTag();
    }

    Message currentListData = getItem(position);

    String author_text = currentListData.author;
    if (author_text.length() > 5) {
      author_text = author_text.substring(0, 5);
    }
    mViewHolder.textView_row_author.setText(author_text);
    Calendar cal = Calendar.getInstance(Locale.getDefault());
    cal.setTimeInMillis(currentListData.timestamp * 1000L);
    String date = DateFormat.format("KK:mma", cal).toString();

    mViewHolder.textView_row_message.setText(Html.fromHtml("<font color=\"#333333\">" + currentListData.message + "</font>" + " <small><font color=\"#888888\">" + date + "</font></small>"));

    return convertView;
  }

  private class MyViewHolder {
    private final TextView textView_row_author;
    private final TextView textView_row_message;

    public MyViewHolder(View item) {
      textView_row_author = (TextView) item.findViewById(R.id.textView_row_author);
      textView_row_message = (TextView) item.findViewById(R.id.textView_row_message);
    }
  }
}