package flo.gameserver.client;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Fynn on 01.02.2017.
 */
public class GamePadView extends View {

    Paint thumbCircle;
    float hold_x = -100;
    float hold_y = -100;
    public GamePadView(Context context, AttributeSet attributeSet) {
        super(context,attributeSet);

        this.setBackgroundColor(Color.BLACK);
        thumbCircle = new Paint();
        thumbCircle.setStyle(Paint.Style.FILL);
        thumbCircle.setColor(Color.WHITE);
    }



    @Override
    protected void dispatchDraw (Canvas canvas){	//anstatt onDraw() welches nicht updated
        if(android.os.Debug.isDebuggerConnected())
            android.os.Debug.waitForDebugger();
        super.dispatchDraw(canvas);
        canvas.drawCircle(hold_x, hold_y, 50, thumbCircle);
    }


    void setHoldCoordinates(float x, float y){
        hold_x = x;
        hold_y = y;
    }
}
