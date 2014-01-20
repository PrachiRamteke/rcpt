package jp.co.isp21.sample.receipt.activity.camera;

import java.util.List;

import jp.co.isp21.sample.receipt.R;
import jp.co.isp21.sample.receipt.data.ReceiptInfo;
import jp.co.isp21.sample.receipt.util.LogManage;
import jp.co.isp21.sample.receipt.util.TimerTaskManager;
import jp.co.isp21.sample.receipt.util.TimerTaskManager.TimerTaskListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

// ----------------------------------------------------------------------

public class CameraActivity extends Activity implements OnClickListener {
	public static final String KEY_RESULT = "result";
	public static final String KEY_MODE = "mode";
	public static final int CAMERA_LONG_MODE_NO = 0;
	public static final int CAMERA_LONG_MODE_YES = 1;

	private static final int DIALOG_REQUEST_OPEN_ERROR = 0;
	private static final int CAMERA_BATTERY_LOW = 1;	// 繝舌ャ繝�Μ繝ｼ荳崎ｶｳ
	private static final int DIALOG_REQUEST_CAMERA_ERROR = 2;
	private static final int DIALOG_REQUEST_CAMERA_MODE = 3;

	// 隗｣譫千ｵ先棡繧ｳ繝ｼ繝�
	public static final int CAMERA_ANALYZE_PROC  = 10;	// 繧ｫ繝｡繝ｩ隗｣譫蝉ｸｭ
	public static final int CAMERA_ANALYZE_OK    = 11;	// 繧ｫ繝｡繝ｩ隗｣譫先�蜉�
	public static final int CAMERA_ANALYZE_ERROR = 12;	// 繧ｫ繝｡繝ｩ隗｣譫舌お繝ｩ繝ｼ
	public static final int CAMERA_MEMORY_ERROR  = 13;	// OutOfMemory 逋ｺ逕�
	public static final int CAMERA_PREVIEW_ERROR = 14;	// 繧ｫ繝｡繝ｩ陦ｨ遉ｺ繧ｨ繝ｩ繝ｼ

	public static final int CAMERA_IMAGE_STORAGE_ERROR = -999;

	public static final int CAMERA_NORMAL_MODE = 0;
	public static final int CAMERA_LONG_MODE = 1;
	public static final int CAMERA_BARCODE_MODE = 2;


    private CameraView cameraView;
	SubMenu sub_flash = null;
	SubMenu sub_exposure = null;
	private boolean mInit;

	// 繝舌ャ繝�Μ繝ｼ髢｢騾｣
    public static final int LIMMIT_BATTERY_LEVEL = 10;              //繝ｭ繝ｼ繝舌ャ繝�Μ繝ｼ髢ｾ蛟､
	private boolean mbRegistIntent;
	private Intent mIntentFilter;
	private boolean mIsTakePicture;
    private PictureTask mPictureTask;
    private Menu mMenu = null;
    private boolean mIsStart;
    private int mCameraMode;
    
	/**
	 * 繝�う繧｢繝ｭ繧ｰ遞ｮ蛻･ID
	 */
	protected static final int DIALOG_YES_NO_MESSAGE = 1;
	protected static final int DIALOG_YES_MESSAGE = 2;
	protected static final int DIALOG_STRING_LIST = 3;

	/**
	 * 繝�う繧｢繝ｭ繧ｰ縺ｮ繝��繧ｿ險ｭ螳哮EY
	 */
	protected static final String DIALOG_KEY_MESSAGE = "dialog_key_message";			/**< putString縺ｫ險ｭ螳壹☆繧�*/
	protected static final String DIALOG_KEY_TITLE = "dialog_key_title";				/**< putString縺ｫ險ｭ螳壹☆繧�*/
	protected static final String DIALOG_KEY_REQUEST_CODE = "dialog_key_requestcode";	/**< putInt縺ｫ險ｭ螳壹☆繧�*/
	protected static final String DIALOG_KEY_ICON = "dialog_key_icon";					/**< putInt縺ｫ險ｭ螳壹☆繧�*/
	protected static final String DIALOG_KEY_STRING_LIST = "dialog_key_string_list";	/**< putStringArray縺ｫ險ｭ螳壹☆繧�*/
	protected static final String DIALOG_KEY_STRING_LIST_SELECT = "dialog_key_string_list_select";	/**< putInt縺ｫ險ｭ螳壹☆繧�*/
    
    private static int[] mPixels;

    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

   		LogManage.debugLog("CameraActivityEx::onCreate() IN");

		// 繝輔Ν繧ｹ繧ｯ繝ｪ繝ｼ繝ｳ陦ｨ遉ｺ
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// 髮ｻ豎�メ繧ｧ繝�け
		mIntentFilter = RegistIntentFilter();
		if( !checkBatteryLevel(mIntentFilter) ){
			showAlertDialog();
			return;
		}

		// View險ｭ螳�
		setContentView(R.layout.activity_camera);

		// 繧ｫ繝｡繝ｩ
		cameraView = (CameraView)findViewById(R.id.camera_view);

		// 謦ｮ蠖ｱ繝懊ち繝ｳ蜿門ｾ�
		findViewById(R.id.btn_camera).setOnClickListener(this);

		mCameraMode = CAMERA_NORMAL_MODE;

		findViewById(R.id.btn2).setOnClickListener(this);
		findViewById(R.id.btn_camera_mode).setOnClickListener(this);
		findViewById(R.id.btn2).setVisibility(View.INVISIBLE);
		findViewById(R.id.camera_layout).setOnClickListener(CameraActivity.this);

		// 繝ｪ繧ｹ繝翫�縺ｮ險ｭ螳�
		CameraManage.getInstance().setPictureCallback(new OnPictureTaken());

		mIsTakePicture = false;
		mInit = true;
		mPictureTask = null;
		mIsStart = false;
    }

    protected void clearFlag()
    {
    	mIsStart = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
   		LogManage.debugLog("CameraActivityEx::onResume() IN");

   		// 髮ｻ豎�メ繧ｧ繝�け
        mIntentFilter = RegistIntentFilter();
        if( !checkBatteryLevel(mIntentFilter) ){
			showAlertDialog();
			return;
		}

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		removeDialog(DIALOG_YES_MESSAGE);

		if( mPictureTask != null ){
			if( mPictureTask.isCancelled() ){
				mPictureTask = null;
			}
		}

		startTimer( new TimerTaskListener()
		{
			public void run()
			{
				// 繧ｫ繝｡繝ｩ縺ｮ繧ｪ繝ｼ繝励Φ
				if( !CameraManage.getInstance().open(new CameraErrorCallBack(),cameraView.getWidth(),cameraView.getHeight()) ){
					Bundle bundle = new Bundle();
					customShowYesDialog(DIALOG_REQUEST_OPEN_ERROR, null, getString(R.string.str_camera_init_err), bundle);
				}

				// 繧ｫ繝｡繝ｩ謠冗判髢句ｧ�
		        cameraView.startPreview();

		        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
		        	CameraManage.getInstance().setOrientation("landscape");
		        }else{
		        	CameraManage.getInstance().setOrientation("portrait");
		        }
			}

		}, 50 );

        mIntentFilter = RegistIntentFilter();
    }


    @Override
    protected void onPause() {
        super.onPause();
   		LogManage.debugLog("CameraActivityEx::onPause() IN");

   		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Because the Camera object is a shared resource, it's very
        // important to release it when the activity is paused.
		PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        if (pm.isScreenOn() == true) {
			LogManage.debugLog("CameraActivityEx::onPause() screenOn true");
		}

        if( mMenu != null ){
        	mMenu.findItem(R.id.submenu_camera_light_off).setChecked(true);
        }

		if( mPictureTask != null ){
			if( mPictureTask.getStatus() == AsyncTask.Status.RUNNING ){
				mPictureTask.cancel(true);
			}
		}

		CameraManage.getInstance().release();
		UnRegistIntentFilter(mBroadcastReceiver);
		mIsStart = false;
    }

    @Override
    protected void onDestroy()
    {
		if( mIsTakePicture){
   			mIsTakePicture = false;
   			AnalyzeManager.cancelLongAnalyze();
		}

    	super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inf = getMenuInflater();
    	inf.inflate(R.menu.menu_camera, menu);

    	//繝ｩ繧ｸ繧ｪ繝懊ち繝ｳ險ｭ螳�
    	menu.setGroupCheckable(R.id.menu_camera_light, true, true);
    	// 繝�ヵ繧ｩ繝ｫ繝�FF縺ｫ繝√ぉ繝�け�医Λ繧､繝郁ｨｭ螳夲ｼ�
    	menu.findItem(R.id.submenu_camera_light_off).setChecked(true);
    	// 繝�ヵ繧ｩ繝ｫ繝域�繧九＆0縺ｫ繝√ぉ繝�け�域�繧九＆險ｭ螳夲ｼ�
    	menu.findItem(R.id.submenu_camera_brightness_3).setChecked(true);

    	mMenu = menu;
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
    	List<String> list = CameraManage.getInstance().getSupportedFlashModes();
    	if( list == null ){
    		menu.findItem(R.id.menu_camera_light).setVisible(false);
    	}

   		menu.findItem(R.id.menu_camera_brightness).setVisible(false);
    	return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

		switch(item.getItemId()) {
			// 繝ｩ繧､繝郁ｨｭ螳�
			case R.id.submenu_camera_light_on: // On
				// 繝ｩ繧､繝�N
				CameraManage.getInstance().setLight(true);
				break;
			case R.id.submenu_camera_light_off: // Off
				// 繝ｩ繧､繝�FF
				CameraManage.getInstance().setLight(false);
				break;
			// 譏弱ｋ縺戊ｨｭ螳�螳滄圀縺ｮ險ｭ螳壼�縺ｯ-12邂ｸ�ｷ12縺九ｂ縺励ｌ縺ｪ縺�
			case R.id.submenu_camera_brightness_1: // -2
				// 譏弱ｋ縺�2
				CameraManage.getInstance().setBrightness(-2);
				break;
			case R.id.submenu_camera_brightness_2: // -1
				// 譏弱ｋ縺�1
				CameraManage.getInstance().setBrightness(-1);
				break;
			case R.id.submenu_camera_brightness_3: // 0
				// 譏弱ｋ縺�
				CameraManage.getInstance().setBrightness(0);
				break;
			case R.id.submenu_camera_brightness_4: // 1
				// 譏弱ｋ縺�
				CameraManage.getInstance().setBrightness(1);
				break;
			case R.id.submenu_camera_brightness_5: // 2
				// 譏弱ｋ縺�
				CameraManage.getInstance().setBrightness(2);
				break;
			case R.id.menu_version:
				// 繝舌�繧ｸ繝ｧ繝ｳ陦ｨ遉ｺ
				int[] version = {0};
				version = new int[4];
				AnalyzeManager.getEngineVersion(version);
				String strVersion = "Ver " + Integer.toString(version[0]) + "." + Integer.toString(version[1]) + "." + Integer.toString(version[2]);

				Toast.makeText(getApplicationContext(), strVersion, Toast.LENGTH_SHORT).show();
				break;
        default:
            return super.onOptionsItemSelected(item);
        }
		setChecked(item);
		return true;
    }

    // 繝ｩ繧ｸ繧ｪ繝懊ち繝ｳ縺ｮON/OFF蛻�ｊ譖ｿ縺�
    private void setChecked(MenuItem item){
    	 if (item.isChecked()){
    		 item.setChecked(false);
    	 } else {
    		  item.setChecked(true);
    	  }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
    	if( event.getRepeatCount() > 0 ){
    		return true;
    	}

        if (keyCode == KeyEvent.KEYCODE_CAMERA) {
			startAnalysis();
        }

    	return super.onKeyDown(keyCode, event);
    }

	// 繝舌ャ繝�Μ繝ｼ髢｢騾｣
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver()
    {
		@Override
		public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if( action.equals(Intent.ACTION_BATTERY_CHANGED) ) {
                //繝舌ャ繝�Μ迥ｶ諷九ｒ蜿門ｾ�
                if( !checkBatteryLevel(intent) ){
    				// 繝�う繧｢繝ｭ繧ｰ陦ｨ遉ｺ
					showAlertDialog();
                }
            }
		}
    };

	public Intent RegistIntentFilter()
    {
    	if( mBroadcastReceiver == null ){
    		return null;
    	}

    	if( mbRegistIntent ){
    		return mIntentFilter;
    	}
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        Intent intent = registerReceiver(mBroadcastReceiver, filter);
        mbRegistIntent = true;
        return intent;
    }

    public void UnRegistIntentFilter(BroadcastReceiver br)
    {
    	if( br == null || !mbRegistIntent ){
    		return;
    	}

    	unregisterReceiver(br);
    	mbRegistIntent = false;
    }

	public boolean checkBatteryLevel(Intent intent){
        boolean ret = false;

        if(intent != null){
            int batteryLevel = intent.getExtras().getInt(BatteryManager.EXTRA_LEVEL);

            //繝舌ャ繝�Μ繝ｼ繝√ぉ繝�け
            if(batteryLevel <= LIMMIT_BATTERY_LEVEL ){
                if(isCharging(intent) == false){
                    ret = false;
                }
                else{//繝ｭ繝ｼ繝舌ャ繝�Μ繝ｼ縺ｧ繧ゅメ繝｣繝ｼ繧ｸ荳ｭ縺ｧ縺ゅｌ縺ｰ繧ｻ繝ｼ繝�
                    ret = true;
                }
            }
            else{
                ret = true;
            }
        }

        return ret;
    }


    public boolean isCharging(Intent intent) {
        boolean isCharging = false;
        int status = BatteryManager.BATTERY_STATUS_UNKNOWN;
        status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN);

        if ((status == BatteryManager.BATTERY_STATUS_CHARGING)
        ||  (status == BatteryManager.BATTERY_STATUS_FULL)) {
            isCharging = true;
        }
        return isCharging;
    }

	/**
	 * 繝�う繧｢繝ｭ繧ｰ縺九ｉ縺ｮ謌ｻ繧�
	 */
	protected void onClickDialog(DialogInterface dialog, int which , int id , Bundle data ) {
	    int request = data.getInt(DIALOG_KEY_REQUEST_CODE, 0);
	    switch(request){
		case CAMERA_BATTERY_LOW:
			finish();
			break;
		case CAMERA_ANALYZE_ERROR:
			break;
		case CAMERA_MEMORY_ERROR:
			finish();
			break;
		case DIALOG_REQUEST_OPEN_ERROR:
		case DIALOG_REQUEST_CAMERA_ERROR:
			finish();
		case DIALOG_REQUEST_CAMERA_MODE:
			if( mIsTakePicture){
	   			mIsTakePicture = false;
	   			AnalyzeManager.cancelLongAnalyze();
			}
			mCameraMode = which;
			if( mCameraMode == CAMERA_NORMAL_MODE ){
				TextView tmode = (TextView)findViewById(R.id.text_mode);
				tmode.setText(R.string.str_camera_normal_mode);
				findViewById(R.id.btn2).setVisibility(View.INVISIBLE);
			}else{
				TextView tmode = (TextView)findViewById(R.id.text_mode);
				tmode.setText(R.string.str_camera_long_mode);
				if( mInit ){
					mInit = false;
				}
			}
			break;
		}
	}

	private void showAlertDialog() {
		Bundle bundle = new Bundle();
		customShowYesDialog(CAMERA_BATTERY_LOW,
							getString(R.string.str_dialog_conf_title),
							getString(R.string.str_camera_low_battery),
							bundle);
	}

	private void startAnalysis()
	{
		if( mPictureTask != null ){
			if( mPictureTask.getStatus() == AsyncTask.Status.RUNNING ){
				return;
			}
		}

		if( mCameraMode == CAMERA_LONG_MODE ){
			if( !mIsTakePicture ){
				AnalyzeManager.initLongReceipt();
			}
			mIsTakePicture = true;
		}
		if( CameraManage.getInstance().start() ){
			mIsStart = true;
		}
	}

	public void onClick(View v) {
		if( mIsStart ){
			return;
		}
		switch( v.getId() ){
		case R.id.btn_camera:
			startAnalysis();
			break;
		case R.id.btn_camera_mode:
			Bundle bundle = new Bundle();
			bundle.putInt(DIALOG_KEY_REQUEST_CODE, DIALOG_REQUEST_CAMERA_MODE);
			bundle.putInt(DIALOG_KEY_ICON, android.R.drawable.ic_dialog_info);
			bundle.putInt(DIALOG_KEY_STRING_LIST_SELECT, mCameraMode);
			bundle.putString(DIALOG_KEY_TITLE, "select mode");
			bundle.putStringArray(DIALOG_KEY_STRING_LIST, new String[]{"singlemode","longmode"});
			showDialog(DIALOG_STRING_LIST, bundle);
			break;
		case R.id.btn2:
			if( mIsTakePicture ){
	   			mIsTakePicture = false;
				ReceiptInfo receiptInfo = new ReceiptInfo();
				int result = AnalyzeManager.analyzeLongReceipt(receiptInfo);

				//繝｡繧､繝ｳ縺ｸ謌ｻ繧�
				cameraAnalyzeSuccess(receiptInfo,result);
			}
			break;
		case R.id.camera_layout:
			{
				CameraManage.getInstance().startAutoFocus();
			}
			break;
		}
	}


	/**
	 * 繝ｬ繧ｷ繝ｼ繝郁ｧ｣譫舌′豁｣蟶ｸ邨ゆｺ�＠縺溘→縺阪�蜃ｦ逅�ｒ陦後≧
	 */
	public void cameraAnalyzeSuccess(ReceiptInfo info, int result) {
		LogManage.debugLog("Tel:"+info.getTel());
		// 逕ｻ髱｢驕ｷ遘ｻ
		Intent intent = new Intent();
		intent.putExtra(ReceiptInfo.KEY, info);
		intent.putExtra(KEY_RESULT, result);
		setResult(RESULT_OK, intent);
		finish();
	}


	/**
	 * 繝ｬ繧ｷ繝ｼ繝郁ｧ｣譫舌′螟ｱ謨励＠縺溘→縺阪�蜃ｦ逅�ｒ陦後≧
	 * @param 繧ｨ繝ｩ繝ｼ逡ｪ蜿ｷ
	 */
	public void cameraAnalyzeFailed(int errno) {

		String message = "";
		if (errno == CAMERA_ANALYZE_ERROR) {
			Log.d("--------str_camera_read_receipt_ng-------", "true");
			message = getString(R.string.str_camera_read_receipt_ng);
		} else {
			Log.d("--------str_camera_out_of_memory-------", "true");
			message = getString(R.string.str_camera_out_of_memory);
		}

		// 繝�う繧｢繝ｭ繧ｰ陦ｨ遉ｺ
		Bundle bundle = new Bundle();
		customShowYesDialog(errno,
							getString(R.string.str_dialog_conf_title),
							message,
							bundle);
	}

	/**
	 * 繧ｫ繝｡繝ｩ謦ｮ蠖ｱ蠕後�蜃ｦ逅�ｒ螳夂ｾｩ縺吶ｋ繧､繝ｳ繧ｿ繝ｼ繝輔ぉ繝ｼ繧ｹ繧ｯ繝ｩ繧ｹ
	 */
    private class OnPictureTaken implements Camera.PictureCallback {
		public void onPictureTaken(byte[] data, Camera camera)
		{
			mPictureTask = getPictureTask();
			mPictureTask.execute(data, camera);
		}
	}

    protected PictureTask getPictureTask()
	{
		return new PictureTask(CameraActivity.this);
	}

    protected class PictureTask extends AsyncTask<Object, Integer, Integer>
	{
		private ProgressDialog progressDialog = null;
		private Context mContext;
		private Camera mCamera;
		private ReceiptInfo mReceiptInfo;
		private int mMessageID;

		public PictureTask(Context context)
		{
			mContext = context;
			progressDialog = null;
			mMessageID = R.string.str_camera_analyzing;
		}

		public PictureTask(Context context, int messageId)
		{
			mContext = context;
			progressDialog = null;
			mMessageID = messageId;
		}

		protected ProgressDialog getProgressDialog()
		{
			return progressDialog;
		}

		@Override
	    protected void onPreExecute()
		{
			progressDialog = new ProgressDialog(mContext);
			progressDialog.setMessage(getString(mMessageID));
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setCancelable(true);
			progressDialog.setCanceledOnTouchOutside(false);					// 繝�う繧｢繝ｭ繧ｰ莉･螟悶ｒ繧ｿ繝�メ縺励◆髫帙�繧ｭ繝｣繝ｳ繧ｻ繝ｫ蜍穂ｽ懊ｒ辟｡蜉ｹ��ndroid4.0 ICS蟇ｾ蠢懶ｼ�
			progressDialog.setOnKeyListener(new OnKeyListener()
			{
				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event)
				{
			    	if( event.getRepeatCount() > 0 ){
			    		return true;
			    	}

			    	if( keyCode == KeyEvent.KEYCODE_SEARCH ){
			    		return true;
			    	}

					return false;
				}
			});
			progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					if( dialog != null ){
						dialog.cancel();
		            	dialog.dismiss();
					}
					PictureTask.this.cancel(true);

					if( !CameraActivity.this.isFinishing() ){
						finish();
					}
				}
			});
			progressDialog.show();
		}

		@Override
		protected void onCancelled()
		{
			super.onCancelled();
			if( progressDialog != null ){
				progressDialog.dismiss();
				progressDialog = null;
			}
			mIsStart = false;
		}

		@Override
		protected void onPostExecute(Integer result)
		{
			if( progressDialog != null ){
				progressDialog.dismiss();
				progressDialog = null;
			}
			mIsStart = false;

			if( isCancelled() ){
				mIsTakePicture = false;
				if (mCameraMode == CAMERA_LONG_MODE) {
					AnalyzeManager.cancelLongAnalyze();
				}
				return;
			}
			if( result == null ){
				mCamera.addCallbackBuffer(CameraManage.getInstance().getCallbackBuffer());
				mCamera.startPreview();
				cameraAnalyzeFailed(CAMERA_MEMORY_ERROR);
				return;
			}

			if (result >= 0) {
				Intent intent = new Intent();
				intent.putExtra(CameraActivity.KEY_RESULT, result );
				intent.putExtra(ReceiptInfo.KEY, mReceiptInfo );
				if (mCameraMode == CAMERA_NORMAL_MODE) {
					cameraAnalyzeSuccess(mReceiptInfo,result);
				} else {
					AnalyzeManager.setDividedLongReceipt();
					mCamera.addCallbackBuffer(CameraManage.getInstance().getCallbackBuffer());
					mCamera.startPreview();
					findViewById(R.id.btn2).setVisibility(View.VISIBLE);
				}
			} else {
				mCamera.addCallbackBuffer(CameraManage.getInstance().getCallbackBuffer());
				mCamera.startPreview();
				cameraAnalyzeFailed(CAMERA_ANALYZE_ERROR);
			}
		}

		@Override
		protected Integer doInBackground(Object... params)
		{
			if( params == null || params.length != 2 ){
				return null;
			}

			byte[] data = (byte[])params[0];
			mCamera = (Camera)params[1];

			if( data == null ){
				return null;
			}

			try{
	        	LogManage.debugLog("CameraActivity::takePicture() data.length = " + data.length);
				// 謦ｮ蠖ｱ繝��繧ｿ縺ｮ螟画鋤
				Bitmap bitmap_pic = BitmapFactory.decodeByteArray(data, 0, data.length);
				data = null;
				System.gc();
				System.gc();
				
				int bmpW = bitmap_pic.getWidth();
				int bmpH = bitmap_pic.getHeight();

				float rot = 90.0f;
				Matrix matrix = new Matrix();
				matrix.postRotate(rot);
				Bitmap bitmap = Bitmap.createBitmap(bitmap_pic, 0, 0, bmpW, bmpH, matrix, true);

				bitmap_pic.recycle();
				bitmap_pic = null;
				System.gc();

				bmpW = bitmap.getWidth();
				bmpH = bitmap.getHeight();
				if( mPixels == null ){
					mPixels = new int[bmpW*bmpH];
				}

				LogManage.debugLog("CameraActivity::takePicture() startOcrJni  new int[bmpW*bmpH] = " + bmpW*bmpH);

				LogManage.debugLog("CameraActivity::takePicture() startOcrJni getPixels");
				bitmap.getPixels(mPixels, 0, bmpW, 0, 0, bmpW, bmpH);
				LogManage.debugLog("CameraActivity::takePicture() startOcrJni bitmap.recycle()");
				bitmap.recycle();
				bitmap = null;
				System.gc();
				
				if( isCancelled() ){
					mIsTakePicture = false;
					return null;
				}

				int ret = 0;
				if( mCameraMode == CAMERA_NORMAL_MODE ){
					mReceiptInfo = new ReceiptInfo();
					// 繝ｬ繧ｷ繝ｼ繝郁ｧ｣譫宣幕蟋�
					ret = AnalyzeManager.receiptAnalyze(mPixels, bmpW, bmpH, mReceiptInfo);
					LogManage.debugLog("CameraActivity::takePicture() receiptAnalyze ret:" + ret);

				}else{
					mReceiptInfo = new ReceiptInfo();
					// 繝ｬ繧ｷ繝ｼ繝郁ｧ｣譫宣幕蟋�
					LogManage.debugLog("CameraActivity::takePicture() setAnalyzeAndReceiptInfo start:");
					ret = AnalyzeManager.analyzeDividedLongReceipt(mPixels, bmpW, bmpH, mReceiptInfo);
					LogManage.debugLog("CameraActivity::takePicture() setAnalyzeAndReceiptInfo end:" + ret);
					if( isCancelled() ){
						mIsTakePicture = false;
						AnalyzeManager.cancelLongAnalyze();
						return null;
					}
				}
				System.gc();
				return ret;

			} catch(OutOfMemoryError e){	//繝｡繝｢繝ｪ貅｢繧悟叙蠕�
				LogManage.errorLog("CameraActivity::takePicture() OutOfMemoryError", e);
				e.printStackTrace();
			}
			
			return null;
		}
	}

    /**
	 * 繧ｨ繝ｩ繝ｼ逋ｺ逕滓凾縺ｫ蜻ｼ縺ｰ繧後ｋ繧ｳ繝ｼ繝ｫ繝舌ャ繧ｯ
	 */
    private class CameraErrorCallBack implements Camera.ErrorCallback{
		public void onError(int error, Camera camera)
		{
			LogManage.errorLog("CameraManage onError() : " + error);
			Bundle bundle = new Bundle();
			customShowYesDialog(DIALOG_REQUEST_CAMERA_ERROR, null, getString(R.string.str_camera_Abnormal_err), bundle);
		}
    }
    
    @Override
    protected Dialog onCreateDialog(int id, Bundle data)
    {
    	String title = data.getString(DIALOG_KEY_TITLE);
    	String message = data.getString(DIALOG_KEY_MESSAGE);
    	final Bundle bundle = (Bundle)data.clone();
    	int icon = data.getInt(DIALOG_KEY_ICON,0);
    	String[] stringlist = data.getStringArray(DIALOG_KEY_STRING_LIST);
    	int select = data.getInt(DIALOG_KEY_STRING_LIST_SELECT,0);

        switch (id) {
	        case DIALOG_YES_NO_MESSAGE:
	        {
	            return new AlertDialog.Builder(this)
	        	.setCancelable(true)
	        	.setIcon(icon)
	        	.setTitle(title)
	        	.setMessage(message)
	            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int whichButton) {
	                	dialog.dismiss();
	                	onClickDialog(dialog, whichButton, DIALOG_YES_NO_MESSAGE, bundle);
	                	removeDialog(DIALOG_YES_NO_MESSAGE);
	                }
	            })
	            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int whichButton) {
	                	dialog.dismiss();
	                	onClickDialog(dialog, whichButton, DIALOG_YES_NO_MESSAGE , bundle);
	                	removeDialog(DIALOG_YES_NO_MESSAGE);
	                }
	            })
	            .setOnCancelListener(new DialogInterface.OnCancelListener(){
	            	@Override
	    			public void onCancel(DialogInterface dialog) {
	            		onClickDialog(dialog, DialogInterface.BUTTON_NEGATIVE , DIALOG_YES_MESSAGE, bundle );
	            	}
	            })
	            .create();
	        }
	        case DIALOG_YES_MESSAGE:
	        {
	            return new AlertDialog.Builder(this)
	            .setCancelable(true)
	        	.setTitle(title)
	        	.setIcon(icon)
	            .setMessage(message)
	            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int whichButton) {
	                	dialog.dismiss();
	                	onClickDialog(dialog, whichButton, DIALOG_YES_MESSAGE , bundle);
	                	removeDialog(DIALOG_YES_MESSAGE);
	                }
	            })
	            .setOnCancelListener(new DialogInterface.OnCancelListener(){
	            	@Override
	    			public void onCancel(DialogInterface dialog) {
	            		onClickDialog(dialog, DialogInterface.BUTTON_NEGATIVE , DIALOG_YES_MESSAGE, bundle );
	            	}
	            })
	           .create();
	        }
	    	case DIALOG_STRING_LIST:
	    	{
	    		return new AlertDialog.Builder(this)
	            .setCancelable(true)
	        	.setTitle(title)
	        	.setIcon(icon)
	            .setSingleChoiceItems(stringlist, select, new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int whichButton) {
	                	dialog.dismiss();
	                	removeDialog(DIALOG_STRING_LIST);
	            		onClickDialog(dialog, whichButton , DIALOG_STRING_LIST, bundle );
	                }
	            })
	            .setOnKeyListener(new OnKeyListener()
				{
					public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event)
					{
						if( keyCode == KeyEvent.KEYCODE_SEARCH )
							return true;
						return false;
					}
				})
	           .create();
	    	}
        }

    	return null;
    }
    
	/**
	 * OK縺ｮ縺ｿ縺ｮ繧｢繝ｩ繝ｼ繝医ム繧､繧｢繝ｭ繧ｰ繧定｡ｨ遉ｺ縺吶ｋ
	 * @param reqcode 繝ｪ繧ｯ繧ｨ繧ｹ繝医さ繝ｼ繝�
	 * @param title 繧ｿ繧､繝医Ν
	 * @param message 繝｡繝�そ繝ｼ繧ｸ
	 * @param bundle 繝舌Φ繝峨Ν
	 */
	protected void customShowYesDialog(int reqcode, String title, String message, Bundle bundle) {
		customShowDialog(DIALOG_YES_MESSAGE, reqcode, title, message, android.R.drawable.ic_dialog_info, bundle);
	}

	/**
	 * OK/繧ｭ繝｣繝ｳ繧ｻ繝ｫ逕ｨ繧｢繝ｩ繝ｼ繝医ム繧､繧｢繝ｭ繧ｰ繧定｡ｨ遉ｺ縺吶ｋ
	 * @param reqcode 繝ｪ繧ｯ繧ｨ繧ｹ繝医さ繝ｼ繝�
	 * @param title 繧ｿ繧､繝医Ν
	 * @param message 繝｡繝�そ繝ｼ繧ｸ
	 * @param bundle 繝舌Φ繝峨Ν
	 */
	protected void customShowYesNoDialog(int reqcode, String title, String message, Bundle bundle) {
		customShowDialog(DIALOG_YES_NO_MESSAGE, reqcode, title, message, android.R.drawable.ic_dialog_info, bundle);
	}

	/**
	 * 繧｢繝ｩ繝ｼ繝医ム繧､繧｢繝ｭ繧ｰ繧定｡ｨ遉ｺ縺吶ｋ
	 * @param type 繝�う繧｢繝ｭ繧ｰ縺ｮ遞ｮ蛻･
	 * @param reqcode 繝ｪ繧ｯ繧ｨ繧ｹ繝医さ繝ｼ繝�
	 * @param title 繧ｿ繧､繝医Ν
	 * @param message 繝｡繝�そ繝ｼ繧ｸ
	 * @param icon 繧｢繧､繧ｳ繝ｳ
	 */
	protected void customShowDialog(int type, int reqcode, String title, String message, int icon, Bundle bundle) {
		bundle.putInt(DIALOG_KEY_REQUEST_CODE, reqcode);	//繝ｪ繧ｯ繧ｨ繧ｹ繝医さ繝ｼ繝�
		bundle.putInt(DIALOG_KEY_ICON, icon);				//繧｢繧､繧ｳ繝ｳ險ｭ螳�
		bundle.putString(DIALOG_KEY_TITLE, title);			//繧ｿ繧､繝医Ν
		bundle.putString(DIALOG_KEY_MESSAGE, message);		//繝｡繝�そ繝ｼ繧ｸ
		showDialog(type, bundle);
	}
    
    /**
     * 繧ｿ繧､繝槭�縺ｮ襍ｷ蜍�
     * @param listener 邨碁℃譎る俣蠕後↓蜻ｼ縺ｳ蜃ｺ縺吶Μ繧ｹ繝翫�
     * @param delay 襍ｷ蜍墓凾髢�ms)
     */
    protected void startTimer(TimerTaskListener listener , int delay)
    {
        TimerTaskManager timer = new TimerTaskManager(listener);
        timer.startTimer(delay);
    }
    
}


