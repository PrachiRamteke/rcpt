/**
 * @file JniAnalyzeWrapper.c
 * @brief AndroidJNItest
 *
 * @author ami.obara
 */
#include <jni.h>
#include <android/log.h>
#include <string.h>
#include <time.h>
#include <arpa/inet.h>

#include "ReceiptAnalyticalEngine.h"

/* ************************************************************************	*/
/* 定義																		*/
/* ************************************************************************	*/
/* Android - logcat用定義 */
#undef	LOG_TAG
#define	LOG_TAG		"SampleReceipt_JNI"
#define LOGD(...) 	__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

#define RET_OK			0
#define RET_NG			-1

#define ITEMS_SYGNATURE "[Ljp/co/isp21/sample/receipt/data/ItemInfo;"
#define ITEMS_CLASS_PATH "jp/co/isp21/sample/receipt/data/ItemInfo"

#define NON_TOTAL			0x1		/**< 合計なし */
#define NON_DATE			0x2		/**< 日付なし */
#define NON_ITEM			0x4		/**< 品目なし */
#define ILLEGA_SUM			0x8		/**< 合計SUMチェック不正 */

/* ************************************************************************	*/
/* プロトタイプ宣言															*/
/* ************************************************************************	*/
int SetReceiptDataToJavaField(JNIEnv *env, ReceiptInfo *receipt, jobject *receiptInfo);

/**
 * RGB888 to Y
 *
 * @param[in] rgb888 RGB88色情報
 * @return Y値
 */
int ConvertRGB888toY(int rgb888){
	int r = (rgb888 & 0xFF0000)>>16;
	int g = (rgb888 & 0x00FF00)>>8;
	int b = (rgb888 & 0x0000FF);

	int tmpY = 299*r + 587*g + 114*b;
	int Y = tmpY/1000;

	/* 四捨五入処理（あまり変わらないようなら速度向上のため無視でも可） */
	if((tmpY%1000)>500){
		Y++;
	}
	return Y;
}

static int getAnalyzeResult(ReceiptInfo *receipt)
{
	int analyzeResult = RECEIPT_OK;
	int i = 0, total = 0;

	for(i = 0; i < receipt->cnt; i++) {
		total += receipt->items[i].price;
	}

	if (receipt->date.year == 0 || receipt->date.month == 0 || receipt->date.day == 0) {
		analyzeResult |= NON_DATE;
	}
	if (receipt->total == 0) {
		analyzeResult |= NON_TOTAL;
	}
	if (receipt->cnt == 0) {
		analyzeResult |= NON_ITEM;
	}
	if (receipt->total != 0 && receipt->cnt != 0 && receipt->total != total) {
		analyzeResult |= ILLEGA_SUM;
	}
	if (receipt->total == 0 && receipt->date.year == 0 && receipt->cnt == 0) {
		return RECEIPT_ANALYZE_ERR;
	}
	return analyzeResult;
}

jint Java_jp_co_isp21_sample_receipt_activity_camera_AnalyzeManager_receiptAnalyze(JNIEnv* env, jobject this, jintArray src, jint width, jint height, jobject receiptInfo)
{
	return startOcrJni(env, this, src, width, height, receiptInfo);
}

jint JNICALL Java_jp_co_isp21_sample_receipt_activity_camera_AnalyzeManager_initLongReceipt(JNIEnv* env, jobject this)
{
	return ExecInitLongReceipt();
}

jint JNICALL Java_jp_co_isp21_sample_receipt_activity_camera_AnalyzeManager_setDividedLongReceipt(JNIEnv* env, jobject this)
{
	ExecSetDividedLongReceipt();
	return RET_OK;
}

jint JNICALL Java_jp_co_isp21_sample_receipt_activity_camera_AnalyzeManager_analyzeDividedLongReceipt(JNIEnv* env, jobject this, jintArray src, jint width, jint height, jobject receiptInfo)
{
	int ret = RET_OK;
	int i;
	int pixel_cnt = width*height;
	char* y8_image = NULL;

	// OCR画像領域確保
	y8_image = (char *)malloc(pixel_cnt);
	if(y8_image == NULL) {
		LOGD("startOcrJni malloc ERROR");
		return RECEIPT_MEM_ERR;
	}

	// 領域確保
	jint*  arr1 = (*env)->GetIntArrayElements(env, src, 0);
	// Y値変換
	for(i=0;i<pixel_cnt;i++){
		y8_image[i] = ConvertRGB888toY(arr1[i]);
	}
	// 領域開放
	(*env)->ReleaseIntArrayElements(env, src, arr1, 0);

	// 構造体初期化
	ReceiptInfo receipt;
	memset(&receipt, 0, sizeof(ReceiptInfo));
	ret = ExecAnalyzeDividedLongReceipt(y8_image, width, height, &receipt);
	if(ret < 0) {
		free(y8_image);
		return ret;
	}
	free(y8_image);

	// レシートデータをJavaフィールドへセット
	if (0 > SetReceiptDataToJavaField(env, &receipt, &receiptInfo)) {
		LOGD("SetReceiptDataToJavaField ERROR");
		return RECEIPT_SYS_ERR;
	}
	return getAnalyzeResult(&receipt);
}

jint JNICALL Java_jp_co_isp21_sample_receipt_activity_camera_AnalyzeManager_analyzeLongReceipt(JNIEnv* env, jobject this, jobject receiptInfo)
{
	int ret = RET_OK;
	ReceiptInfo receipt;
	memset(&receipt, 0, sizeof(ReceiptInfo));
	
	ret = ExecAnalyzeLongReceipt(&receipt);
	if(ret < 0) {
		LOGD("ExecAnalyzeLongReceipt ERROR");
		return ret;
	}
	
	// レシートデータをJavaフィールドへセット
	if (0 > SetReceiptDataToJavaField(env, &receipt, &receiptInfo)) {
		LOGD("SetReceiptDataToJavaField ERROR");
		return RECEIPT_SYS_ERR;
	}
	return getAnalyzeResult(&receipt);
}

jint JNICALL Java_jp_co_isp21_sample_receipt_activity_camera_AnalyzeManager_cancelLongAnalyze(JNIEnv* env, jobject this)
{
	return ExecCancelLongReceipt();
}

jint JNICALL Java_jp_co_isp21_sample_receipt_activity_camera_AnalyzeManager_getEngineVersion(JNIEnv* env, jobject this, jintArray version)
{
    return getEngineVersion(env, this, version);
}

/**
 * レシート解析呼び出し
 *
 * @param[in] env JNIインタフェース第一引数
 * @param[in] this JNIインタフェース第二引数
 * @param[in] src 画像データ(RGB88色情報)
 * @param[in] width 画像幅
 * @param[in] height 画像高さ
 * @param[out] receiptInfo 解析されたレシート情報
 * @return 処理結果(0:正常、0以外：異常)
 */
jint startOcrJni(JNIEnv* env, jobject this, jintArray src, jint width, jint height, jobject receiptInfo)
{
	int ret = RET_OK;
	int i;
	int pixel_cnt = width*height;
	char* y8_image = NULL;

	// OCR画像領域確保
	y8_image = (char *)malloc(pixel_cnt);
	if(y8_image == NULL) {
		LOGD("startOcrJni malloc ERROR");
		return RECEIPT_MEM_ERR;
	}

	// 領域確保
	jint*  arr1 = (*env)->GetIntArrayElements(env, src, 0);
	// Y値変換
	for(i=0;i<pixel_cnt;i++){
		y8_image[i] = ConvertRGB888toY(arr1[i]);
	}
	// 領域開放
	(*env)->ReleaseIntArrayElements(env, src, arr1, 0);

	// 構造体初期化
	ReceiptInfo receipt;
	memset(&receipt, 0, sizeof(ReceiptInfo));
	ret = ExecReceiptAnalyze(y8_image, width, height, &receipt);
	if(ret < 0) {
		free(y8_image);
		return ret;
	}
	free(y8_image);

	// レシートデータをJavaフィールドへセット
	if (0 > SetReceiptDataToJavaField(env, &receipt, &receiptInfo)) {
		LOGD("SetReceiptDataToJavaField ERROR");
		return RECEIPT_SYS_ERR;
	}
	return getAnalyzeResult(&receipt);
}

/**
 * @brief レシートデータセット関数
 *
 * レシート解析エンジンにて解析済みデータを
 * Javaフィールドにセットする
 *
 * @param	   *env			JNI構造体ポインタ
 * @param[in]  *receipt	    解析済みレシート情報
 * @param[out] *receiptInfo セット先
 * @return                  OK=1 NG=-1
 */
int SetReceiptDataToJavaField(JNIEnv *env, ReceiptInfo *receipt, jobject *receiptInfo)
{
	jboolean b;
	int i, itemNameLen;

	// Java側の実態の取得
	jclass output = (*env)->GetObjectClass(env, *receiptInfo);
	if(0 == output){
		return RET_NG;
	}

	// JavaフィールドのCポインタ（ID）取得
	jfieldID idYear = (*env)->GetFieldID(env, output, "year", "S");
	jfieldID idMonth = (*env)->GetFieldID(env, output, "month", "S");
	jfieldID idDay = (*env)->GetFieldID(env, output, "day", "S");
	jfieldID idHour = (*env)->GetFieldID(env, output, "hour", "S");
	jfieldID idMin = (*env)->GetFieldID(env, output, "min", "S");
	jfieldID idSec = (*env)->GetFieldID(env, output, "sec", "S");
	jfieldID idSum = (*env)->GetFieldID(env, output, "total", "I");
	jfieldID itel = (*env)->GetFieldID(env, output, "tel", "[B");
	jfieldID idItemInfo = (*env)->GetFieldID(env, output, "items", ITEMS_SYGNATURE);

	jclass items = (*env)->FindClass(env, ITEMS_CLASS_PATH);
	if (items==NULL) return RET_NG;
	jmethodID constructer = (*env)->GetMethodID(env, items, "<init>", "()V");
	if (constructer==NULL) return RET_NG;

	if( receipt->cnt > 0 ){
		jobjectArray jni_items = (*env)->NewObjectArray(env, receipt->cnt, items, NULL);
		if (jni_items==NULL) return RET_NG;

		for(i = 0;i < receipt->cnt; i++)
		{
			// 品目情報のCポインタ取得
			jobject one_item = (*env)->NewObject(env, items, constructer);
			if (one_item==NULL) return RET_NG;
			jclass jni_itemInfo = (*env)->GetObjectClass(env, one_item);
			if(0 == jni_itemInfo){
				return RET_NG;
			}

			// 解析済み金額のセット
			jfieldID idPrice = (*env)->GetFieldID(env, jni_itemInfo, "price", "I");
			(*env)->SetIntField(env, one_item, idPrice, receipt->items[i].price);

			jfieldID idName = (*env)->GetFieldID(env, jni_itemInfo, "sjisName", "[B");

			jsize namelen = strnlen(receipt->items[i].name, ITEM_NAME_MAX);
			if( namelen > 0 ){
				jbyteArray jni_name = (*env)->NewByteArray(env, namelen);
				(*env)->SetByteArrayRegion(env , jni_name , 0 , namelen , receipt->items[i].name );
				(*env)->SetObjectField(env , one_item, idName, jni_name);
				(*env)->DeleteLocalRef(env , jni_name );
			}
			(*env)->SetObjectArrayElement(env, jni_items, i, one_item);
		}
		(*env)->SetObjectField(env , *receiptInfo , idItemInfo , jni_items );
	}

	// 解析済み日付のセット
	(*env)->SetShortField(env, *receiptInfo, idYear, receipt->date.year);
	(*env)->SetShortField(env, *receiptInfo, idMonth, receipt->date.month);
	(*env)->SetShortField(env, *receiptInfo, idDay, receipt->date.day);
	(*env)->SetShortField(env, *receiptInfo, idHour, receipt->date.hour);
	(*env)->SetShortField(env, *receiptInfo, idMin, receipt->date.minute);
	(*env)->SetShortField(env, *receiptInfo, idSec, receipt->date.second);

	// 解析済み合計金額のセット
	(*env)->SetIntField(env, *receiptInfo, idSum, receipt->total);

	jsize tellen = strnlen(receipt->tel, TEL_MAX);
	jbyteArray jtel = (*env)->NewByteArray(env, tellen);
	(*env)->SetByteArrayRegion(env , jtel , 0 , tellen , receipt->tel );
	(*env)->SetObjectField(env , *receiptInfo, itel, jtel);
	(*env)->DeleteLocalRef(env , jtel );
	

	return RET_OK;
}

/* ************************************************************************	*/
/* レシート解析エンジン呼び出し												*/
/* ************************************************************************	*/
#include <dlfcn.h>
#include <unistd.h>

#define LIBRARY_NAME	"libReceiptAnalyticalEngine.so"

/* 関数ポインタ */
#define FUNC_NAME "ReceiptAnalyze"
#define FUNC_NAME1 "InitLongReceipt"
#define FUNC_NAME2 "SetDividedLongReceipt"
#define FUNC_NAME3 "AnalyzeLongReceipt"
#define FUNC_NAME4 "CancelLongReceipt"
#define FUNC_NAME5 "AnalyzeDividedLongReceipt"
#define FUNC_NAME6 "GetEngineVersion"
int (*pReceiptAnalyze)(char *img, int width, int height, ReceiptInfo *receipt);
int (*pInitLongReceipt)(void);
int (*pSetDividedLongReceipt)(void);
int (*pAnalyzeLongReceipt)(ReceiptInfo *receipt);
int (*pCancelLongReceipt)();
int (*pAnalyzeDividedLongReceipt)(char *img, int width, int height, ReceiptInfo *receipt);
void (*pGetEngineVersion)(int *major, int *minor, int *revision);

/**
 * レシート解析実行
 *
 */
int ExecReceiptAnalyze(char *img, int width, int height, ReceiptInfo *receipt)
{
	void *handle = dlopen(LIBRARY_NAME, RTLD_LAZY);
    if (!handle) {
		LOGD("dlopen(%s) ERROR(%s)", LIBRARY_NAME, dlerror());
        return RECEIPT_LOAD_ERR;
    }

	pReceiptAnalyze = dlsym(handle, FUNC_NAME);
	if(pReceiptAnalyze == NULL) {
		LOGD("dlsym(%s) ERROR", FUNC_NAME);
		dlclose(handle);
        return RECEIPT_LOAD_ERR;
	}

	int ret = pReceiptAnalyze(img, width, height, receipt);

	dlclose(handle);

	return ret;
}

int ExecInitLongReceipt()
{
	void *handle = dlopen(LIBRARY_NAME, RTLD_LAZY);
    if (!handle) {
		LOGD("dlopen(%s) ERROR(%s)", LIBRARY_NAME, dlerror());
        return RET_NG;
    }

	pInitLongReceipt = dlsym(handle, FUNC_NAME1);
	if(pInitLongReceipt == NULL) {
		LOGD("dlsym(%s) ERROR", FUNC_NAME1);
		dlclose(handle);
        return RET_NG;
	}
	
	int ret = pInitLongReceipt();
	
	dlclose(handle);
	
	return ret;
}

int ExecSetDividedLongReceipt()
{
	void *handle = dlopen(LIBRARY_NAME, RTLD_LAZY);
    if (!handle) {
		LOGD("dlopen(%s) ERROR(%s)", LIBRARY_NAME, dlerror());
        return RET_NG;
    }

	pSetDividedLongReceipt = dlsym(handle, FUNC_NAME2);
	if(pSetDividedLongReceipt == NULL) {
		LOGD("dlsym(%s) ERROR", FUNC_NAME2);
		dlclose(handle);
        return RET_NG;
	}

	int ret = pSetDividedLongReceipt();

	dlclose(handle);

	return ret;
}

int ExecAnalyzeDividedLongReceipt(char *img, int width, int height, ReceiptInfo *receipt)
{
	void *handle = dlopen(LIBRARY_NAME, RTLD_LAZY);
    if (!handle) {
		LOGD("dlopen(%s) ERROR(%s)", LIBRARY_NAME, dlerror());
        return RECEIPT_LOAD_ERR;
    }

	pAnalyzeDividedLongReceipt = dlsym(handle, FUNC_NAME5);
	if(pAnalyzeDividedLongReceipt == NULL) {
		LOGD("dlsym(%s) ERROR", FUNC_NAME5);
		dlclose(handle);
		return RECEIPT_LOAD_ERR;
	}

	int ret = pAnalyzeDividedLongReceipt(img, width, height, receipt);

	dlclose(handle);

	return ret;
}

int ExecAnalyzeLongReceipt(ReceiptInfo *receipt)
{
	void *handle = dlopen(LIBRARY_NAME, RTLD_LAZY);
    if (!handle) {
		LOGD("dlopen(%s) ERROR(%s)", LIBRARY_NAME, dlerror());
        return RET_NG;
    }

	pAnalyzeLongReceipt = dlsym(handle, FUNC_NAME3);
	if(pAnalyzeLongReceipt == NULL) {
		LOGD("dlsym(%s) ERROR", FUNC_NAME3);
		dlclose(handle);
        return RET_NG;
	}
	
	int ret = pAnalyzeLongReceipt(receipt);
	
	dlclose(handle);
	
	return ret;
}

int ExecCancelLongReceipt()
{
	void *handle = dlopen(LIBRARY_NAME, RTLD_LAZY);
    if (!handle) {
		LOGD("dlopen(%s) ERROR(%s)", LIBRARY_NAME, dlerror());
        return RET_NG;
    }

	pCancelLongReceipt = dlsym(handle, FUNC_NAME4);
	if(pCancelLongReceipt == NULL) {
		LOGD("dlsym(%s) ERROR", FUNC_NAME4);
		dlclose(handle);
        return RET_NG;
	}
	
	int ret = pCancelLongReceipt();
	
	dlclose(handle);
	
	return ret;
}

jint getEngineVersion(JNIEnv* env, jobject this, jintArray version)
{
	void *handle = dlopen(LIBRARY_NAME, RTLD_LAZY);
	jint*  pVersion;

	int major_ver = 0;
	int minor_ver = 0;
	int revision_ver = 0;

	pGetEngineVersion = dlsym(handle, FUNC_NAME6);
	if(pGetEngineVersion == NULL) {
		LOGD("dlsym(%s) ERROR", FUNC_NAME6);
		dlclose(handle);
        return RET_NG;
	}
	
	pGetEngineVersion(&major_ver, &minor_ver, &revision_ver);

	pVersion  = (*env)->GetIntArrayElements(env, version, 0);
	pVersion[0] = major_ver;
	pVersion[1] = minor_ver;
	pVersion[2] = revision_ver;
	(*env)->ReleaseIntArrayElements(env, version, pVersion, 0);
	return RET_OK;
}
