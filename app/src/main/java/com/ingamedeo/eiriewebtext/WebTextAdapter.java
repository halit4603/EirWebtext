package com.ingamedeo.eiriewebtext;

import android.content.Context;
import android.database.Cursor;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ingamedeo.eiriewebtext.db.WebTextsTable;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by ingamedeo on 25/07/15.
 */

public class WebTextAdapter extends CursorAdapter {

    String time = "XX:XX";
    DateFormat timeFormat;

    private static class ViewHolder {
        public TextView txtMessage;
        public ImageView imageMessage;
        public LinearLayout txtInfoLayout;
        public TextView txtInfo;
        public TextView txtInfoStatus;
        public LinearLayout content;
        public LinearLayout contentWithBG;
    }

    public WebTextAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        timeFormat = DateFormat.getTimeInstance();
    }

    private void setAlignment(ViewHolder holder) {

            holder.contentWithBG.setBackgroundResource(R.drawable.background);

            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.contentWithBG.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            holder.contentWithBG.setLayoutParams(layoutParams);

            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) holder.content.getLayoutParams();
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            holder.content.setLayoutParams(lp);
            layoutParams = (LinearLayout.LayoutParams) holder.txtMessage.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            holder.txtMessage.setLayoutParams(layoutParams);

            layoutParams = (LinearLayout.LayoutParams) holder.txtInfoLayout.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            holder.txtInfoLayout.setLayoutParams(layoutParams);

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        ViewHolder holder = new ViewHolder();
        View v = null;

        LayoutInflater inflater = LayoutInflater.from(context);

        v = inflater.inflate(R.layout.list_item_chat_message, null);
        holder.txtMessage = (TextView) v.findViewById(R.id.txtMessage);
        holder.imageMessage = (ImageView) v.findViewById(R.id.imageMessage);
        holder.content = (LinearLayout) v.findViewById(R.id.content);
        holder.contentWithBG = (LinearLayout) v.findViewById(R.id.contentWithBackground);
        holder.txtInfoLayout = (LinearLayout) v.findViewById(R.id.txtInfoLayout);
        holder.txtInfo = (TextView) v.findViewById(R.id.txtInfo);
        holder.txtInfoStatus = (TextView) v.findViewById(R.id.txtInfoStatus);

        v.setTag(holder);

        return v;
    }

    @Override
    public void bindView(View v, Context c, Cursor cursor) {

        /* We already have our holder ready at this stage */
        ViewHolder holder = (ViewHolder) v.getTag();

        byte[] content = cursor.getBlob(cursor.getColumnIndex(WebTextsTable.CONTENT));
        long timestamp = cursor.getLong(cursor.getColumnIndex(WebTextsTable.TIMESTAMP));

        setAlignment(holder);

        time = timeFormat.format(new Date(timestamp * 1000)); //Values in the database are down-scaled, up-scale them

        if (holder.txtMessage != null) {
            String contentStr = Utils.fromBytesToUTF8String(content);
            holder.txtMessage.setText(contentStr);
            holder.txtMessage.setTextColor(c.getResources().getColor(R.color.white));
        }
        if (holder.txtInfo != null) {
            holder.txtInfo.setText(time);
        }

        holder.txtInfoStatus.setVisibility(View.GONE);
    }
}
