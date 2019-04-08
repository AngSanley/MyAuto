package binus.mat.ics.myauto;


import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.vipulasri.timelineview.TimelineView;

import org.w3c.dom.Text;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class VehicleInfoFragment extends Fragment {
    private static final String key = "";
    private int arrayIndex;
    private MainMenuActivity mainMenuActivity;
    private TextView licensePlateText;
    private TextView licensePlateMonthText;
    private TextView licensePlateYearText;
    RecyclerView recyclerView;

    public VehicleInfoFragment() {
        // Required empty public constructor
    }

    public static VehicleInfoFragment newInstance(int index) {
        Bundle args = new Bundle();
        args.putInt(key, index);
        VehicleInfoFragment fragment = new VehicleInfoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // assign values
        arrayIndex = getArguments().getInt(key, 0);
        mainMenuActivity = (MainMenuActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_vehicle_info, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // check if array changed
        if (mainMenuActivity.responseArray.length-1 < arrayIndex) {
            arrayIndex = 0;
        }

        // check if no vehicle
        if (mainMenuActivity.responseArray[0].result == 2) {
            NoVehicleFragment noVehicleFragment = new NoVehicleFragment();
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, noVehicleFragment, "findThisFragment")
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .commit();
        }

        // assign view
        licensePlateText = getView().findViewById(R.id.licensePlateText);
        licensePlateMonthText = getView().findViewById(R.id.licensePlateMonthText);
        licensePlateYearText = getView().findViewById(R.id.licensePlateYearText);

        // set font
        Typeface licensePlateFont = Typeface.createFromAsset(mainMenuActivity.getAssets(),  "fonts/LicensePlate.ttf");
        licensePlateText.setTypeface(licensePlateFont);

        // set license plate
        String month;
        String year = "00";
        if (mainMenuActivity.responseArray[arrayIndex].stnk_month < 10) {
            month = 0 + String.valueOf(mainMenuActivity.responseArray[arrayIndex].stnk_month);
        } else {
            month = String.valueOf(mainMenuActivity.responseArray[arrayIndex].stnk_month);
        }
        try {
            year = String.valueOf(mainMenuActivity.responseArray[arrayIndex].stnk_year).substring(2, 4);
        } catch (Exception e) {

        }

        licensePlateText.setText(mainMenuActivity.responseArray[arrayIndex].license_plate);
        licensePlateMonthText.setText(month);
        licensePlateYearText.setText(year);

        // Initializing list view with the custom adapter
        ArrayList <Item> itemList = new ArrayList<>();

        ItemArrayAdapter itemArrayAdapter = new ItemArrayAdapter(R.layout.vehicle_info_item_view, itemList);
        recyclerView = getView().findViewById(R.id.recycler_view_vehicle_info);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(itemArrayAdapter);

        // format stnk date
        SimpleDateFormat inputDate = new SimpleDateFormat("MMyy");
        SimpleDateFormat outputDate = new SimpleDateFormat("MMM yyyy");


        String stnk_expiry = month + year;

        //parse date
        String stnk_exp = "";
        try {
            stnk_exp = outputDate.format(inputDate.parse(stnk_expiry));
        } catch (ParseException e) {
        }

        // Populating list items
        itemList.add(new Item(getString(R.string.brand), mainMenuActivity.responseArray[arrayIndex].brand));
        itemList.add(new Item(getString(R.string.type), mainMenuActivity.responseArray[arrayIndex].type));
        itemList.add(new Item(getString(R.string.category), mainMenuActivity.responseArray[arrayIndex].category));
        itemList.add(new Item(getString(R.string.make_year), String.valueOf(mainMenuActivity.responseArray[arrayIndex].make_year)));
        itemList.add(new Item(getString(R.string.odometer), String.valueOf(mainMenuActivity.responseArray[arrayIndex].odometer) + " km"));
        itemList.add(new Item(getString(R.string.gas_type), mainMenuActivity.responseArray[arrayIndex].gas_type_name));
        itemList.add(new Item(getString(R.string.engine_displacement), String.valueOf(mainMenuActivity.responseArray[arrayIndex].engine_displacement) + " cc"));
        itemList.add(new Item(getString(R.string.license_plate), mainMenuActivity.responseArray[arrayIndex].license_plate));
        itemList.add(new Item(getString(R.string.stnk_expiry), stnk_exp));
    }

    public class Item {

        String title;
        String item;

        public Item(String title, String item) {
            this.title = title;
            this.item = item;
        }
    }

    public class ItemArrayAdapter extends RecyclerView.Adapter<ItemArrayAdapter.ViewHolder> {

        //All methods in this adapter are required for a bare minimum recyclerview adapter
        private int listItemLayout;
        private ArrayList<Item> itemList;
        // Constructor of the class
        public ItemArrayAdapter(int layoutId, ArrayList<Item> itemList) {
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
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(listItemLayout, parent, false);
            ViewHolder myViewHolder = new ViewHolder(view);
            return myViewHolder;
        }

        // load data in each row element
        @Override
        public void onBindViewHolder(final ViewHolder holder, final int listPosition) {
            TextView item = holder.item;
            TextView title = holder.title;
            title.setText(itemList.get(listPosition).title);
            item.setText(itemList.get(listPosition).item);
        }

        // Static inner class to initialize the views of rows
        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public TextView title;
            public TextView item;
            public ViewHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);
                title = itemView.findViewById(R.id.title);
                item = itemView.findViewById(R.id.item);
            }
            @Override
            public void onClick(View view) {
                Log.d("onclick", "onClick " + getLayoutPosition() + " " + item.getText());
            }
        }
    }

}
