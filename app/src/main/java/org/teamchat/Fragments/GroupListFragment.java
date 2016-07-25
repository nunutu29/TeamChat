package org.teamchat.Fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.teamchat.Activity.MainActivity;
import org.teamchat.Adapter.PagesAdapter;
import org.teamchat.App.Config;
import org.teamchat.App.MyApplication;
import org.teamchat.GCM.GcmIntentService;
import org.teamchat.R;
import org.teamchat.Helper.SimpleDividerItemDecoration;
import org.teamchat.Model.Pages;

import java.util.ArrayList;

public class GroupListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{
    //private static final String TAG = GroupListFragment.class.getSimpleName();
    public SwipeRefreshLayout swipeRefreshLayout;
    private PagesAdapter mAdapter;
    private ArrayList<Pages> pagesArrayList;

    private OnFragmentInteractionListener mListener;

    public GroupListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_group_list, container, false);
        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        toolbar.setTitle(MyApplication.getInstance().getPrefManager().getUser().getName());

        TextView subTitle = (TextView)rootView.findViewById(R.id.subtitle);
        subTitle.setText(MyApplication.getInstance().getPrefManager().getUser().getEmail());

        ImageView imageView = (ImageView)rootView.findViewById(R.id.imageView);
        MyApplication.getInstance().getPrefManager().getUser().setAvatar(getContext(), imageView, false);

        FloatingActionButton addButton = (FloatingActionButton) rootView.findViewById(R.id.fab_add);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.OpenCreateGroup();
            }
        });

        swipeRefreshLayout = (SwipeRefreshLayout)rootView.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);

        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        pagesArrayList = new ArrayList<>();
        mAdapter = new PagesAdapter(pagesArrayList);
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnItemTouchListener(new PagesAdapter.RecyclerTouchListener(getContext(),
                recyclerView, new PagesAdapter.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                // when chat is clicked, launch full chat thread activity
                Pages page = pagesArrayList.get(position);
                //Intent intent = new Intent(getContext(), ChatListActivity.class);
                //intent.putExtra("IDPAGINA", page.getId());
                //intent.putExtra("NOME", page.getTitle());
                //startActivity(intent);
                mListener.OpenChatList(page.getId());
            }

            @Override
            public void onLongClick(View view, int position) {
                //Toast.makeText(getContext(), "Long Touch", Toast.LENGTH_SHORT).show();
            }
        }));
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity)getActivity()).CURRENT_FRAGMENT = Config.GROUPS_LIST_FRAGMENT;
        LoadLocal();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /*Other Methos*/
    public void LoadLocal(){
        pagesArrayList.clear();
        pagesArrayList.addAll(MyApplication.getInstance().getDbManager().getPagesList());
        mAdapter.notifyDataSetChanged();
    }

    public void UpdateCounters(String idPage) {
        Pages p = Pages.getPageByID(idPage, pagesArrayList);
        if(p == null) return;
        int index = pagesArrayList.indexOf(p);
        p.setUnreadCount(p.getUnreadCount() + 1);
        pagesArrayList.remove(index);
        pagesArrayList.add(index, p);
        mAdapter.notifyItemChanged(index);

    }

    public void subscribeToAllTopics() {
        for (Pages cr : pagesArrayList) {
            Intent intent = new Intent(getContext(), GcmIntentService.class);
            intent.putExtra(GcmIntentService.KEY, GcmIntentService.SUBSCRIBE);
            intent.putExtra(GcmIntentService.TOPIC, "topic_" + cr.getId());
            getActivity().startService(intent);
        }

    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        mListener.StartSync();
    }

    public interface OnFragmentInteractionListener {
        void OpenChatList(String idPage);
        void OpenCreateGroup();
        void StartSync();
    }
}