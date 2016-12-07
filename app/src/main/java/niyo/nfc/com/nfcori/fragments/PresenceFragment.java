package niyo.nfc.com.nfcori.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import niyo.nfc.com.nfcori.Main2Activity;
import niyo.nfc.com.nfcori.PresenceStateListener;
import niyo.nfc.com.nfcori.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PresenceFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PresenceFragment extends Fragment {
    public static final String LOG_TAG = PresenceFragment.class.getSimpleName();
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private Integer mParam1;

    private OnFragmentInteractionListener mListener;

    public PresenceFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment PresenceFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PresenceFragment newInstance(int param1) {
        PresenceFragment fragment = new PresenceFragment();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_presence, container, false);

        Main2Activity act = (Main2Activity)getActivity();
        Log.d(LOG_TAG, "tallLampState is: "+act.tallLampState);

        updateViews(view, act.oriState, act.oriLastStateTime, act.yifatState, act.yifatLastStateTime, act.lastUpdateTime);

        act.registerForPresenceChange(new PresenceStateListener() {
            @Override
            public void onChange(Boolean oriState,
                                 String oriSince,
                                 Boolean yifatState,
                                 String yifatSince,
                                 String lastUpdateTime) {
                updateViews(view, oriState, oriSince, yifatState, yifatSince, lastUpdateTime);
            }
        });

        return view;
    }

    private void updateViews(View view,
                             Boolean oriState,
                             String oriSince,
                             Boolean yifatState,
                             String yifatSince,
                             String lastUpdate) {
        TextView oriText = (TextView)view.findViewById(R.id.oriState);
        setTextView(oriText, oriState);

        TextView oriSinceView = (TextView)view.findViewById(R.id.oriSince);
        oriSinceView.setText("Since "+oriSince);

        TextView yifatText = (TextView)view.findViewById(R.id.yifatState);
        setTextView(yifatText, yifatState);
        TextView yifatSinceView = (TextView)view.findViewById(R.id.yifatSince);
        yifatSinceView.setText("Since "+yifatSince);

        if (lastUpdate != null) {
            TextView lastUpdateView = (TextView)view.findViewById(R.id.lastUpdate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(Long.parseLong(lastUpdate));

            SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy kk:mm:ss");
            SimpleDateFormat.getDateInstance();
            String lastUpdateStr = sdf.format(calendar.getTime());
            lastUpdateView.setText("Last update: "+lastUpdateStr);
        }

    }

    private void setTextView(TextView view, Boolean state) {
        if (state == null) return;
        if (state) {
            view.setText("Home");
            view.setTextColor(getResources().getColor(R.color.white));
        }
        else {
            view.setText("Away");
            view.setTextColor(getResources().getColor(R.color.opaque_red));
        }
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
