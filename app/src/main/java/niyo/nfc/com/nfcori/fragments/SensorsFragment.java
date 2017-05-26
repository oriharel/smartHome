package niyo.nfc.com.nfcori.fragments;

import android.content.Context;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import niyo.nfc.com.nfcori.Main2Activity;
import niyo.nfc.com.nfcori.R;
import niyo.nfc.com.nfcori.SensorsStateListener;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link SensorsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SensorsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static final String NAME = "Sensors";

    public static final String LOG_TAG = SensorsFragment.class.getSimpleName();

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public SensorsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment SensorsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SensorsFragment newInstance(int param1) {
        SensorsFragment fragment = new SensorsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sensors, container, false);
    }
    @Override
    public void onResume() {
        super.onResume();

        Log.d(LOG_TAG, "onResume started");

        final Main2Activity act = (Main2Activity)getActivity();

        final View view = getView();

        updateViews(view, act.doorStatus, act.doorStatusTime, act.ginaStatus, act.ginaStatusTime);

        act.registerForSensorsChange(new SensorsStateListener() {
            @Override
            public void onChange(Boolean doorStatus, Long doorTime, Boolean ginaStatus, Long ginaTime) {
                updateViews(view, doorStatus, doorTime, ginaStatus, ginaTime);
            }
        });
    }

    private void updateViews(View view,
                             Boolean doorStatus,
                             Long doorTime,
                             Boolean ginaStatus,
                             Long ginaTime) {
        Log.d(LOG_TAG, "updateViews started with doorStatus: "+doorStatus);

        ImageView doorStatusImage = (ImageView) view.findViewById(R.id.doorStatusImagae);
        TextView doorStatusText = (TextView) view.findViewById(R.id.doorStatusText);
        TextView doorStatusTime = (TextView) view.findViewById(R.id.doorStatusTime);
        ImageView ginaStatusImage = (ImageView) view.findViewById(R.id.ginaStatusImage);
        TextView ginaStatusText = (TextView) view.findViewById(R.id.ginaStatusText);
        TextView ginaStatusTime = (TextView) view.findViewById(R.id.ginaStatusTime);

        if (doorStatus) {
            doorStatusImage.setImageResource(R.drawable.closed_filled_rectangular_door);
            doorStatusText.setText(R.string.doorClosed);

        }
        else {
            doorStatusImage.setImageResource(R.drawable.open_door_entrance);
            doorStatusText.setText(R.string.doorOpened);
        }

        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(new Date(doorTime));

        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy kk:mm:ss");
        SimpleDateFormat.getDateInstance();
        String lastUpdateStrDoor = sdf.format(cal.getTime());
        doorStatusTime.setText(lastUpdateStrDoor);

        if (ginaStatus) {
            ginaStatusImage.setImageResource(R.drawable.closed_doors_with_windows);
            ginaStatusText.setText(R.string.ginaClosed);

        }
        else {
            ginaStatusImage.setImageResource(R.drawable.opened_window_door_of_glasses);
            ginaStatusText.setText(R.string.ginaOpened);
        }

        cal = java.util.Calendar.getInstance();
        cal.setTime(new Date(ginaTime));
        String lastUpdateStrGina = sdf.format(cal.getTime());
        ginaStatusTime.setText(lastUpdateStrGina);

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
}
