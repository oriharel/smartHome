package niyo.nfc.com.nfcori.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import niyo.nfc.com.nfcori.LampStateListener;
import niyo.nfc.com.nfcori.Main2Activity;
import niyo.nfc.com.nfcori.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LightsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LightsFragment extends Fragment {
    public static final String LOG_TAG = LightsFragment.class.getSimpleName();
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private int mParam1;

    private OnFragmentInteractionListener mListener;

    public LightsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment LightsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LightsFragment newInstance(int param1) {
        LightsFragment fragment = new LightsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getInt(ARG_PARAM1);
        }


    }

    private void setupClickListeners(View view) {

        final Main2Activity act = (Main2Activity)getActivity();

        Button onButton = (Button) view.findViewById(R.id.allOn);
        onButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                act.turnTheLights("on");
            }
        });

        Button offButton = (Button) view.findViewById(R.id.allOff);
        offButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                act.turnTheLights("off");
            }
        });

        final ImageView tallBulb = (ImageView) view.findViewById(R.id.tallBulb);
        tallBulb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                act.turnSingleLight("tallLamp", tallBulb, "Tall Lamp");
            }
        });

        final ImageView windowBulb = (ImageView) view.findViewById(R.id.windowBulb);
        windowBulb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                act.turnSingleLight("windowLamp", windowBulb, "Window Lamp");
            }
        });

        final ImageView sofaBulb = (ImageView) view.findViewById(R.id.sofaBulb);
        sofaBulb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                act.turnSingleLight("sofaLamp", sofaBulb, "Sofa Lamp");
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        final View view = inflater.inflate(R.layout.fragment_lights, container, false);

        Main2Activity act = (Main2Activity)getActivity();
        Log.d(LOG_TAG, "tallLampState is: "+act.tallLampState);

        updateViews(view, act.tallLampState, act.sofaLampState, act.windowLampState);
        setupClickListeners(view);

        act.registerForLampsStateChange(new LampStateListener() {
            @Override
            public void onChange(Boolean tallState, Boolean sofaState, Boolean windowState) {
                updateViews(view, tallState, sofaState, windowState);
            }
        });

        return view;
    }

    void updateViews(View view, Boolean tallState, Boolean sofaState, Boolean windowState) {
        ImageView tallImage = (ImageView)view.findViewById(R.id.tallBulb);
        tallImage.setImageResource(tallState ? R.drawable.on_bulb : R.drawable.off_bulb);
        tallImage.setTag(tallState ? "on" : "off");

        ImageView sofaImage = (ImageView)view.findViewById(R.id.sofaBulb);
        sofaImage.setImageResource(sofaState ? R.drawable.on_bulb : R.drawable.off_bulb);
        sofaImage.setTag(sofaState ? "on" : "off");

        ImageView windowImage = (ImageView)view.findViewById(R.id.windowBulb);
        windowImage.setImageResource(windowState ? R.drawable.on_bulb : R.drawable.off_bulb);
        windowImage.setTag(windowState ? "on" : "off");
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

}
