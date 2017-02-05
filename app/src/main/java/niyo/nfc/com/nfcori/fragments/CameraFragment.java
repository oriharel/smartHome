package niyo.nfc.com.nfcori.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import niyo.nfc.com.nfcori.CameraStateListener;
import niyo.nfc.com.nfcori.GenericHttpRequestTask;
import niyo.nfc.com.nfcori.HomeStateFetchService;
import niyo.nfc.com.nfcori.Main2Activity;
import niyo.nfc.com.nfcori.R;
import niyo.nfc.com.nfcori.Utils;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CameraFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CameraFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    public static final String LOG_TAG = CameraFragment.class.getSimpleName();

    // TODO: Rename and change types of parameters
    private Integer mParam1;

    private VideoView mVideoView;
    private MediaController mController;

    private OnFragmentInteractionListener mListener;

    public CameraFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment CameraFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CameraFragment newInstance(int param1) {
        CameraFragment fragment = new CameraFragment();
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        Main2Activity act = (Main2Activity)getActivity();

        final View view = getView();

        if (view != null) {
            ImageButton camBtn = (ImageButton)view.findViewById(R.id.startCam);
            camBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String camUrl = Utils.getHomeURL()+"/cam/start?uuid=f589cad6-49bf-4d1b-9091-4ba9ef1d466b";
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(camUrl), "video/mpeg");
                    startActivity(intent);
                }
            });
        }

        updateViews(view, act.homeImage64);
        updateCamSnapshot(view, act.homeCamImage64);

        act.registerForCameraChange(new CameraStateListener() {
            @Override
            public void onChange(byte[] homeImage64) {
                updateViews(view, homeImage64);
            }
        });
    }

    private void updateViews(View view, byte[] homeImage64) {

        if (homeImage64 != null) {
            ImageView homeImageView = (ImageView)view.findViewById(R.id.livingImage);
            byte[] decodedString = Base64.decode(homeImage64, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            homeImageView.setImageBitmap(decodedByte);
        }

    }

    private void updateCamSnapshot(View view, byte[] camHomeImage64) {

        if (camHomeImage64 != null) {
            ImageButton camHomeImageView = (ImageButton)view.findViewById(R.id.startCam);
            byte[] decodedString = Base64.decode(camHomeImage64, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            camHomeImageView.setImageBitmap(decodedByte);
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
