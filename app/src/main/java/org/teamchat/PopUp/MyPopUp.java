package org.teamchat.PopUp;

import android.content.Context;
import android.graphics.Point;
import android.support.v4.content.res.ResourcesCompat;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import org.teamchat.R;

import java.util.ArrayList;

/**
 * PopUP Class sopra un framelayout
 * Bisgona aver dichiarato un foreground nella framelayout.
 * Created by Johnny on 10/05/2016.
 */
public class MyPopUp extends PopupWindow {
    private Context context;
    PopupWindow popWindow;
    View inflatedView;
    private ArrayAdapter<Values> listViewArrayAdapter;
    private ArrayList<Values> listItems;
    private AdapterView.OnItemClickListener onItemClickListener;

    public MyPopUp(Context context, ArrayList<Values> listItems){
        this.context = context;
        this.listItems = listItems;
        LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflatedView = layoutInflater.inflate(R.layout.content_popup, null, false);

        popWindow = new PopupWindow(inflatedView);
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

    public void show(FrameLayout v){
        this.onShow(v);
    }

    public void close(){
        popWindow.dismiss();
    }

    private void onShow(final FrameLayout v){
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        final Point size = new Point();
        display.getSize(size);
        //int mDeviceHeight = size.y;

        ListView listView = (ListView) inflatedView.findViewById(R.id.commentsListView);
        getList(listView);

        // set height depends on the device size


        if(listView.getCount() > 5){
            View item = listViewArrayAdapter.getView(0, null, listView);
            item.measure(0, 0);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (5.5 * item.getMeasuredHeight()));
            listView.setLayoutParams(params);
        }

        popWindow.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        popWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popWindow.setBackgroundDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.bg_popup, null));

        popWindow.setFocusable(true);
        // make it outside touchable to dismiss the popup window
        popWindow.setOutsideTouchable(true);
        //set animation
        popWindow.setAnimationStyle(R.style.AnimationPopUp);
        // show the popup at bottom of the screen and set some margin at bottom ie,
        popWindow.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                v.setForeground(null);
            }
        });
        v.setForeground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.blur_effect, null));
        v.getForeground().setAlpha(80);
        popWindow.showAtLocation(v, Gravity.BOTTOM,0,0);
    }

    private void getList(ListView listView){
        listViewArrayAdapter = new ArrayAdapter<>(context, R.layout.list_popup_row, android.R.id.text1, listItems);
        listView.setAdapter(listViewArrayAdapter);
        listView.setOnItemClickListener(onItemClickListener);
    }

    public static class Values{
        private int key;
        private String value;
        public Values(int id, String value){
            this.key = id;
            this.value = value;
        }
        public int getKey(){
            return this.key;
        }
        @Override
        public String toString(){
            return this.value;
        }
    }
}
