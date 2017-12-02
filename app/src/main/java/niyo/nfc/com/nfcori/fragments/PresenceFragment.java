package niyo.nfc.com.nfcori.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.ComposeShader;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
import java.util.Calendar;

import niyo.nfc.com.nfcori.Main2Activity;
import niyo.nfc.com.nfcori.PresenceStateListener;
import niyo.nfc.com.nfcori.R;

import static android.R.attr.width;
import static android.support.v7.appcompat.R.attr.height;

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
    public static final String NAME = "Presence";

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

        updateViews(view,
                act.oriState,
                act.oriLastStateTime,
                act.yifatState,
                act.yifatLastStateTime,
                act.itchukState,
                act.itchukLastStateTime,
                act.lastUpdateTime
        );

        act.registerForPresenceChange(new PresenceStateListener() {
            @Override
            public void onChange(Boolean oriState,
                                 String oriSince,
                                 Boolean yifatState,
                                 String yifatSince,
                                 Boolean itchukState,
                                 String itchukSince,
                                 String lastUpdateTime) {
                updateViews(view,
                        oriState,
                        oriSince,
                        yifatState,
                        yifatSince,
                        itchukState,
                        itchukSince,
                        lastUpdateTime
                );
            }
        });

        ImageView oriImage = (ImageView)view.findViewById(R.id.oriImageView);
        ImageView yifatImage = (ImageView)view.findViewById(R.id.yifatImageView);
        ImageView itchukImage = (ImageView)view.findViewById(R.id.itchukImageView);



        oriImage.setImageBitmap(getRoundImageBitmap(R.drawable.ori));
        yifatImage.setImageBitmap(getRoundImageBitmap(R.drawable.yifat));
        itchukImage.setImageBitmap(getRoundImageBitmap(R.drawable.itchuk));


        return view;
    }

    private Bitmap getRoundImageBitmap(int resId) {
        Bitmap mbitmap = BitmapFactory.decodeResource(getResources(), resId);

        Bitmap imageRounded = Bitmap.createBitmap(mbitmap.getWidth(), mbitmap.getHeight(), mbitmap.getConfig());
        Canvas canvas = new Canvas(imageRounded);
        Paint mpaint = new Paint();
        mpaint.setAntiAlias(true);
        mpaint.setShader(new BitmapShader(mbitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        canvas.drawRoundRect((new RectF(0, 0, mbitmap.getWidth(), mbitmap.getHeight())), 750, 750, mpaint);// Round Image Corner 100 100 100 100

        return imageRounded;
    }

    class RoundImage extends Drawable {

        private final float mCornerRadius;
        private final RectF mRect = new RectF();
        private final BitmapShader mBitmapShader;
        private final Paint mPaint;
        private final int mMargin;

        RoundImage(Bitmap bitmap, float cornerRadius, int margin) {
            mCornerRadius = cornerRadius;

            mBitmapShader = new BitmapShader(bitmap,
                    Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setShader(mBitmapShader);

            mMargin = margin;

            mRect.set(margin, margin, 360, 360);
        }

//        @Override
//        protected void onBoundsChange(Rect bounds) {
//            super.onBoundsChange(bounds);
//            mRect.set(mMargin, mMargin, bounds.width() - mMargin, bounds.height() - mMargin);

//            RadialGradient vignette = new RadialGradient(
//                    mRect.centerX(), mRect.centerY() * 1.0f / 0.7f, mRect.centerX() * 1.3f,
//                    new int[] { 0, 0, 0x7f000000 }, new float[] { 0.0f, 0.7f, 1.0f },
//                    Shader.TileMode.CLAMP);
//
//            Matrix oval = new Matrix();
//            oval.setScale(1.0f, 0.7f);
//            vignette.setLocalMatrix(oval);

//            mPaint.setShader(mBitmapShader);
//        }

        @Override
        public void draw(Canvas canvas) {
            canvas.drawRoundRect(mRect, mCornerRadius, mCornerRadius, mPaint);
        }

        @Override
        public void setAlpha(int alpha) {
            mPaint.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
            mPaint.setColorFilter(colorFilter);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }

    private void updateViews(View view,
                             Boolean oriState,
                             String oriSince,
                             Boolean yifatState,
                             String yifatSince,
                             Boolean itchukState,
                             String itchukSince,
                             String lastUpdate) {
        TextView oriText = (TextView)view.findViewById(R.id.oriState);
        setTextView(oriText, oriState);

        TextView oriSinceView = (TextView)view.findViewById(R.id.oriSince);
        oriSinceView.setText("Since "+oriSince);

        TextView yifatText = (TextView)view.findViewById(R.id.yifatState);
        setTextView(yifatText, yifatState);
        TextView yifatSinceView = (TextView)view.findViewById(R.id.yifatSince);
        yifatSinceView.setText("Since "+yifatSince);

        TextView itchukText = (TextView)view.findViewById(R.id.itchukState);
        setTextView(itchukText, itchukState);
        TextView itchukSinceView = (TextView)view.findViewById(R.id.itchukSince);
        itchukSinceView.setText("Since "+itchukSince);

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
