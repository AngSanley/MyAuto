package binus.mat.ics.myauto;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
    }

    private void updateView() {
        Log.d("askkas", responseArray[0].type);
        getActivity().setTitle(responseArray[0].brand + " " + responseArray[0].type + " ▾");


        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.choose_vehicle));

        ArrayList<CharSequence> carType = new ArrayList<>();
        for (CarResponseStructure temp : responseArray) {
            carType.add(temp.brand + " " + temp.type);
        }

        CharSequence[] cs = carType.toArray(new CharSequence[carType.size()]);

        builder.setItems(cs, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    // pilih mobil
                }
            }
        });
        AlertDialog dialog = builder.create();

        dialog.show();
    }


}