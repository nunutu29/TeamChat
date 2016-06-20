package org.teamchat.Adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import org.teamchat.App.Config;
import org.teamchat.R;
import org.teamchat.Helper.Utils;
import org.teamchat.Model.Message;

public class ChatRoomThreadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    //private static String TAG = ChatRoomThreadAdapter.class.getSimpleName();

    private String userId;
    private String type;
    private int SELF = 100;
    private ArrayList<Message> messageArrayList;

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView message, timestamp, username;
        RelativeLayout relativeLayout;
        public ViewHolder(View view) {
            super(view);
            message = (TextView) itemView.findViewById(R.id.message);
            timestamp = (TextView) itemView.findViewById(R.id.timestamp);
            username = (TextView)itemView.findViewById(R.id.username);
            relativeLayout = (RelativeLayout)itemView.findViewById(R.id.relative_layout);
        }
    }

    public ChatRoomThreadAdapter(ArrayList<Message> messageArrayList, String userId, String type) {
        this.messageArrayList = messageArrayList;
        this.userId = userId;
        this.type = type;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;

        // view type is to identify where to render the chat message
        // left or right
        if (viewType == SELF) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_item_self, parent, false);
        } else {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_item_other, parent, false);
        }
        itemView.setTag(viewType);
        return new ViewHolder(itemView);
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageArrayList.get(position);
        if (message != null && message.getUser().getId().equals(userId)) {
            return SELF;
        }
        return position;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        Message message = messageArrayList.get(position);
        ((ViewHolder) holder).message.setText(message.getMessage());

        String timestamp = Utils.getTimeStamp(message.getCreatedAt());
        ((ViewHolder) holder).timestamp.setText(timestamp);

        if(((ViewHolder)holder).username != null) {
            if(type.equals(Config.SINGLE_CHAT_ROOM))
                ((ViewHolder) holder).username.setVisibility(View.GONE);
            else
                ((ViewHolder) holder).username.setText(message.getUser().getName());
        }
    }

    @Override
    public int getItemCount() {
        return messageArrayList.size();
    }
}

