/**
 * @file ReceiptAnalyticalEngine.h
 * @brief レシート解析エンジン
 */

#ifndef RECEIPT_ANALYTICAL_ENGINE_H_
#define RECEIPT_ANALYTICAL_ENGINE_H_

#ifdef __cplusplus
extern "C" {
#endif

#define ITEM_NAME_MAX       62      /**< 品目名最大値 */
#define ITEM_MAX            100     /**< 品目数最大値 */
#define TEL_MAX             20      /**< 電話番号最大値 */
#define CODE_MAX            32      /**< 製品コード最大値 */

/* レシート解析結果 */
#define RECEIPT_OK          0       /**< レシート解析正常 */
#define RECEIPT_ANALYZE_ERR -1      /**< レシート解析失敗 */
#define RECEIPT_OCR_ERR     -2      /**< OCR処理失敗 */
#define RECEIPT_MEM_ERR     -10     /**< メモリエラー */
#define RECEIPT_LOAD_ERR    -11     /**< ライブラリ読み込みエラー */
#define RECEIPT_SYS_ERR     -12     /**< システムエラー */

/* レシート解析結果詳細 */
#define NON_TOTAL           0x1     /**< 合計なし */
#define NON_DATE            0x2     /**< 日付なし */
#define NON_ITEM            0x4     /**< 品目なし */
#define ILLEGA_SUM          0x8     /**< 合計SUMチェック不正 */

/** @brief 日付情報構造体 */
typedef struct tagDateInfo {
    uint16_t year;              /**< 年 */
    uint16_t month;             /**< 月 */
    uint16_t day;               /**< 日 */
    uint16_t hour;              /**< 時 */
    uint16_t minute;            /**< 分 */
    uint16_t second;            /**< 秒 */
} DateInfo;

/** @brief 品目構造体 */
typedef struct tagItemInfo {
    char name[ITEM_NAME_MAX];   /**< 名称(最大32文字) */
    int price;                  /**< 金額 */
} ItemInfo;

/** @brief レシート解析構造体 */
typedef struct tagReceiptInfo {
    DateInfo date;              /**< 日付 */
    char tel[TEL_MAX];          /**< 電話番号 */
    int total;                  /**< 合計金額 */
    size_t cnt;                 /**< 品目数 */
    ItemInfo items[ITEM_MAX];   /**< 品目(nameが0文字は品目無し) */
} ReceiptInfo;

/**
 * レシート解析
 *
 * @param[in] img 画像データ(Y8)
 * @param[in] width 画像幅
 * @param[in] height 画像高さ
 * @param[out] receipt レシート解析結果
 * @return 処理結果(0以上:成功、0未満：失敗)
 */
int ReceiptAnalyze(char *img, int width, int height, ReceiptInfo *receipt);

/**
 * 長尺レシート解析
 * 初期化
 *
 * @return 常に0
 */
int InitLongReceipt(void);

/**
 * 長尺レシート解析
 * レシート分割解析
 *
 * @param[in] img 画像データ(Y8)
 * @param[in] width 画像幅
 * @param[in] height 画像高さ
 * @param[out] receipt レシート解析結果
 * @return 処理結果(0以上:成功、0未満：失敗)
 */
int AnalyzeDividedLongReceipt(char *img, int width, int height, ReceiptInfo *receipt);

/**
 * 長尺レシート解析
 * レシート分割解析結果設定
 *
 * @return 処理結果(0:成功、0未満：失敗)
 */
int SetDividedLongReceipt(void);

/**
 * 長尺レシート解析
 * 長尺レシート解析
 *
 * @param[out] receipt レシート解析結果
 * @return 処理結果(0以上:成功、0未満：失敗)
 */
int AnalyzeLongReceipt(ReceiptInfo *receipt);

/**
 * 長尺レシート解析
 * 長尺レシート解析中止
 *
 * @return 常に0
 */
int CancelLongReceipt(void);

/**
 * レシート解析
 * エンジン バージョン取得
 *
 * @param[out] major メジャーバージョン
 * @param[out] minor マイナーバージョン
 * @param[out] revision リビジョンバージョン
 */
void GetEngineVersion(int *major, int *minor, int *revision);

#ifdef __cplusplus
}
#endif

#endif /*  RECEIPT_ANALYTICAL_ENGINE_H_ */
