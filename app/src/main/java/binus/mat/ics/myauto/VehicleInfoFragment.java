package binus.mat.ics.myauto;


import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;


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

        // assign view
        licensePlateText = getView().findViewById(R.id.licensePlateText);
        licensePlateMonthText = getView().findViewById(R.id.licensePlateMonthText);
        licensePlateYearText = getView().findViewById(R.id.licensePlateYearText);

        // set font
        Typeface licensePlateFont = Typeface.createFromAsset(mainMenuActivity.getAssets(),  "fonts/LicensePlate.ttf");
        licensePlateText.setTypeface(licensePlateFont);

        // set license plate
        String month = "";
        String year = "";
        if (mainMenuActivity.responseArray[arrayIndex].stnk_month < 10) {
            month = 0 + String.valueOf(mainMenuActivity.responseArray[arrayIndex].stnk_month);
        } else {
            month = String.valueOf(mainMenuActivity.responseArray[arrayIndex].stnk_month);
        }
        year += String.valueOf(mainMenuActivity.responseArray[arrayIndex].stnk_year).substring(2,4);

        licensePlateText.setText(mainMenuActivity.responseArray[arrayIndex].license_plate);
        licensePlateMonthText.setText(month);
        licensePlateYearText.setText(year);
    }

}
