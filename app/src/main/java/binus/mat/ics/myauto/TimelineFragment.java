package binus.mat.ics.myauto;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.vipulasri.timelineview.TimelineView;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import binus.mat.ics.myauto.structures.CarResponseStructure;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class TimelineFragment extends Fragment {

    // OkHttp
    public static final MediaType JSON = MediaType.get("application/json");
    public String PostResponse = "";

    CarResponseStructure[] responseArray;

    Gson gson = new Gson();
    OkHttpClient client = new OkHttpClient();

    // Timeline
    private RecyclerView mRecyclerView;
    private TimeLineAdapter mAdapter;

    // Temporary data
    private ArrayList<DataTimeline> texts;

    public class TimeLineViewHolder extends RecyclerView.ViewHolder {
        private TimelineView mTimelineView;
        private TextView mTextView;
        private DataTimeline mDt;
        private CardView mCardView;

        public TimeLineViewHolder(View itemView, int viewType) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.text_view);
            mTimelineView = itemView.findViewById(R.id.timeline);
            mTimelineView.initLine(viewType);
            mCardView = itemView.findViewById(R.id.card_view);
            mRecyclerView.setNestedScrollingEnabled(false);
        }

        public void bind(DataTimeline dt) {
            mDt = dt;
            mTextView.setText(mDt.title);
            mCardView.setOnClickListener(view -> {
                Toast.makeText(getContext(), String.valueOf(mDt.count), Toast.LENGTH_LONG).show();
            });
        }
    }

    public class TimeLineAdapter extends RecyclerView.Adapter<TimeLineViewHolder> {

        @NonNull
        @Override
        public TimeLineViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = View.inflate(getActivity(), R.layout.item_view, null);
            return new TimeLineViewHolder(view, i);
        }

        @Override
        public void onBindViewHolder(@NonNull TimeLineViewHolder timeLineViewHolder, int i) {
            timeLineViewHolder.bind(texts.get(i));
        }

        @Override
        public int getItemCount() {
            return texts.size();
        }

        @Override
        public int getItemViewType(int position) {
            return TimelineView.getTimeLineViewType(position, getItemCount());
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        texts = new ArrayList<>();
        for (int i = 1; i <= 9; ++i) {
            texts.add(new DataTimeline(i, "Aashadshd" + i));
        }
    }

    public class DataTimeline {
        int count;
        String title;

        public DataTimeline(int count, String title){
            this.count = count;
            this.title = title;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //returning our layout file
        //change R.layout.yourlayoutfilename for each of your fragments
        View view = inflater.inflate(R.layout.fragment_timeline, container, false);

        mRecyclerView = view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(view1 -> Snackbar.make(view1, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());

        MainMenuActivity mainmenu = (MainMenuActivity) getActivity();

        // TODO change URL
        new DownloadImageFromInternet((ImageView) view.findViewById(R.id.imageView))
                .execute(mainmenu.responseArray[0].img_url);

        updateUI();

        return view;
    }

    private void updateUI() {
        if (mAdapter == null) {
            mAdapter = new TimeLineAdapter();
            mRecyclerView.setAdapter(mAdapter);
        } else {
            // enter update data code below

            //.......
            mAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private class DownloadImageFromInternet extends AsyncTask<String, Void, Bitmap> {
        ImageView imageView;

        public DownloadImageFromInternet(ImageView imageView) {
            this.imageView = imageView;
            //Toast.makeText(getContext(), "Please wait, it may take a few minute...", Toast.LENGTH_SHORT).show();
        }

        protected Bitmap doInBackground(String... urls) {
            String imageURL = urls[0];
            Bitmap bimage = null;
            try {
                InputStream in = new java.net.URL(imageURL).openStream();
                bimage = BitmapFactory.decodeStream(in);

            } catch (Exception e) {
                Log.e("Error Message", e.getMessage());
                e.printStackTrace();
            }
            return bimage;
        }

        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);
        }
    }
}