package jp.co.isp21.sample.receipt.util;

import android.graphics.Bitmap;
import android.graphics.Matrix;

public class BitmapOperation
{
    private static int[] mPixels = null;
	static public int[] getPixels(Bitmap src, float rotate)
	{
		int bmpW = src.getWidth();
		int bmpH = src.getHeight();

		float rot = 90.0f;
		Matrix matrix = new Matrix();
		matrix.postRotate(rot);
		Bitmap bitmap = Bitmap.createBitmap(src, 0, 0, bmpW, bmpH, matrix, true);

		bmpW = bitmap.getWidth();
		bmpH = bitmap.getHeight();
		
		if( mPixels == null ){
			mPixels = new int[bmpW*bmpH];
		}
		
		bitmap.getPixels(mPixels, 0, bmpW, 0, 0, bmpW, bmpH);
		bitmap.recycle();
		bitmap = null;
		return mPixels;
	}
}
