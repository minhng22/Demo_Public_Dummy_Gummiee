package game.minh.administrator.dummy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

public class GameView extends SurfaceView implements SurfaceHolder.Callback{
    private GummyThread thread;
    private Context myContext;
    private SurfaceHolder mySurfaceHolder;
    private Bitmap backgroundImg, gummyInPlay, foreground, splash, normalGummy,bombGummy, bonusGummy;
    private int whackSound, missSound, starSound, fingerX, fingerY, disBetweenGummy, gumXStart, gumYStart;
    private int gummyKindIndex, activeGummy, gummyWhacked,highscore, whackBomb, number, numberPutIn,numGummy =4, module3 =0;
    private int[] arrayOfPopUpLocation = new int[3], arrayOfGummyKind = new int[3];
    private float drawScaleW, drawScaleH, scaleWByBG, scaleHByBG, bgOrWid, bgOrHei, screenW, screenH;
    private boolean running, justTouchGummy, gameOver, gumSinking, gummyRising, soundOn, onTitle, gameGoing, newHS;
    private final float defWid = 1280, defHei= 720, finalScale = 0.9f;
    private float gumMRIncrease, gumMRIncrease1, gumMRIncrease2, gumMRIncrease3;
    private float gumAccIncrease, gumAccIncrease1,gumAccIncrease2, gumAccIncrease3;

    private float gumMoveRate = 13f, atGroundGummyMovRate = 13f, gumAccRate = 0.3f;
    private Paint blackPaint;
    private SoundPool sounds;
    private GummyData gummyData = new GummyData();
    private String TAG = "Log Tracking: ", link ="";
    SharedPreferences sharedPref;

    public GameView(Context context) {
        //super(context, attrs);
        super(context);
        //ini. var
        myContext = context;
        gameGoing= newHS= running= justTouchGummy = gameOver= gumSinking =  false;
        gummyInPlay = foreground = splash =null;
        gummyRising = onTitle= true;
        activeGummy = gummyWhacked = 0;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        link = sharedPref.getString(getResources().getString(R.string.Cropped_Photo), " ");
        soundOn= sharedPref.getBoolean(getResources().getString(R.string.sound),false);
        highscore = Integer.parseInt(sharedPref.getString(getResources().getString(R.string.HighScore), "0"));
        gumMRIncrease= 1.032f; gumMRIncrease1= 1.07f; gumMRIncrease2= 1.12f; gumMRIncrease3= 0.9689f;
        gumAccIncrease= 1.065024f; gumAccIncrease1= 1.1449f; gumAccIncrease2= 1.2544f; gumAccIncrease3= 0.93876721f;

        //adjusting var to fit multiple screen
        if (myContext == null) Log.e(TAG, "NULL CONTEXT");
        screenH = Integer.parseInt(sharedPref.getString(getResources().getString(R.string.screenH), "0"));
        screenW =  Integer.parseInt(sharedPref.getString(getResources().getString(R.string.screenW), "0"));
        Log.e(TAG, "screen W: "+String.valueOf(screenW));
        Log.e(TAG, "screen H: "+String.valueOf(screenH));

        drawScaleW = (screenW/ defWid);
        drawScaleH = (screenH/ defHei);

        backgroundImg= BitmapFactory.decodeResource(myContext.getResources(), R.drawable.game_waiting_screen);
        bgOrWid = backgroundImg.getWidth();
        bgOrHei = backgroundImg.getHeight();
        scaleWByBG = (screenW/ bgOrWid);
        scaleHByBG = (screenH/ bgOrHei);
        Log.e(TAG, "bg W: " + String.valueOf(backgroundImg.getWidth()));
        Log.e(TAG, "bg H: " + String.valueOf(backgroundImg.getHeight()));
        //Toast.makeText(getContext(), "bg W: "+String.valueOf(backgroundImg.getWidth()), Toast.LENGTH_SHORT).show();
        backgroundImg = Bitmap.createScaledBitmap(backgroundImg,(int)(screenW),(int)(screenH),true);

        disBetweenGummy = (int) (266 * drawScaleW);
        gumXStart = (int) (167 * drawScaleW );
        gumYStart = (int) (575 * drawScaleH );
        float x = 480; float y = 720; float z = (x/y);

        Log.e(TAG, "scale by W: "+String.valueOf(scaleWByBG));
        Log.e(TAG, "scale by H: " + String.valueOf(scaleHByBG));
        Log.e(TAG, "drawScaleW: "+String.valueOf(drawScaleW));
        Log.e(TAG, "drawScaleH: " + String.valueOf(drawScaleH));
        Log.e(TAG, "Test value: " + String.valueOf(z));
        //Toast.makeText(getContext(), "screen W: "+String.valueOf(screenW), Toast.LENGTH_SHORT).show();
        //Toast.makeText(getContext(), "Y START: "+String.valueOf(575 * drawScaleH), Toast.LENGTH_SHORT).show();

        getHolder().addCallback(this);
        thread = new GummyThread(getHolder(), context, new Handler() {
            @Override
            public void handleMessage(Message m) {}
        });
        setFocusable(true);
    }

    class GummyThread extends Thread {
        public GummyThread(SurfaceHolder holder, Context context, Handler handler) {
            mySurfaceHolder = holder;
            myContext = context;
            //backgroundImg.setDensity((int) drawScaleDDef);

            sounds = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
            whackSound = sounds.load(myContext, R.raw.whack_sound, 1);
            missSound = sounds.load(myContext, R.raw.miss_sound, 1);
            starSound = sounds.load(myContext, R.raw.pickup_star,1);
            whackBomb = sounds.load(myContext, R.raw.bomb_pick,1);
        }

        @Override
        public void run() {
            while (running){
                //update game state
                //Log.e(TAG, "In RUN() ");
                Canvas c =null;
                try {
                    c = mySurfaceHolder.lockCanvas(null);
                    synchronized (mySurfaceHolder) {if (!gameOver) {
                        animateGummy(); }
                        draw(c);
                    }
                } finally {if (c != null) {mySurfaceHolder.unlockCanvasAndPost(c);}
                }
            }
        }

        private void draw(Canvas canvas) {
            try {canvas.drawBitmap(backgroundImg, 0, 0, null);} catch (Exception e) {}
            if (!onTitle) {
                canvas.drawText("Score: " + Integer.toString(gummyWhacked), 10 *drawScaleW, (blackPaint.getTextSize() + 10)* drawScaleH, blackPaint);
                if (highscore > 0){
                    canvas.drawText("High Score: " + Integer.toString(highscore), screenW- (int)(250*drawScaleW), (blackPaint.getTextSize()+10)* drawScaleH, blackPaint);}
                if (!gameOver) {
                    canvas.drawBitmap(gummyInPlay, gummyData.getGummyX(), gummyData.getGumY(), null);
                    if (justTouchGummy) {
                        canvas.drawBitmap(splash, fingerX - (splash.getWidth() / 2), fingerY - (splash.getHeight() / 2), null);
                    }
                }
                canvas.drawBitmap(foreground, 0, 0, null);
            }
        }

        boolean doTouchEvent(MotionEvent event) {
            synchronized (mySurfaceHolder) {
                int eventaction = event.getAction();
                int X = (int)event.getX();
                int Y = (int)event.getY();
                switch (eventaction ) {
                    case MotionEvent.ACTION_DOWN:
                        if (!gameOver){
                            fingerX = X;
                            fingerY = Y;
                            if (!onTitle && detectGummyContact()) {
                                if (gummyIsAFace(gummyKindIndex)) {
                                    gummyWhacked++;
                                    justTouchGummy = true;
                                    if (soundOn) {
                                        // give whacked sound when a gummyInPlay is whacked
                                        AudioManager audioManager = (AudioManager) myContext.getSystemService(Context.AUDIO_SERVICE);
                                        float volume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                                        sounds.play(whackSound, volume, volume, 1, 0, 1);
                                    }
                                    pickAcGummyandDuelMissed();
                                }
                                else if (gummyIsAStar(gummyKindIndex)) {
                                    gummyWhacked += 2;
                                    justTouchGummy = true;
                                    if (soundOn) {
                                        // give whacked sound when a gummyInPlay is whacked
                                        AudioManager audioManager = (AudioManager) myContext.getSystemService(Context.AUDIO_SERVICE);
                                        float volume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                                        sounds.play(starSound, volume, volume, 1, 0, 1);
                                    }
                                    pickAcGummyandDuelMissed();
                                }
                                else if (gummyIsABomb(gummyKindIndex)){
                                    gameOver = true;
                                    if (soundOn) {
                                        AudioManager audioManager = (AudioManager) myContext.getSystemService(Context.AUDIO_SERVICE);
                                        float volume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                                        sounds.play(whackBomb, volume, volume, 1, 0, 1);
                                    }
                                    showResultBoard();
                                }
                            }
                        }
                        else showResultBoard();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        break;

                    case MotionEvent.ACTION_UP:
                        float x = drawScaleW, y= drawScaleH;
                        Log.e(TAG, "drawScaleW in Action up: "+ String.valueOf(x));
                        Log.e(TAG, "drawScaleH in Action up: "+ String.valueOf(y));
                        //Toast.makeText(getContext(), "drawScaleW: "+String.valueOf(drawScaleW), Toast.LENGTH_SHORT).show();

                        //start the game
                        if (onTitle && !gameGoing && touchStartButton(x,y)) {
                            backgroundImg=  BitmapFactory.decodeResource(myContext.getResources(), R.drawable.background);
                            foreground = BitmapFactory.decodeResource(myContext.getResources(), R.drawable.foreground);
                            splash =  BitmapFactory.decodeResource(myContext.getResources(), R.drawable.splash);

                            normalGummy = BitmapFactory.decodeResource(myContext.getResources(), R.drawable.gummie);
                            bonusGummy = BitmapFactory.decodeResource(myContext.getResources(), R.drawable.bonus_gummy);
                            bombGummy = BitmapFactory.decodeResource(myContext.getResources(), R.drawable.bomb_gummy);
                            Log.e(TAG, String.valueOf(backgroundImg.getWidth()));

                            backgroundImg = Bitmap.createScaledBitmap(backgroundImg, (int) (backgroundImg.getWidth() * scaleWByBG), (int) (backgroundImg.getHeight() * scaleHByBG), true);
                            Log.e(TAG, "background: "+ (int) (backgroundImg.getWidth() * scaleWByBG)+" "+ (int) (backgroundImg.getHeight() * scaleHByBG));
                            foreground = Bitmap.createScaledBitmap(foreground, (int) (foreground.getWidth() * scaleWByBG), (int) (foreground.getHeight() * scaleHByBG), true);
                            Log.e(TAG, "foreground: "+ (int) (foreground.getWidth() * scaleWByBG)+" "+ (int) (foreground.getHeight() * scaleHByBG));
                            splash = Bitmap.createScaledBitmap(splash, (int) (splash.getWidth() * scaleWByBG* finalScale), (int) (splash.getHeight() * scaleHByBG* finalScale), true);

                            Log.e(TAG,"Gum width pre-scale: " +String.valueOf(normalGummy.getWidth()));
                            Log.e(TAG,"Gum width after-scale: " +String.valueOf(normalGummy.getWidth()));
                            Log.e(TAG,"BG width after-scale: " +String.valueOf(backgroundImg.getWidth()));

                            gummyData.setnormalGummy(Bitmap.createScaledBitmap(getGummy(normalGummy), (int)(normalGummy.getWidth()* scaleWByBG), (int)(normalGummy.getHeight()* scaleHByBG),true));
                            gummyData.setbonusGummy(Bitmap.createScaledBitmap(bonusGummy, (int)(bonusGummy.getWidth()* scaleWByBG), (int)(bonusGummy.getHeight()* scaleHByBG),true));
                            gummyData.setBombGummy(Bitmap.createScaledBitmap(bombGummy, (int)(bombGummy.getWidth()* scaleWByBG), (int)(bombGummy.getHeight()* scaleHByBG),true));
                            onTitle = false;
                            pickAcGummyandDuelMissed();
                            gameGoing = true;
                        }
                        justTouchGummy = false;
                        break;
                }
            }
            return true;
        }

        public void setSurfaceSize(int width, int height) {
            synchronized (mySurfaceHolder) {
                screenW = width;
                screenH = height;
                backgroundImg = Bitmap.createScaledBitmap(backgroundImg, width, height, true);

                //reimplement to change text style
                blackPaint = new Paint();
                setUpPainting(blackPaint);
            }
        }

        public void setUpPainting(Paint input) {
            input.setAntiAlias(true);
            input.setColor(Color.BLACK);
            input.setStyle(Paint.Style.FILL_AND_STROKE);
            input.setTextAlign(Paint.Align.LEFT);
            input.setTextSize(drawScaleW*30);
        }

        public void setRunning(boolean b) {
            synchronized (mySurfaceHolder){running = b;}
        }

        private void animateGummy() {
            //control the animation of gummyPoses
            //make sure gummyInPlay is genereated before running
            if(gummyInPlay != null){
                for(int i=0; i< numGummy;i++){
                    if (activeGummy == (i+1)) {
                        if(gummyData.getGumY() == gumYStart){
                            gumMoveRate = atGroundGummyMovRate;}
                        break; }
                    else continue;
                }
                //moving gummyInPlay upward and downward
                if (gummyRising) {
                    gummyData.setGumY(gummyData.getGumY() - (gumMoveRate * drawScaleH));
                    gumMoveRate -= gumAccRate;

                    if (gummyData.getGumY() < (486 * drawScaleH)) {justTouchGummy = false;}
                } else if (gumSinking) {
                    gummyData.setGumY(gummyData.getGumY() + (gumMoveRate * drawScaleH));
                    gumMoveRate += gumAccRate;
                }

                //max height
                if(gumMoveRate <= 0){
                    Log.e(TAG,"Gummy Y Max: "+ String.valueOf(gummyData.getGumY()));
                    gumMoveRate =0;
                    gummyRising = false;
                    gumSinking = true;
                }

                Log.e(TAG,"Gummy Y Start: "+String.valueOf(gumYStart));
                Log.e(TAG,"Gummy X "+String.valueOf(gummyData.getGummyX()));
                //gummyInPlay hit the ground
                if(gummyData.getGumY() > gumYStart){
                    gummyData.setGumY(gumYStart);
                    gumMoveRate = atGroundGummyMovRate;

                    if (gummyIsABomb(gummyKindIndex)) {
                        pickAcGummyandDuelMissed();
                    }
                    else {
                        gameOver = true;
                        if (soundOn) {
                            AudioManager audioManager = (AudioManager) myContext.getSystemService(Context.AUDIO_SERVICE);
                            float volume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                            if (!gummyIsABomb(gummyKindIndex))sounds.play(missSound, volume, volume, 1, 0, 1);
                        }
                        showResultBoard();
                    }
                }
            }
        }
        private void pickKindOfGummyAndScale(){
            /* if
            1 <= kind <= 4: normal
            5 <= kind <= 7: star
            else: bomb
             */
            gummyKindIndex = pickWhichKindOfGummy();

            if(gummyIsAFace(gummyKindIndex)) gummyInPlay = Bitmap.createScaledBitmap( gummyData.getnormalGummy(), (int)(gummyData.getnormalGummy().getWidth() * finalScale),  (int)(gummyData.getnormalGummy().getHeight()* finalScale), true);
            if (gummyIsAStar(gummyKindIndex)) gummyInPlay = Bitmap.createScaledBitmap( gummyData.getbonusGummy(),(int)(gummyData.getbonusGummy().getWidth() * finalScale),  (int)(gummyData.getbonusGummy().getHeight()* finalScale),true);
            if (gummyIsABomb(gummyKindIndex)) gummyInPlay = Bitmap.createScaledBitmap( gummyData.getBombGummy(), (int)(gummyData.getBombGummy().getWidth() * finalScale),  (int)(gummyData.getBombGummy().getHeight()* finalScale), true);
            //gummyInPlay.setDensity((int)drawScaleDDef);
        }

        private void changeBGInCase(){
            if(gameGoing){
                int n = gummyWhacked/ 20;
                int m = n%3;

                if (n!= 0){
                    if ( m!= module3){
                        module3 = m;
                        if(m ==0){
                            backgroundImg=  BitmapFactory.decodeResource(myContext.getResources(), R.drawable.background);
                            backgroundImg = Bitmap.createScaledBitmap(backgroundImg, (int) (backgroundImg.getWidth() * scaleWByBG), (int) (backgroundImg.getHeight() * scaleHByBG), true);
                        }
                        if(m ==1){
                            backgroundImg=  BitmapFactory.decodeResource(myContext.getResources(), R.drawable.background_sunset);
                            backgroundImg = Bitmap.createScaledBitmap(backgroundImg, (int) (backgroundImg.getWidth() * scaleWByBG), (int) (backgroundImg.getHeight() * scaleHByBG), true);
                        }
                        if(m ==2){
                            backgroundImg=  BitmapFactory.decodeResource(myContext.getResources(), R.drawable.background_night_sky);
                            backgroundImg = Bitmap.createScaledBitmap(backgroundImg, (int) (backgroundImg.getWidth() * scaleWByBG), (int) (backgroundImg.getHeight() * scaleHByBG), true);
                        }
                    }
                }
            }
        }

        private void pickAcGummyandDuelMissed() {
            //Log.e(TAG, "In Duel with Missed");
            changeBGInCase();
            activeGummy = pickWhereGummyPopUp();
            gummyRising = true;
            gumSinking = false;
            if(justTouchGummy) {
                udBasedOnScore();
            }

            //generarte new gummyInPlay data and bitmap
            pickKindOfGummyAndScale();
            int GummyX = gumXStart + disBetweenGummy * (activeGummy -1);
            gummyData.setGummyX(GummyX);
            gummyData.setGumY(gumYStart);
        }

        private void udBasedOnScore(){
            int scr = gummyWhacked % 50;
            if (0 <= scr && scr <=6){
                gumMoveRate *= gumMRIncrease;
                atGroundGummyMovRate *= gumMRIncrease;
                gumAccRate *= gumAccIncrease;
            }
            else if (6 < scr && scr <=15){
                gumMoveRate *= gumMRIncrease1;
                atGroundGummyMovRate *= gumMRIncrease1;
                gumAccRate *= gumAccIncrease1;
            }
            else if (15 < scr && scr <= 26){
                gumMoveRate *= gumMRIncrease2;
                atGroundGummyMovRate *= gumMRIncrease2;
                gumAccRate *= gumAccIncrease2;
            }
            else if (26 < scr && scr <=40){
                gumMoveRate *= gumMRIncrease3;
                atGroundGummyMovRate *= gumMRIncrease3;
                gumAccRate *= gumAccIncrease3;
            }
            else if (40 < scr && scr <50) {
                gumMoveRate *= gumMRIncrease2;
                atGroundGummyMovRate *= gumMRIncrease2;
                gumAccRate *= gumAccIncrease2;
            }
        }

        private boolean detectGummyContact() {
            Boolean contact = false;
            for (int i=0; i<5 ;i++) {
                //user touch gummyInPlay
                if (activeGummy == (i + 1)
                        && fingerX >= gummyData.getGummyX()
                        && fingerX < gummyData.getGummyX() + (int) (154 * drawScaleW)
                        && fingerY > gummyData.getGumY()
                        && fingerY < gummyData.getGumY() + (int) 154 * drawScaleH) {
                    contact = true;
                    break;
                } else
                    continue;
            }
            return contact;
        }

        private boolean touchStartButton(float x, float y){
            //float x1= 435* x, x2=844* x, y1 = 414* y,y2= 486*y;
            float x1= 435* x, x2=844* x, y1 = 340* y,y2= 412*y;

            if (fingerX >= x1&& fingerX <= x2 && fingerY >= y1 &&fingerY <= y2 && onTitle){
                Log.e(TAG,"Value x1: "+ String.valueOf(x));
                Log.e(TAG,"Value y1: "+ String.valueOf(y));
                Log.e(TAG,"finger X: "+ String.valueOf(fingerX));
                //Toast.makeText(getContext(),"finger X: "+ String.valueOf(fingerX),Toast.LENGTH_SHORT).show();
                Log.e(TAG,"finger Y: "+ String.valueOf(fingerY));
                return true;
            }
            else {return false;}
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

        //to decode large bitmap from resource
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

        public Bitmap getCircleBitmap(Bitmap bitmap) {
            final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            final Canvas canvas = new Canvas(output);

            final int color = Color.WHITE;
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, (int) (bitmap.getWidth() / 1.05), (int) (bitmap.getHeight() /1.05));
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
        public Bitmap scaleBitmap(Bitmap bitmap, int wantedWidth, int wantedHeight) {
            Bitmap output = Bitmap.createBitmap(wantedWidth, wantedHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);
            Matrix m = new Matrix();
            m.setScale((float) wantedWidth / bitmap.getWidth(), (float) wantedHeight / bitmap.getHeight());
            canvas.drawBitmap(bitmap, m, new Paint());

            return output;
        }

        public Bitmap getGummy(Bitmap bitmap){
            Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas newCanvas = new Canvas(output);

            Paint paint = new Paint();
            newCanvas.drawBitmap(bitmap, 0, 0, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
            //newCanvas.drawBitmap(Bitmap.createScaledBitmap(getCircleBitmap(BitmapFactory.decodeFile(link)), (int) (bitmap.getWidth() / 1.51), (int) (bitmap.getWidth() / 1.51), true), (float) (bitmap.getWidth() * 1.1/ 5), (float) (bitmap.getWidth() * 1.85 / 5), paint);
            /* change number for picture change in St. Patrick Day
            3.6.2016
             */
            newCanvas.drawBitmap(Bitmap.createScaledBitmap(getCircleBitmap(BitmapFactory.decodeFile(link)), (int) (bitmap.getWidth() / 1.51), (int) (bitmap.getWidth() / 1.51), true), (float) (bitmap.getWidth() / 5), (float) (bitmap.getWidth() * 3.35 / 5), paint);
            return output;
        }
    }

    private boolean gummyIsAFace(int index) {return (1 <= index && index<= 5);}

    private boolean gummyIsAStar(int index) {return (6 <= index && index<= 7);}

    private boolean gummyIsABomb(int index) {return (8 <= index && index<= 10);}

    /* bombs come after 10.
    star come out after 4
     */
    public int pickWhichKindOfGummy(){
        number = new Random().nextInt(10) +1;

        if (gummyIsAFace(number)) {numberPutIn =1;}
        else if (gummyIsAStar(number)) {numberPutIn =2;}
        else if (gummyIsABomb(number)) {numberPutIn =3;}

        if (gummyWhacked > 10) {
            if (arrayOfGummyKind[0] == 0) {arrayOfGummyKind[0] = numberPutIn;}
            else if (arrayOfGummyKind[1] == 0) {arrayOfGummyKind[1] = numberPutIn;}
            else if (arrayOfGummyKind[2] == 0) {
                if ( no3InARow(number, arrayOfGummyKind)) {arrayOfGummyKind[2] = numberPutIn;}
                else {
                    arrayOfGummyKind[2] = pickNo3InARow(arrayOfGummyKind);
                    if (arrayOfGummyKind[2] ==1) number =1;
                    else if (arrayOfGummyKind[2] ==2) number = 6;
                    else if (arrayOfGummyKind[2] ==3) number = 10;
                }
            }
            else {
                arrayOfGummyKind[0] = arrayOfGummyKind[1] = arrayOfGummyKind[2] = 0;
                arrayOfGummyKind[0] = numberPutIn;
            }
            return number;
        }
        else if (gummyWhacked > 3){
            number = new Random().nextInt(7) +1;

            if (gummyIsAFace(number)) {numberPutIn =1;}
            else if (gummyIsAStar(number)) {numberPutIn =2;}
            else if (gummyIsABomb(number)) {numberPutIn =3;}

            if (arrayOfGummyKind[0] == 0) {arrayOfGummyKind[0] = numberPutIn;}
            else if (arrayOfGummyKind[1] == 0) {arrayOfGummyKind[1] = numberPutIn;}
            else if (arrayOfGummyKind[2] == 0) {
                if ( no3InARow(number, arrayOfGummyKind)) {arrayOfGummyKind[2] = numberPutIn;}
                else {
                    arrayOfGummyKind[2] = pickNo3InARowFor2(arrayOfGummyKind);
                    if (arrayOfGummyKind[2] ==1) number =1;
                    else if (arrayOfGummyKind[2] ==2) number = 6;
                }
            }
            else {
                arrayOfGummyKind[0] = arrayOfGummyKind[1] = arrayOfGummyKind[2] = 0;
                arrayOfGummyKind[0] = numberPutIn;
            }
            return number;
        }
        else return 3; // a number that refer type of normal gummyInPlay
    }
    private int pickNo3InARow(int[] array) {
        int num = new Random().nextInt(3) +1;
        while (num == array[1]){
            num = new Random().nextInt(3) +1;
        }
        return num;
    }

    private int pickNo3InARowFor2(int[] array) {
        int num = new Random().nextInt(2) +1;
        while (num == array[1]){
            num = new Random().nextInt(2) +1;
        }
        return num;
    }

    private boolean no3InARow(int num, int[] array) {
        if (array[0] != array[1] && array[1] != num)
            return true;
        else return false;
    }

    public int pickWhereGummyPopUp(){
        int number = new Random().nextInt(numGummy) +1;

        if (arrayOfPopUpLocation[0] == 0) {
            arrayOfPopUpLocation[0] = number;
            return arrayOfPopUpLocation[0];
        }
        else if (arrayOfPopUpLocation[1] == 0) {
            arrayOfPopUpLocation[1] = number;
            return arrayOfPopUpLocation[1];
        }
        else if (arrayOfPopUpLocation[2] == 0) {
            if (no3InARow(number, arrayOfGummyKind)) {arrayOfPopUpLocation[2] = number; }
            else arrayOfPopUpLocation[2] = pickNo3InARow(arrayOfPopUpLocation);
            return arrayOfPopUpLocation[2];
        }
        else {
            arrayOfPopUpLocation[0] = arrayOfPopUpLocation[1] = arrayOfPopUpLocation[2] = 0;
            arrayOfPopUpLocation[0] = number;
            return arrayOfPopUpLocation[0];
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return thread.doTouchEvent(event);
    }

    @Override
    public void surfaceChanged(SurfaceHolder mySurfaceHolder, int format, int width, int height) {
        thread.setSurfaceSize(width, height);}

    @Override
    public void surfaceCreated(SurfaceHolder holder){
        thread.setRunning(true);
        if (thread.getState() == Thread.State.NEW ){
            thread.start();
        }
    }

    public SurfaceHolder getSurfaceHolder(){return mySurfaceHolder;}

    public void resetValue(){
        //if (whack != null) whack.recycle();
        if (backgroundImg != null) backgroundImg.recycle();
        if (foreground != null) foreground.recycle();
        if (splash != null) splash.recycle();
        if (gummyInPlay != null) gummyInPlay.recycle();
        if (normalGummy != null) normalGummy.recycle();
        if (bombGummy != null) bombGummy.recycle();
        if (bonusGummy != null) bonusGummy.recycle();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        thread.setRunning(false);
        thread.interrupt();
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
    }

    private void showResultBoard(){
        //save highScore to preference
        if (gummyWhacked >  highscore) newHS = true;

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getResources().getString(R.string.Score), String.valueOf(gummyWhacked));
        if (newHS) {editor.putString(getResources().getString(R.string.HighScore), String.valueOf(gummyWhacked));}
        editor.putBoolean(getResources().getString(R.string.NewHighScore), newHS);
        editor.commit();

        Intent resultBoard = new Intent(myContext, reSultBoard.class);
        myContext.startActivity(resultBoard);
    }
}

