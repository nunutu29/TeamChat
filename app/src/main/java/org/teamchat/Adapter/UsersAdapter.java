package org.teamchat.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.teamchat.R;
import org.teamchat.Helper.Utils;
import org.teamchat.Model.User;

import java.util.ArrayList;

/**
 * Created by Johnny
 */
public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {
    private Context context;
    private ArrayList<User> userArrayList;
    private ClickListener clickListener;

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView nome, joinDate;
        public ImageView avatar;
        public ViewHolder(View view){
            super(view);
            nome = (TextView)view.findViewById(R.id.name);
            joinDate = (TextView)view.findViewById(R.id.join_date);
            avatar = (ImageView)view.findViewById(R.id.avatar);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(clickListener != null)
                        clickListener.onClick(v, getAdapterPosition());
                }
            });
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(clickListener != null)
                        clickListener.onLongClick(v, getAdapterPosition());
                    return true;
                }
            });
        }
    }

    public UsersAdapter(Context context, ArrayList<User> b){
        this.context = context;
        this.userArrayList = b;
    }

    public void setClickListener(ClickListener click){
        this.clickListener = click;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_user_row, parent, false);
        return new ViewHolder(item);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position){
        User user = userArrayList.get(position);
        holder.nome.setText(user.getName());
        holder.joinDate.setText(Utils.getDateTimeStamp(user.getJoinDate()));
        user.setAvatar(this.context, holder.avatar, false);
        //holder.data_creazione = user.
    }

    public int getItemCount(){
        return userArrayList.size();
    }

    public interface ClickListener{
        void onClick(View view, int pos);
        boolean onLongClick(View view, int pos);
    }

}
