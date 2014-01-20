package jp.co.isp21.sample.receipt.activity.camera;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jp.co.isp21.sample.receipt.util.LogManage;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.view.SurfaceHolder;

// ----------------------------------------------------------------------

public class CameraManage {

	// 画面サイズ
	private static final int JUST_PIC_WIDTH   = 1600; // 90度回転するのでWが640
	private static final int JUST_PIC_HEIGHT  = 1200; // 90度回転するのでHが480
//	private static final int JUST_PIC_WIDTH   = 2048; // 90度回転するのでWが640
//	private static final int JUST_PIC_HEIGHT  = 1536; // 90度回転するのでHが480

	// カメラオブジェクト
    private Camera camera;

    private static byte[] previewCallBackBuff = null;

	boolean isSetAutoFocus = false;
	private boolean mbAutoFocus = false;
	private boolean mIsStartAutoFocus = false;
	private boolean mIsStart = false;

	// 解析結果用リスナ
	Camera.PictureCallback cameraListener;
	Camera.ErrorCallback mErrorCallback;

	// 自分自身のインスタンス
	static CameraManage _instance = null;

	static public CameraManage getInstance() {
		if (_instance == null) {
			previewCallBackBuff = null;
			_instance = new CameraManage();
		}
		return _instance;
	}

	/**
	 * コンストラクタ
	 */
	private CameraManage() {
	}

	/**
	 * リスナを設定する
	 */
	public void setPictureCallback(Camera.PictureCallback listener) {
		cameraListener = listener;
	}

	/**
	 * カメラオブジェクトの取得
	 * @return カメラオブジェクト
	 */
	public Camera getCamera() {
		return this.camera;
	}

	/**
	 * camera のインスタンスの状態を取得する
	 * @return camera のインスタンスの状態
	 */
	public boolean isCameraNull() {
		if (this.camera == null) {
			return true;
		}
		return false;
	}

	/**
	 * カメラでサポートしているサイズを取得する
	 * @return サイズリスト
	 */
	public List<Size> getSupportedPreviewSizes() {
        if (this.camera == null) {
			return null;
		}
		return this.camera.getParameters().getSupportedPreviewSizes();
	}

	/**
	 * カメラでサポートしているモバイルライトのリストを取得する
	 * @return　モバイルライトリスト
	 */
	public List<String> getSupportedFlashModes(){
        if (this.camera == null) {
			return null;
		}
		return this.camera.getParameters().getSupportedFlashModes();
	}

	public boolean isSupportedBrightness(){
        if (this.camera == null) {
			return false;
		}
		Camera.Parameters param = this.camera.getParameters();
        int max = param.getMaxExposureCompensation();
        int min = param.getMinExposureCompensation();

        if( max == 0 && min == 0 ){
        	return false;
        }
        return true;
	}

	/**
	 * カメラの表示を行う
	 */
	public void setPreviewDisplay(SurfaceHolder holder) {
        if (this.camera == null) {
			return;
		}
		try {
			this.camera.setPreviewDisplay(holder);
			this.camera.startPreview();
		} catch (IOException ex) {
			LogManage.errorLog("CameraManage::setPreviewDisplay() IOException", ex);
			ex.printStackTrace();
		}
	}

	/**
	 * カメラの生成
	 */
    public boolean open(Camera.ErrorCallback errorCallback, int width, int height) {
   		LogManage.debugLog("CameraManage::open() IN");

        // Open the default i.e. the first rear facing camera.
		if (this.camera == null) {
			try{
				this.camera = Camera.open();
				// フォーカス、画像サイズなどの設定
				setFocusMode();
				setPictureSize();
				setPreviewSize(width, height);
				updateCameraParametersInitialize();

				mIsStart = false;
				mIsStartAutoFocus = false;
				mbAutoFocus = false;
				// ９０度回転
				this.camera.setDisplayOrientation(90);

				// バッファの生成
				Camera.Parameters param = this.camera.getParameters();
				if( previewCallBackBuff == null ){
					int size = param.getPreviewSize().width * param.getPreviewSize().height *
							   ImageFormat.getBitsPerPixel(param.getPreviewFormat()) / 8;
					previewCallBackBuff = new byte[size];
					LogManage.debugLog("CameraManage::open previewCallBackBuff size = " + size);
				}
	//#ifndef ANDROID_ROTATION
			/**	 change android rotatrion
				// 撮影時に９０度回転する設定
				param.setRotation(90);
				this.camera.setParameters(param);
			*/
	//#endif

				// コールバック設定
				mErrorCallback = errorCallback;
				this.camera.setErrorCallback(new CameraErrorCallBack());
				this.camera.setPreviewCallbackWithBuffer(new CameraPreviewCallBack());
				this.camera.addCallbackBuffer(previewCallBackBuff);

				LogManage.debugLog("CameraManage::open Camera.open()");
			}catch(Exception e){
				return false;
			}
		}
		return true;
    }

	/**
	 * カメラの開放
	 */
    public void release() {
   		LogManage.debugLog("CameraManage::release() IN");

        if (this.camera != null) {
        	if( mbAutoFocus ){
        		this.camera.cancelAutoFocus();
        	}
			this.camera.stopPreview();

			this.camera.setErrorCallback(null);
			this.camera.setPreviewCallbackWithBuffer(null);
            this.camera.release();
            this.camera = null;
			LogManage.debugLog("CameraManage::release() camera Release");
        }
    }

    private void updateCameraParametersInitialize()
    {
        if (this.camera == null) {
			return;
		}
		Camera.Parameters param = this.camera.getParameters();
        List<Integer> frameRates = param.getSupportedPreviewFrameRates();
        if (frameRates != null) {
            Integer max = Collections.max(frameRates);
            param.setPreviewFrameRate(max);
    		this.camera.setParameters(param);
        }
    }

	/**
	 * フォーカスの設定を行う
	 */
	private void setFocusMode() {

        if (this.camera == null) {
			return;
		}

		Camera.Parameters param = this.camera.getParameters();

		// サポート情報取得
		List<String> supportFocusMode = param.getSupportedFocusModes();

		// フォーカスモード判定
		boolean isSupportedAuto = false;	// Parameters.FOCUS_MODE_AUTO (API5)
		boolean isSupportedMacro = false;	// Parameters.FOCUS_MODE_MACRO (API5)
		boolean isSupportedCaf = false;		// "caf"

		if( supportFocusMode == null ){
			return;
		}

		for (int i = 0; i < supportFocusMode.size(); i++){
			String focusMode = supportFocusMode.get(i).trim();
			LogManage.debugLog("CameraManage::setFocusMode(): " + focusMode);

			// フォーカスモード(CAF)判定
			if(focusMode.compareTo("caf") == 0){
				isSupportedCaf = true;
			}
			// フォーカスモード(AF)判定
			if(focusMode.compareTo(Parameters.FOCUS_MODE_AUTO) == 0){
				isSupportedAuto = true;
				isSetAutoFocus = true;
			}
			// フォーカスモード(Macro)判定
			if(focusMode.compareTo(Parameters.FOCUS_MODE_MACRO) == 0){
				isSupportedMacro = true;
			}
		}

		// フォーカスモード設定
		if(isSupportedMacro){
			LogManage.debugLog("CameraManage::setFocusMode(): FocusMode:FOCUS_MODE_MACRO");
			param.setFocusMode(Parameters.FOCUS_MODE_MACRO);
		}else if(isSupportedCaf){
			LogManage.debugLog("CameraManage::setFocusMode(): FocusMode:caf");
			param.setFocusMode("caf");
		}else if(isSupportedAuto){
			LogManage.debugLog("CameraManage::setFocusMode(): FocusMode:FOCUS_MODE_AUTO");
			param.setFocusMode(Parameters.FOCUS_MODE_AUTO);
			isSetAutoFocus = true;
		}
		
		if( android.os.Build.MANUFACTURER.equals("FUJITSU TOSHIBA MOBILE COMMUNICATIONS LIMITED") && isSupportedAuto ){
			LogManage.debugLog("android.os.Build.MANUFACTURER : " + android.os.Build.MANUFACTURER);
			LogManage.debugLog("CameraManage::setFocusMode(): FocusMode:FOCUS_MODE_AUTO");
			param.setFocusMode(Parameters.FOCUS_MODE_AUTO);
			isSetAutoFocus = true;
		}
		
		this.camera.setParameters(param);
	}


	/**
	 * 画像サイズの設定を行う
	 */
	private void setPictureSize() {

        if (this.camera == null) {
			return;
		}
		Camera.Parameters param = this.camera.getParameters();

		// サポート情報取得
		List<Camera.Size> supportPicSizes = param.getSupportedPictureSizes();

		//ログ出力
		Camera.Size csize = null;
		for (int i = 0; i < supportPicSizes.size(); i++) {
			csize = (Camera.Size)supportPicSizes.get(i);
			LogManage.debugLog("CameraManage::setPictureSize(): -SupportedPictureSizes():"+csize.width+", "+csize.height);
		}

		csize = searchSize(supportPicSizes, JUST_PIC_WIDTH, JUST_PIC_HEIGHT);
		param.setPictureSize(csize.width, csize.height);
		LogManage.debugLog("CameraManage::setPictureSize():w:" + csize.width + ", h:" + csize.height);
		this.camera.setParameters(param);
	}

	/**
	 * 描画サイズの設定を行う
	 */
	private void setPreviewSize(int width, int height) {

        if (this.camera == null) {
			return;
		}

		Camera.Parameters param = this.camera.getParameters();

		// 各種サポート情報取得
		List<Camera.Size> supportPrevSizes = param.getSupportedPreviewSizes();

		Camera.Size csize = null;
		for(int i = 0; i < supportPrevSizes.size(); i++){
			csize = (Camera.Size)supportPrevSizes.get(i);
			LogManage.debugLog("CameraManage::PreviewSize(): -getSupportedPreviewSizes():"+csize.width+", "+csize.height+  "  f : " + (float)((float)csize.width/(float)csize.height) + " --------------------");
		}
		LogManage.debugLog(" width : " + width + " height : " + height + "  f : " + (float)((float)height/(float)width));

		csize = searchSize(supportPrevSizes, height, width);
		setPreviewSize(csize);
	}

	/**
	 * 描画サイズの設定を行う
	 */
	public boolean setPreviewSize(Size previewSize) {

        if (this.camera == null) {
			return false;
		}
		Camera.Parameters param = this.camera.getParameters();
		if (param == null) {
			LogManage.debugLog("CameraManage::setPreviewSize() Parameters is null");
			return false;
		}
		param.setPreviewSize(previewSize.width, previewSize.height);
		LogManage.debugLog("CameraManage::setPreviewSize():w:" + previewSize.width + ", h:" + previewSize.height);
		this.camera.setParameters(param);
		return true;
	}



	/**
	 * カメラのサイズに合うサイズのパラメータを取得する
	 * @param supportSize サイズリスト
	 * @param justWidth 横幅
	 * @param justHeight 縦幅
	 */
	private Camera.Size searchSize(List<Camera.Size> supportSizes,
								   int justWidth,
								   int justHeight) {

		LogManage.debugLog("tagetsize w : " + justWidth + " tagetsize h : " + justHeight);		LogManage.debugLog("tagetsize w : " + justWidth + " tagetsize h : " + justHeight);
		Camera.Size csize = null;
		int size = supportSizes.size();

		// justサイズ判定
		for(int i = 0; i < size; i++){
			csize = supportSizes.get(i);
			if (csize.width == justWidth && csize.height == justHeight){
				return csize;
			}
		}

		//アスペクト比の最小を取得する
		float diffAspect = Float.MAX_VALUE;
		int index = 0;
		diffAspect = Float.MAX_VALUE;
		for(int i = 0; i < size; i++){
			csize = supportSizes.get(i);
			float aspect = Math.abs(((float)csize.width/(float)csize.height) - ((float)justWidth/(float)justHeight));
			LogManage.debugLog("w : " + csize.width + " h : " + csize.height + " aspect : " + aspect);
			if( diffAspect > aspect ){
				diffAspect = aspect;
			}
		}
		
		//最小のアスペクト比＋0.1分の閾値を設けてサイズ取得する
		List<Camera.Size> minList = new ArrayList<Camera.Size>();
		for(int i = 0; i < size; i++){
			csize = supportSizes.get(i);
			float aspect = Math.abs(((float)csize.width/(float)csize.height) - ((float)justWidth/(float)justHeight));
			if( aspect <= (diffAspect + 0.1f) ){
				minList.add(csize);
			}
		}

		//取得したサイズから幅と高さが一番近いものを選ぶ
		size = minList.size();
		int diff = Integer.MAX_VALUE;
		for(int i = 0; i < size; i++){
			csize = minList.get(i);
			int w = 0, h = 0;
			w = Math.abs(csize.width - justWidth);
			h = Math.abs(csize.height - justHeight);
			LogManage.debugLog("w : " + csize.width + " h : " + csize.height);
			LogManage.debugLog("diff : w : " + w + " h : " + h );
			if( diff > w + h  ){
				diff = w + h;
				index = i;
			}
		}
		LogManage.debugLog("w : " + minList.get(index).width + " h : " + minList.get(index).height);
		
		return minList.get(index);
	}


	/**
	 * ライトの設定
	 * @param light ライト設定
	 */
	public void setLight(boolean light) {
        if (this.camera == null) {
			return;
		}
		Camera.Parameters param = this.camera.getParameters();
		String mode = Camera.Parameters.FLASH_MODE_OFF;
		if (light == true) {
			mode = Camera.Parameters.FLASH_MODE_ON;
		}
		param.setFlashMode(mode);
		this.camera.setParameters(param);
	}


	/**
	 * カメラの明度設定
	 * @param brigth 明度
	 */
	public void setBrightness(int bright) {
        if (this.camera == null) {
			return;
		}
		Camera.Parameters param = this.camera.getParameters();

        int max = param.getMaxExposureCompensation();
        int min = param.getMinExposureCompensation();
        if (bright >= min && bright <= max) {
        	param.setExposureCompensation(bright);
        }
        LogManage.debugLog("ExposureCompensation max : " + max + " min : " + min );
		this.camera.setParameters(param);
	}


	/**
	 * カメラの撮影角度設定
	 * @param rotation
	 */
	public void setRotation(int rotation )
	{
        if (this.camera == null) {
			return;
		}
		LogManage.debugLog("CameraManage::setRotation() rotation : " + rotation);
		Camera.Parameters param = this.camera.getParameters();
		param.setRotation(rotation);
		this.camera.setParameters(param);
	}

	public void setOrientation(String ori)
	{
        if (this.camera == null) {
			return;
		}
		Camera.Parameters param = this.camera.getParameters();
		param.set("orientation", ori);
		this.camera.setParameters(param);
	}

	/**
	 * レシート撮影を実施する
	 */
	public boolean start() {
        if (this.camera == null) {
			return false;
		}

		if( mIsStartAutoFocus || mbAutoFocus || mIsStart){
			return false;
		}

		mIsStart = true;
		if(isSetAutoFocus == true){
			autoFocus();
		}else{
			takePicture();
		}
		return true;
	}

	public void startAutoFocus(){
		if( isSetAutoFocus && !mIsStartAutoFocus){
			mIsStartAutoFocus = true;
			autoFocus();
		}
	}

	public byte[] getCallbackBuffer()
	{
		return previewCallBackBuff;
	}

    //写真撮影
    private void takePicture() {
        if (this.camera == null) {
			return;
		}

		LogManage.debugLog("CameraManage::takePicture() startOcrJni takePicture()");
		//カメラのスクリーンショットの取得
    	this.camera.takePicture(null,null,new OnPictureTaken());
    }

	/**
	 * autoFocus の設定を実施する
	 */
	private void autoFocus() {
        if (this.camera == null) {
			return;
		}
		mbAutoFocus = true;
		this.camera.autoFocus(new CameraAutoFocusCallback());
	}

	/**
	 * AutoFocus 用コールバック
	 */
	private class CameraAutoFocusCallback implements Camera.AutoFocusCallback
	{
		public void onAutoFocus(boolean success, Camera camera) {
			LogManage.debugLog("CameraManage onAutoFocus():" + success);
			if( !mIsStartAutoFocus ){
				camera.setOneShotPreviewCallback(new CameraOnePreviewCallBack());
			}
			mbAutoFocus = false;
			mIsStartAutoFocus = false;
		}
	};

	/**
	 * 写真撮影用コールバック
	 */
	private class CameraOnePreviewCallBack implements Camera.PreviewCallback
	{
		public void onPreviewFrame(byte[] data, Camera camera){
			LogManage.debugLog("CameraManage onPreviewFrame()");
			takePicture();
		}
	};

	private class CameraPreviewCallBack implements Camera.PreviewCallback
	{
		public void onPreviewFrame(byte[] data, Camera camera){
			//LogManage.debugLog("CameraPreviewCallBack::onPreviewFrame()");
			//invalidate();
			camera.addCallbackBuffer(previewCallBackBuff);
		}
	};

	/**
	 * カメラ撮影後の処理を定義するインターフェースクラス
	 */
    private class OnPictureTaken implements Camera.PictureCallback {
		public void onPictureTaken(byte[] data, Camera camera)
		{
			if( cameraListener != null ){
				cameraListener.onPictureTaken(data, camera);
			}
			mIsStart = false;
		}
	}

    private class CameraErrorCallBack implements Camera.ErrorCallback{
		public void onError(int error, Camera camera)
		{
			if( mErrorCallback != null ){
				mErrorCallback.onError(error, camera);
			}
			mIsStart = false;
		}
    }
}

