package binus.mat.ics.myauto;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import binus.mat.ics.myauto.structures.CarResponseStructure;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ManageVehicleActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<CarResponseStructure> vehicleData = new ArrayList<>();
    ArrayList <ManageVehicleActivity.Item> itemList = new ArrayList<>();
    ManageVehicleActivity.ItemArrayAdapter itemArrayAdapter;
    Button addNewVehicleButton;

    // OkHttp
    public static final MediaType JSON = MediaType.get("application/json");

    OkHttpClient client = new OkHttpClient()
            .newBuilder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .callTimeout(5, TimeUnit.SECONDS)
            .build();

    String AddVehicleUrl = "http://wendrian.duckdns.org/stanley/myauto/api/vehiclelist.php";
    String RemoveVehicleUrl = "http://wendrian.duckdns.org/stanley/myauto/api/deletevehicle.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_vehicle);

        // Get Vehicle data
        Bundle bundle = getIntent().getBundleExtra("bundle");
        vehicleData = (ArrayList<CarResponseStructure>) bundle.getSerializable("vehicle_data");

        itemArrayAdapter = new ManageVehicleActivity.ItemArrayAdapter(R.layout.manage_vehicle_item_view, itemList);
        recyclerView = findViewById(R.id.recycler_view_manage_vehicle);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(itemArrayAdapter);

        for (CarResponseStructure vehicles: vehicleData) {
            itemList.add(new ManageVehicleActivity.Item(vehicles.brand + " " + vehicles.type, vehicles.license_plate));
        }

        addNewVehicleButton = findViewById(R.id.add_vehicle_button);

        addNewVehicleButton.setOnClickListener(v -> {
            startActivity(new Intent(ManageVehicleActivity.this, AddNewVehicleActivity.class));
        });
    }

    public class Item {

        String carName;
        String licensePlate;

        public Item(String carName, String licensePlate) {
            this.carName = carName;
            this.licensePlate = licensePlate;
        }
    }

    public class ItemArrayAdapter extends RecyclerView.Adapter<ManageVehicleActivity.ItemArrayAdapter.ViewHolder> {

        //All methods in this adapter are required for a bare minimum recyclerview adapter
        private int listItemLayout;
        private ArrayList<ManageVehicleActivity.Item> itemList;
        // Constructor of the class
        public ItemArrayAdapter(int layoutId, ArrayList<ManageVehicleActivity.Item> itemList) {
            listItemLayout = layoutId;
            this.itemList = itemList;
        }

        // get the size of the list
        @Override
        public int getItemCount() {
            return itemList == null ? 0 : itemList.size();
        }


        // specify the row layout file and click for each row
        @Override
        public ManageVehicleActivity.ItemArrayAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(listItemLayout, parent, false);
            ManageVehicleActivity.ItemArrayAdapter.ViewHolder myViewHolder = new ManageVehicleActivity.ItemArrayAdapter.ViewHolder(view);
            return myViewHolder;
        }

        // load data in each row element
        @Override
        public void onBindViewHolder(final ManageVehicleActivity.ItemArrayAdapter.ViewHolder holder, final int listPosition) {
            TextView item = holder.item;
            TextView title = holder.title;
            title.setText(itemList.get(listPosition).carName);
            item.setText(itemList.get(listPosition).licensePlate);
        }

        // Static inner class to initialize the views of rows
        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public TextView title;
            public TextView item;
            public Button deleteButton;
            public ViewHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);
                title = itemView.findViewById(R.id.carNameField);
                item = itemView.findViewById(R.id.licensePlateField);
                deleteButton = itemView.findViewById(R.id.deleteButton);
                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("onclick", "onClick delete button" + getLayoutPosition() + " " + item.getText());
                        deleteVehicle(getLayoutPosition());
                    }
                });
            }
            @Override
            public void onClick(View view) {
                Log.d("onclick", "onClick " + getLayoutPosition() + " " + item.getText());
            }
        }

        public void removeItemAt(int position) {
            itemList.remove(position);
            notifyDataSetChanged();
        }
    }

    private void deleteVehicle(int layoutPosition) {
        // get vehicle data
        CarResponseStructure vehicle = vehicleData.get(layoutPosition);

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        processDelete(layoutPosition);
                        break;
                }
            }

            private void processDelete(int layoutPosition) {
                SharedPreferences mSharedPref = getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
                Map<String, String> postParam = new HashMap<>();
                postParam.put("user_id", mSharedPref.getString("user_id", "null"));
                postParam.put("login_hash", mSharedPref.getString("user_hash", "null"));
                postParam.put("vehicle_id", String.valueOf(vehicle.vehicle_id));
                JSONObject jObj = new JSONObject(postParam);

                RequestBody body = RequestBody.create(JSON, jObj.toString());
                Request request = new Request.Builder().url(RemoveVehicleUrl).post(body).build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e(getApplicationContext().toString(), e.toString());
                        call.cancel();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        runOnUiThread(() -> itemArrayAdapter.removeItemAt(layoutPosition));
                    }
                });
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(ManageVehicleActivity.this);
        builder.setMessage(getString(R.string.vehicle_remove_confirmation, vehicle.brand + " " + vehicle.type, vehicle.license_plate))
                .setTitle(getString(R.string.are_you_sure))
                .setPositiveButton(getString(R.string.yes), dialogClickListener)
                .setNegativeButton(getString(R.string.no), dialogClickListener)
                .show();
    }

}
