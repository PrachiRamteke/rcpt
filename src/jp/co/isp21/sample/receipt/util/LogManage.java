package jp.co.isp21.sample.receipt.util;

import android.app.Activity;
import android.content.res.Resources;
import android.util.Log;

/**
 * ログ出力クラス
 */
public class LogManage extends Activity {

	// ログ出力フラグ
	private static boolean logwrite = false;
	// ログ出力用タグ名
	private static final String TAG_NAME = "SampleReceiptCamera";

	/**
	 * ログ出力フラグを設定する
	 * @param res リソース
	 */
	public static void setResource(Resources res) {
		logwrite = true;
	}


	/**
	 * Info ログを出力する
	 * @param message メッセージ
	 */
	public static void infoLog(String message) {
		if (logwrite == false) {
			return;
		}
		Log.i(TAG_NAME, message);
	}

	/**
	 * Debug ログを出力する
	 * @param message メッセージ
	 */
	public static void debugLog(String message) {
		if (logwrite == false) {
			return;
		}
		Log.d(TAG_NAME, message);
	}

	/**
	 * Error ログを出力する
	 * @param message メッセージ
	 */
	public static void errorLog(String message)	{
		if (logwrite == false) {
			return;
		}
		Log.e(TAG_NAME, message);
	}

	/**
	 * Error ログを出力する
	 * @param message メッセージ
	 * @param th Throwable
	 */
	public static void errorLog(String message, Throwable tr)	{
		if (logwrite == false) {
			return;
		}
		Log.e(TAG_NAME, message, tr);
	}
}
