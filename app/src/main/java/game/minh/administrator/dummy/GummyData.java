package game.minh.administrator.dummy;

import android.graphics.Bitmap;

public class GummyData {
    //ini. var
    private float moleX =0;
    private float moleY =0;
    private Bitmap normalMole, bonusMole, BombMole;
    // hit the normal mole +1 point, bonusMole +2 point, bombMole get you Game Over.

    public GummyData(){moleX = 0; moleY =0;}

    public void setGummyX(float moleX){this.moleX= moleX;}
    public float getGummyX(){return moleX;}

    public void setGumY(float moleY){this.moleY= moleY;}
    public float getGumY(){return moleY;}

    public void setnormalGummy(Bitmap normalMole){this.normalMole = normalMole;}
    public Bitmap getnormalGummy(){return normalMole;}

    public void setbonusGummy(Bitmap bonusMole){this.bonusMole = bonusMole;}
    public Bitmap getbonusGummy(){return bonusMole;}

    public void setBombGummy(Bitmap BombMole){this.BombMole = BombMole;}
    public Bitmap getBombGummy(){return BombMole;}

}
