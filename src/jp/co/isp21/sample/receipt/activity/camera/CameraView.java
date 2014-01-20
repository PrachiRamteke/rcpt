package jp.co.isp21.sample.receipt.activity.camera;

import java.util.List;

import jp.co.isp21.sample.receipt.util.LogManage;
import android.content.Context;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

class CameraView extends SurfaceView implements SurfaceHolder.Callback {

    private final String TAG = "Preview";
    SurfaceHolder mHolder;
    Size mPreviewSize = null;

    CameraView(Context context) {
        super(context);
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

	public CameraView(Context context, AttributeSet attrs) {
		super(context, attrs);
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public CameraView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}


    //public void setCamera(Camera camera) {
    public void startPreview() {
   		LogManage.debugLog( "CameraViewEx::setCamera() IN");
		if (CameraManage.getInstance().isCameraNull() == false) {
            requestLayout();
			CameraManage.getInstance().setPreviewDisplay(mHolder);
		}
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
   		LogManage.debugLog( "CameraViewEx::onMeasure() IN");

        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);

	    List<Size> supportedPreviewSizes = CameraManage.getInstance().getSupportedPreviewSizes();
        if (supportedPreviewSizes != null) {
            mPreviewSize = getOptimalPreviewSize(supportedPreviewSizes, width, height);
        }
    }

/**
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
   		LogManage.debugLog( "CameraViewEx::onLayout() IN");
    }
*/

    public void surfaceCreated(SurfaceHolder holder) {
   		LogManage.debugLog("CameraViewEx::surfaceCreated() IN");
		mHolder = holder;
		CameraManage.getInstance().setPreviewDisplay(holder);
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
   		LogManage.debugLog("CameraViewEx::surfaceDestroyed() IN");
	/**
        if (camera != null) {
			LogManage.debugLog("CameraViewEx::surfaceDestroyed() stopPreview()");
            camera.stopPreview();
        }
	*/
    }


    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
   		LogManage.debugLog( "CameraViewEx::surfaceChanged() IN");
   		LogManage.debugLog( "CameraViewEx::surfaceChanged() format : " + format + " w : " + w + " h : " + h);
		mHolder = holder;

		if (CameraManage.getInstance().isCameraNull() == true) {
			return;
		}

		//camera.stopPreview();
		if (mPreviewSize != null) {
			if (CameraManage.getInstance().setPreviewSize(mPreviewSize) == false) {
				return;
			}
			requestLayout();
		}
		CameraManage.getInstance().setPreviewDisplay(mHolder);
        //camera.startPreview();
    }
}


