package binus.mat.ics.myauto;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;

import java.io.IOException;
import java.sql.Time;
import java.util.HashMap;
import java.util.Map;

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

    Gson gson = new Gson();

    OkHttpClient client = new OkHttpClient();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //returning our layout file
        //change R.layout.yourlayoutfilename for each of your fragments
        return inflater.inflate(R.layout.fragment_timeline, container, false);

    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String PostUrl = "http://wendrian.duckdns.org/stanley/myauto/getVehiclesFromUser.php";
        String json = "{\"asas\": 1}";

        try {
            postJson(PostUrl, json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void postJson(String url, String json) throws IOException {

        OkHttpClient client = new OkHttpClient();

        RequestBody body = RequestBody.create(JSON, json);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String myResponse = response.body().string();

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        PostResponse = myResponse;
                        ResponseStructure[] responseArray = gson.fromJson(PostResponse, ResponseStructure[].class);

                        updateView(responseArray);
                    }
                });
            }
        });
    }

    private void updateView(ResponseStructure[] responseArray) {
        Log.d("askkas", responseArray[0].type);
        getActivity().setTitle(responseArray[0].brand + " " + responseArray[0].type + " ▾");
    }

    class ResponseStructure {
        int ID;
        int cat_id;
        String category;
        int brand_id;
        String brand;
        int vehicle_type_id;
        String type;
        int make_year;
        int stnk_year;
        int odometer;
    }
}