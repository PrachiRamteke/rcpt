package jp.co.isp21.sample.receipt.data;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

/**
 * レシートクラス（レシート解析JNIインタフェース用）
 */
public class ReceiptInfo implements Serializable
{
	private static final long serialVersionUID = 181333264215588640L;

	public static final String KEY = ReceiptInfo.class.getName();

	private short year;			/** 年 */
	private short month;		/** 月 */
	private short day;			/** 日 */
	private short hour;			/** 時 */
	private short min;			/** 分 */
	private short sec;			/** 秒 */

	private byte[] tel;			/** 電話番号 */
	private int total;			/** 合計 */
	private ItemInfo[] items;	/** 品目 */

	public ReceiptInfo(){
	}

	/** 年 */
	public void setYear(short year) {
		this.year = year;
	}
	public short getYear() {
		return year;
	}

	/** 月 */
	public void setMonth(short month) {
		this.month = month;
	}
	public short getMonth() {
		return month;
	}

	/** 日 */
	public void setDay(short day) {
		this.day = day;
	}
	public short getDay() {
		return day;
	}

	/**
	 * @return hour
	 */
	public short getHour()
	{
		return hour;
	}

	/**
	 * @param hour セットする hour
	 */
	public void setHour(short hour)
	{
		this.hour = hour;
	}

	/**
	 * @return min
	 */
	public short getMin()
	{
		return min;
	}

	/**
	 * @param min セットする min
	 */
	public void setMin(short min)
	{
		this.min = min;
	}

	/** 秒 */
	public void setSec(short sec) {
		this.sec = sec;
	}

	public short getSec() {
		return sec;
	}

	/**
	 * @return tel
	 */
	public String getTel()
	{
		String tel = null;
		if( this.tel == null ){
			return null;
		}
		try {
			tel = new String(this.tel, 0, this.tel.length, "SJIS");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return tel;
	}

	/**
	 * @param tel セットする tel
	 */
	public void setTel(String tel)
	{
		byte[] bytes = null;

		if( tel == null ){
			this.tel = null;
			return;
		}

		try {
			bytes = tel.getBytes("SJIS");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		this.tel = bytes;
	}

	/** 合計 */
	public void setTotal(int total) {
		this.total = total;
	}
	public int getTotal() {
		return total;
	}

	/** 品目 */
	public void setItems(ItemInfo[] items) {
		this.items = items;
	}
	public ItemInfo[] getItems() {
		return items;
	}
}
