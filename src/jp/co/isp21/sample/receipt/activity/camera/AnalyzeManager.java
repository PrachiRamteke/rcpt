package jp.co.isp21.sample.receipt.activity.camera;

import jp.co.isp21.sample.receipt.data.ReceiptInfo;

public class AnalyzeManager
{
	static {
		// JNIロード
		System.loadLibrary("receiptAnalyzer");
		System.loadLibrary("receiptocr");
		System.loadLibrary("ReceiptAnalyticalEngine");
	}

    // JNI
	public static native int receiptAnalyze(int[] src, int width, int height, ReceiptInfo receiptInfo);
	public static native int initLongReceipt();
	public static native int setDividedLongReceipt();
	public static native int analyzeDividedLongReceipt(int[] src, int width, int height, ReceiptInfo receiptInfo);
	public static native int analyzeLongReceipt(ReceiptInfo receiptInfo);
	public static native int cancelLongAnalyze();
	public static native int getEngineVersion(int[] version);
}
