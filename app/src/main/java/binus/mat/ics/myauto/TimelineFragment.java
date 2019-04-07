package binus.mat.ics.myauto;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
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

import com.facebook.shimmer.ShimmerFrameLayout;
import com.github.vipulasri.timelineview.TimelineView;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Time;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import binus.mat.ics.myauto.structures.ActivityResponseStructure;
import binus.mat.ics.myauto.structures.CarResponseStructure;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class TimelineFragment extends Fragment {
    private static final String key = "haehehaheah";
    private int arrayIndex;

    public static TimelineFragment newInstance(int index) {
        Bundle args = new Bundle();
        args.putInt(key, index);
        TimelineFragment fragment = new TimelineFragment();
        fragment.setArguments(args);
        return fragment;
    }

    // OkHttp
    public static final MediaType JSON = MediaType.get("application/json");
    public String PostResponse = "";

    CarResponseStructure[] responseArray;
    ActivityResponseStructure[] activityResponseArray;

    Gson gson = new Gson();
    OkHttpClient client = new OkHttpClient();

    // Shimmer
    private ShimmerFrameLayout mImageViewShimmerLayout;
    private ShimmerFrameLayout mRecyclerViewShimmerLayout;

    // Timeline
    private RecyclerView mRecyclerView;
    private TimeLineAdapter mAdapter;

    // Temporary data
    private ArrayList<DataTimeline> texts;

    public class TimeLineViewHolder extends RecyclerView.ViewHolder {
        private TimelineView mTimelineView;
        private TextView mTextView1, mTextView2, mTextView3, mTextView4, mTextView5, mTextView6;
        private DataTimeline mDt;
        private CardView mCardView;

        public TimeLineViewHolder(View itemView, int viewType) {
            super(itemView);
            mTextView1 = itemView.findViewById(R.id.text_view1);
            mTextView2 = itemView.findViewById(R.id.text_view2);
            mTextView3 = itemView.findViewById(R.id.text_view3);
            mTextView4 = itemView.findViewById(R.id.text_view4);
            mTextView5 = itemView.findViewById(R.id.text_view5);
            mTextView6 = itemView.findViewById(R.id.text_view6);

            mTimelineView = itemView.findViewById(R.id.timeline);
            mTimelineView.initLine(viewType);
            mCardView = itemView.findViewById(R.id.card_view);
            mRecyclerView.setNestedScrollingEnabled(false);
        }

        public void bind(DataTimeline dt) {
            mDt = dt;
            mTextView1.setText(mDt.title);
            mTextView2.setText(Integer.toString(mDt.odometer) + " km");
            mTextView3.setText(mDt.location);
            mTextView4.setText(mDt.date);
            mTextView5.setText(mDt.price);
            mTextView6.setText("");
            mCardView.setOnClickListener(view -> {
                Toast.makeText(getContext(), String.valueOf(mDt.count), Toast.LENGTH_LONG).show();
            });
        }
    }

    public class TimeLineAdapter extends RecyclerView.Adapter<TimeLineViewHolder> {

        @NonNull
        @Override
        public TimeLineViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = null;
            if (i % 2 == 0) view = View.inflate(getActivity(), R.layout.timeline_item_view, null);
            else view = View.inflate(getActivity(), R.layout.timeline_item_view_upcoming, null);
            return new TimeLineViewHolder(view, i/2);
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
            int type = TimelineView.getTimeLineViewType(position, getItemCount());
            // posisi array terbalik
            if (activityResponseArray[activityResponseArray.length-position-1].done == 1) return type*2;
            else return type*2+1;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainMenuActivity mainMenuActivity = (MainMenuActivity) getActivity();
        responseArray = mainMenuActivity.responseArray;
        texts = new ArrayList<>();

        arrayIndex = getArguments().getInt(key, 0);

        // make JSON to get vehicle data
        SharedPreferences mSharedPref = getActivity().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
        Map<String, String> postParam = new HashMap<>();
        postParam.put("user_id", mSharedPref.getString("user_id", "null"));
        postParam.put("login_hash", mSharedPref.getString("user_hash", "null"));
        postParam.put("vehicle_id", String.valueOf(responseArray[arrayIndex].vehicle_id));

        // Convert Map to JSONObject
        JSONObject jObj = new JSONObject(postParam);

        // Get vehicle data
        RequestBody body = RequestBody.create(JSON, jObj.toString());
        // TODO change url
        Request request = new Request.Builder().url("http://wendrian.duckdns.org/stanley/myauto/api/vehicleactivities.php").post(body).build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                activityResponseArray = gson.fromJson(response.body().string(), ActivityResponseStructure[].class);

                // format price
                DecimalFormat kursIndonesia = (DecimalFormat) DecimalFormat.getCurrencyInstance();
                DecimalFormatSymbols formatRp = new DecimalFormatSymbols();

                formatRp.setCurrencySymbol("Rp. ");
                formatRp.setMonetaryDecimalSeparator(',');
                formatRp.setGroupingSeparator('.');

                kursIndonesia.setDecimalFormatSymbols(formatRp);

                // format timestamp
                SimpleDateFormat inputDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                SimpleDateFormat outputDate = new SimpleDateFormat("dd MMM yyyy");

                if (activityResponseArray != null && activityResponseArray[0].result == 1) {
                    for (int i = activityResponseArray.length - 1; i >= 0; --i) {
                        //parse date
                        String date = "1970-01-01 00:00";
                        try {
                            date = outputDate.format(inputDate.parse(activityResponseArray[i].timestamp));
                        } catch (ParseException e) {
                        }

                        String title = activityResponseArray[i].activity_type_name;
                        int odometer = activityResponseArray[i].odometer;
                        String location = activityResponseArray[i].location;
                        String price = kursIndonesia.format(activityResponseArray[i].price);
                        texts.add(new DataTimeline(i, title, odometer, location, date, price));
                    }

                    // show recyclerview
                    getActivity().runOnUiThread(() -> {
                        mRecyclerViewShimmerLayout.stopShimmerAnimation();
                        mRecyclerViewShimmerLayout.setVisibility(View.GONE);
                        mRecyclerView = getView().findViewById(R.id.recycler_view);
                        mRecyclerView.setVisibility(View.VISIBLE);
                        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                        updateUI();
                    });
                } else if (activityResponseArray != null && responseArray != null && responseArray[0].result == 2) {
                    // no vehicle present, set fragment to NoVehicleFragment
                    NoVehicleFragment noVehicleFragment = new NoVehicleFragment();
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.content_frame, noVehicleFragment, "findThisFragment")
                            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                            .addToBackStack(null)
                            .commit();
                } else {
                    // vehicle is present, but no activity
                    // show no activity
                    getActivity().runOnUiThread(() -> {
                        mRecyclerViewShimmerLayout.stopShimmerAnimation();
                        mRecyclerViewShimmerLayout.setVisibility(View.GONE);
                        CardView noActivityText = getView().findViewById(R.id.no_activity_text);
                        noActivityText.setVisibility(View.VISIBLE);
                    });
                }
            }
        });

    }

    public class DataTimeline {
        int count;
        String title;
        int odometer;
        String location;
        String date;
        String price;

        public DataTimeline(int count, String title, int odometer, String location, String date, String price){
            this.count = count;
            this.title = title;
            this.odometer = odometer;
            this.location = location;
            this.date = date;
            this.price = price;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //returning our layout file
        //change R.layout.yourlayoutfilename for each of your fragments
        View view = inflater.inflate(R.layout.fragment_timeline, container, false);
        mImageViewShimmerLayout = view.findViewById(R.id.imageView_shimmer);
        mRecyclerViewShimmerLayout = view.findViewById(R.id.recyclerView_shimmer);
        mRecyclerView = view.findViewById(R.id.recycler_view);

        mRecyclerView.setVisibility(View.GONE);
        mRecyclerViewShimmerLayout.startShimmerAnimation();

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(view1 -> Snackbar.make(view1, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());

        MainMenuActivity mainmenu = (MainMenuActivity) getActivity();

        new DownloadImageFromInternet(view.findViewById(R.id.imageView))
                .execute(mainmenu.responseArray[arrayIndex].img_url);

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
            mImageViewShimmerLayout.startShimmerAnimation();
            imageView.setVisibility(View.GONE);
            //Toast.makeText(getContext(), "Please wait, it may take a few minute...", Toast.LENGTH_SHORT).show();
        }

        protected Bitmap doInBackground(String... urls) {
            String imageURL = urls[0];
            Bitmap bimage = null;
            try {
                //Thread.sleep(2000);
                InputStream in = new java.net.URL(imageURL).openStream();
                bimage = BitmapFactory.decodeStream(in);

            } catch (Exception e) {
                Log.e("Error Message", e.getMessage());
                e.printStackTrace();
            }
            return bimage;
        }

        protected void onPostExecute(Bitmap result) {
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageBitmap(result);
            mImageViewShimmerLayout.stopShimmerAnimation();
            mImageViewShimmerLayout.setVisibility(View.GONE);
        }
    }
}