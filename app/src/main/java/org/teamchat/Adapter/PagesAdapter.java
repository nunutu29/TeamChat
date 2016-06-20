package org.teamchat.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.teamchat.R;
import org.teamchat.Model.Pages;

import java.util.ArrayList;

/**
 * Created by Johnny
 */
public class PagesAdapter extends RecyclerView.Adapter<PagesAdapter.ViewHolder> {
    private ArrayList<Pages> pagesArrayList;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView titolo, descrizione, count;
        public ViewHolder(View view) {
            super(view);
            titolo = (TextView) view.findViewById(R.id.name);
            descrizione = (TextView) view.findViewById(R.id.message);
            count = (TextView) view.findViewById(R.id.count);
        }
    }

    public PagesAdapter(ArrayList<Pages> pagesArrayList){
        this.pagesArrayList = pagesArrayList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position){
        Pages pages = pagesArrayList.get(position);
        holder.titolo.setText(pages.getTitle());
        holder.descrizione.setText(pages.getDescription());
        if(pages.getUnreadCount() > 0)
        {
            holder.count.setText(String.valueOf(pages.getUnreadCount()));
            holder.count.setVisibility(View.VISIBLE);
        }
        else
            holder.count.setVisibility(View.GONE);
    }

    public int getItemCount(){
        return pagesArrayList.size();
    }

    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener{
        private GestureDetector gestureDetector;
        private PagesAdapter.ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final PagesAdapter.ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildAdapterPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e){
            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if(child != null && clickListener != null && gestureDetector.onTouchEvent(e)){
                clickListener.onClick(child, rv.getChildAdapterPosition(child));
            }
            return  false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }

    }

}
