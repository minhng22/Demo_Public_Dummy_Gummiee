package game.minh.administrator.dummy;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

public class GameMain extends Activity {
    private AdView adView;
    private String TAG = "Tracking";
    private AdRequest request;
    private GameView myWhackAMoleView;
    private DisplayMetrics metrics = new DisplayMetrics();
    int screenW, screenH;
    RelativeLayout mainLayout; // Create a RelativeLayout as the main layout and add the gameView.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        screenW = metrics.widthPixels;
        screenH = metrics.heightPixels;
        Log.e(TAG, "screen W in GM: "+String.valueOf(screenW));
        Log.e(TAG, "screen H in GM: "+String.valueOf(screenH));

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getResources().getString(R.string.screenW), String.valueOf(screenW));
        editor.putString(getResources().getString(R.string.screenH), String.valueOf(screenH));
        editor.commit();

        myWhackAMoleView = new GameView(this);
        mainLayout =new RelativeLayout(this);

        // Create and load the AdView.
        adView = new AdView(this);
        adView.setAdUnitId(getApplication().getResources().getString(R.string.banner_ad_unit_id));
        adView.setAdSize(com.google.android.gms.ads.AdSize.BANNER);

        request= new AdRequest.Builder().addTestDevice(getApplication().getResources().getString(R.string.device_unit_id)).build();

        adView.loadAd(request);

        // Add adView to the bottom of the screen.
        RelativeLayout.LayoutParams adParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        adParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        adParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                adView.setVisibility(View.GONE);
                adView.setVisibility(View.VISIBLE);
            }
        });

        if(adView.getVisibility() == View.VISIBLE ){
            mainLayout.addView(adView, adParams);
            // Set the RelativeLayout as the main layout.
            setContentView(mainLayout);
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "Destroying...");
        if (adView != null) {adView.destroy();}

        super.onDestroy();
    }

    @Override
    protected void onPause() {
        if (myWhackAMoleView != null){
            myWhackAMoleView.surfaceDestroyed(myWhackAMoleView.getSurfaceHolder());// pause game when Activity pauses
            myWhackAMoleView.resetValue();
        }
        else{Log.e(TAG, "onPause(). Null game view");}

        if (adView != null) {adView.pause();}
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "Stopping...");
        if (adView != null) {adView.pause();}
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Resume...");

        myWhackAMoleView = new GameView(this);
        mainLayout = new RelativeLayout(this);
        mainLayout.addView(myWhackAMoleView);

        adView.resume();
        adView.setVisibility(View.VISIBLE);
        setContentView(mainLayout);
    }
}