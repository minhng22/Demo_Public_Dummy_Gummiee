package game.minh.administrator.dummy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.CallbackManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class reSultBoard extends Activity {
    ImageButton play_again, new_game, exit_button, high_score;
    Bitmap background, sharedBG, RIPGuy, toSharePic;
    TextView scoreView, highscoreView;
    ImageView backGroundView;
    String link,userID, tempPicA;
    int score, highSCore, RIPGuySize;
    float screenWidth, screenHeith, drawScaleW, drawScaleH;
    final float RIPGuySizeIni = 138, defWid = 1280, defHei= 720;
    File f;
    Boolean newHS= false;
    Typeface font;
    private String TAG = "Log Tracking: ";
    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result_board);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        callbackManager = CallbackManager.Factory.create();
        screenWidth = getWidthScreen(getApplicationContext());
        screenHeith = getHeightScreen(getApplicationContext());
        drawScaleW = screenWidth / defWid;
        drawScaleH = screenHeith / defHei;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getBaseContext());

        RIPGuySize = (int)(RIPGuySizeIni * drawScaleW );

        font = Typeface.createFromAsset(getAssets(), "Sharrpe_Gothik.ttf");

        link = sharedPref.getString(getResources().getString(R.string.Cropped_Photo), " ");
        userID = sharedPref.getString(getResources().getString(R.string.User_ID), " ");
        newHS = sharedPref.getBoolean(getResources().getString(R.string.NewHighScore), false);
        score = Integer.parseInt(sharedPref.getString(getResources().getString(R.string.Score), "null"));
        highSCore =Integer.parseInt(sharedPref.getString(getResources().getString(R.string.HighScore), "0"));

        f = new File(link);
        scoreView = (TextView) findViewById(R.id.score);

        backGroundView = (ImageView) findViewById(R.id.resultPage);
        highscoreView = (TextView) findViewById(R.id.high_score);

        RIPGuy = Bitmap.createScaledBitmap(getCircleBitmap(BitmapFactory.decodeFile(link)), RIPGuySize, RIPGuySize, true);
        sharedBG = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.share_result);
        background = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.result_page);

        sharedBG = Bitmap.createScaledBitmap(sharedBG,(int) screenWidth, (int) (screenHeith), true);
        background = Bitmap.createScaledBitmap(background,(int)(screenWidth), (int)(screenHeith),true);

        if (background != null && sharedBG != null) {
            background = onDraw(background);
            sharedBG = onDrawSB(sharedBG);
            backGroundView.setImageBitmap(background);}
        else Log.e("Tag", "Null bg");

        scoreView.setTypeface(font);
        highscoreView.setTypeface(font);

        toSharePic = getSharedPicture(sharedBG,String.valueOf(score));

        play_again = (ImageButton) findViewById(R.id.play_again);
        new_game = (ImageButton) findViewById(R.id.newGame);
        exit_button = (ImageButton) findViewById(R.id.exitButton);
        high_score = (ImageButton) findViewById(R.id.high_score_board);

        play_again.setOnClickListener(playAgain);
        new_game.setOnClickListener(newGame);
        exit_button.setOnClickListener(exit);
        high_score.setOnClickListener(shareScore);

        if (newHS){
            setContentView(R.layout.result_board_new_hs);

            play_again = (ImageButton) findViewById(R.id.play_again_hs);
            new_game = (ImageButton) findViewById(R.id.newGame_hs);
            exit_button = (ImageButton) findViewById(R.id.exitButton_hs);
            high_score = (ImageButton) findViewById(R.id.high_score_board_hs);
            scoreView = (TextView) findViewById(R.id.score_hs);
            backGroundView = (ImageView) findViewById(R.id.resultPage_hs);
            scoreView.setText(String.valueOf(highSCore));

            sharedBG = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.share_high_score);
            background = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.high_score);

            sharedBG = Bitmap.createScaledBitmap(sharedBG,(int) screenWidth, (int) (screenHeith), true);
            background = Bitmap.createScaledBitmap(background,(int)(screenWidth), (int)(screenHeith),true);

            if (background != null) {
                background = onDraw(background);
                sharedBG = onDrawSB(sharedBG);
                backGroundView.setImageBitmap(background);}
            else Log.e("Tag", "Null bg");

            scoreView.setTypeface(font);
            highscoreView.setTypeface(font);

            toSharePic = getSharedPicture(sharedBG, String.valueOf(highSCore));
            //toSharePic = Bitmap.createBitmap(takeScreenShot());

            play_again.setOnClickListener(playAgain);
            new_game.setOnClickListener(newGame);
            exit_button.setOnClickListener(exit);
            high_score.setOnClickListener(shareScore);
        }
        else{
            highscoreView.setText("High score - " + String.valueOf(highSCore));
            scoreView.setText(String.valueOf(score));
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public View.OnClickListener playAgain = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            Intent playAgain = new Intent(getApplication().getBaseContext(), GameMain.class);
            startActivity(playAgain);
        }
    };

    public View.OnClickListener shareScore = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("image/png");

            // Make sure you put example png image named myImage.png in your
            // directory
            Uri uri = Uri.fromFile(encodeBitmapToFile(toSharePic));
            share.putExtra(Intent.EXTRA_STREAM, uri);

            startActivity(Intent.createChooser(share, "Share Image!"));

        }
    };

    public File encodeBitmapToFile (Bitmap bitmap){
        Calendar c = Calendar.getInstance();

        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        tempPicA = path.getAbsolutePath()+ c.getTimeInMillis()+ ".png";
        File imageFile = new File(tempPicA);
        FileOutputStream fileOutPutStream = null;
        try {
            fileOutPutStream = new FileOutputStream(imageFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, fileOutPutStream);

        try {
            fileOutPutStream.flush();
            fileOutPutStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return imageFile;
    }

    private static DisplayMetrics getDisplayMetrics(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics;
    }

    public static int getWidthScreen(Context context) {
        return getDisplayMetrics(context).widthPixels;
    }

    public static int getHeightScreen(Context context) {
        return getDisplayMetrics(context).heightPixels;
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "Stopping...");
        if (background != null) background.recycle();
        if (RIPGuy != null) RIPGuy.recycle();
        if (sharedBG != null) sharedBG.recycle();
        if (toSharePic != null) toSharePic.recycle();
        super.onStop();
    }

    public Bitmap decodeBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 2;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {inSampleSize *= 2;}
        }
        return inSampleSize;
    }

    public View.OnClickListener newGame = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if (f.exists()) f.delete();
            Intent newGame = new Intent(getApplication().getBaseContext(), logInWithFB.class);
            startActivity(newGame);
        }
    };

    // quit the game
    public View.OnClickListener exit = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    };

    public Bitmap getSharedPicture(Bitmap bitmap, String sharedText) {
        Resources resources = getApplicationContext().getResources();
        float scale = resources.getDisplayMetrics().density;

        android.graphics.Bitmap.Config bitmapConfig = bitmap.getConfig();
        // set default bitmap config if none
        if(bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }
        // resource bitmaps are imutable,
        // so we need to convert it to mutable one
        bitmap = bitmap.copy(bitmapConfig, true);

        Canvas canvas = new Canvas(bitmap);
        // new antialised Paint
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // text color - #3D3D3D
        paint.setColor(Color.BLACK);
        // text size in pixels
        paint.setTextSize((int) (40 * scale));
        // text shadow
        paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);

        // draw text to the Canvas center
        Rect bounds = new Rect();
        paint.getTextBounds(sharedText, 0, sharedText.length(), bounds);
        paint.setTypeface(font);

        int x = (int) (bitmap.getWidth() *593/defWid);
        int y = (int) (bitmap.getHeight() * 544/ defHei);

        canvas.drawText(sharedText, x, y, paint);

        return bitmap;
    }

    protected Bitmap onDraw(Bitmap bitmap)
    {
        RIPGuy = Bitmap.createScaledBitmap(RIPGuy,(int)(bitmap.getWidth() * RIPGuySizeIni/defWid),(int)(bitmap.getWidth() * RIPGuySizeIni/defWid),true);
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas newCanvas = new Canvas(output);
        newCanvas.drawColor(Color.WHITE);

        Paint paint = new Paint();
        newCanvas.drawBitmap(bitmap, 0, 0, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
        newCanvas.drawBitmap(RIPGuy, (float)(bitmap.getWidth() * 560.5 /defWid), (float)(bitmap.getHeight() * 295.5 / defHei), paint);

        return output;
    }

    protected Bitmap onDrawSB(Bitmap bitmap)
    {
        RIPGuy = Bitmap.createScaledBitmap(RIPGuy,(int)(bitmap.getWidth() * RIPGuySizeIni/defWid),(int)(bitmap.getWidth() * RIPGuySizeIni/defWid),true);
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas newCanvas = new Canvas(output);
        newCanvas.drawColor(Color.WHITE);

        Paint paint = new Paint();
        newCanvas.drawBitmap(bitmap, 0, 0, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
        newCanvas.drawBitmap(RIPGuy, (float)(bitmap.getWidth() * 561 /defWid), (float)(bitmap.getHeight() * 345 / defHei), paint);

        return output;
    }

    public Bitmap takeScreenShot(){
        View rootView = getWindow().getDecorView().getRootView();
        rootView.setDrawingCacheEnabled(true);
        return rootView.getDrawingCache();
    }

    private Bitmap getCircleBitmap(Bitmap bitmap) {
        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = Color.WHITE;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        bitmap.recycle();

        return output;
    }
}
