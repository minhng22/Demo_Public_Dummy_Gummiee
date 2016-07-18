package game.minh.administrator.dummy;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.facebook.CallbackManager;
import com.facebook.appevents.AppEventsLogger;

public class logInWithFB extends Activity {
    //inni. var.
    ImageButton startGame, soundBtn, instrucBtn, backBtn;
    public static CallbackManager callbackmanager;
    SharedPreferences sharedPref;
    boolean soundOn= true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //initialize FB sdk
        setContentView(R.layout.mode_choice_layout);
        // hide the status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        soundOn = sharedPref.getBoolean(getResources().getString(R.string.sound),false);

        //declare var.
        startGame = (ImageButton) findViewById (R.id.startGame);
        startGame.setOnClickListener(startGameListener);

        soundBtn = (ImageButton) findViewById (R.id.soundBtn);
        if(soundOn) {soundBtn.setBackgroundResource(R.drawable.sound_on);}
        else{soundBtn.setBackgroundResource(R.drawable.sound_off);}

        soundBtn.setOnClickListener(soundModeListener);

        instrucBtn = (ImageButton) findViewById (R.id.instrucButton);
        instrucBtn.setOnClickListener(instrucModeListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //AppEventsLogger.deactivateApp(this);
    }

    public View.OnClickListener startGameListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            Intent singleMode = new Intent(getApplicationContext(), picturePicking.class);
            startActivity(singleMode);
        }
    };

    public View.OnClickListener instrucModeListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            setContentView(R.layout.instruction_board_show);

            backBtn= (ImageButton) findViewById(R.id.goBackBtn);
            backBtn.setOnClickListener(backListener);
        }
    };

    public View.OnClickListener backListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            setContentView(R.layout.mode_choice_layout);
            // hide the status bar
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            soundOn = sharedPref.getBoolean(getResources().getString(R.string.sound),false);

            //declare var.
            startGame = (ImageButton) findViewById (R.id.startGame);
            startGame.setOnClickListener(startGameListener);

            soundBtn = (ImageButton) findViewById (R.id.soundBtn);
            if(soundOn) {soundBtn.setBackgroundResource(R.drawable.sound_on);}
            else{soundBtn.setBackgroundResource(R.drawable.sound_off);}

            soundBtn.setOnClickListener(soundModeListener);

            instrucBtn = (ImageButton) findViewById (R.id.instrucButton);
            instrucBtn.setOnClickListener(instrucModeListener);
        }
    };


    public View.OnClickListener soundModeListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if (soundOn) {
                soundOn= false;
                soundBtn.setBackgroundResource(R.drawable.sound_off);
            }
            else {
                soundOn = true;
                soundBtn.setBackgroundResource(R.drawable.sound_on);
            }

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(getResources().getString(R.string.sound), soundOn);
            editor.commit();
        }
    };
    //don't ever try to miz this with any onActivityResult() class it won't return ANY data
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackmanager.onActivityResult(requestCode, resultCode, data);
    }
}
/* Note:
You can use this class to get FB profile pic as bitmap without AsyncTask required

public Bitmap getFacebookProfilePicture(String userID) throws IOException {
        imageURL = new URL("https://graph.facebook.com/" + userID + "/picture?type=large");
        HttpURLConnection urlConnection = (HttpURLConnection) imageURL.openConnection();
        try {
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            ppView = BitmapFactory.decodeStream(in);
        }
        finally {
            urlConnection.disconnect();
        }
        return getRoundedBitmap(ppView);
    }

or use Picasso:
Picasso.with(getApplicationContext()).
                    load("https://graph.facebook.com/" + profile.getId() + "/picture?type=large")
                    .into(profilePictureView);
 */