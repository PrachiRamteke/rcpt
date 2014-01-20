package jp.co.isp21.sample.receipt.activity.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class CameraOverlayView extends View {
	int[][] linePos = { {0, 0, 0, 0},
						{0, 0, 0, 0},
						{0, 0, 0, 0},
						{0, 0, 0, 0}
	};
	Paint paint = null;
	boolean isInit = false;
	int i;

	public CameraOverlayView(Context context) {
		super(context);

		paint = new Paint();
        paint.setStrokeWidth(2.0f);
        paint.setAntiAlias(true);
        paint.setARGB(255, 255, 153, 0);
	}

	public CameraOverlayView(Context context, AttributeSet attrs) {
		super(context, attrs);
		paint = new Paint();
        paint.setStrokeWidth(2.0f);
        paint.setAntiAlias(true);
        paint.setARGB(255, 255, 153, 0);
	}

	public CameraOverlayView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		paint = new Paint();
        paint.setStrokeWidth(2.0f);
        paint.setAntiAlias(true);
        paint.setARGB(255, 255, 153, 0);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

        float previewWidth = (float)getWidth();
        float previewHeight = (float)getHeight();

        canvas.drawLine(0, (previewHeight/5), previewWidth, (previewHeight/5), paint);
        canvas.drawLine(0, (previewHeight/5)*2, previewWidth, (previewHeight/5)*2, paint);
		canvas.drawLine(0, (previewHeight/5)*3, previewWidth, (previewHeight/5)*3, paint);
        canvas.drawLine(0, (previewHeight/5)*4, previewWidth, (previewHeight/5)*4, paint);
	}
}
