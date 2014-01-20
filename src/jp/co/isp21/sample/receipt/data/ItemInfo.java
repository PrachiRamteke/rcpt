package jp.co.isp21.sample.receipt.data;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

/**
 * 蜩∫岼繧ｯ繝ｩ繧ｹ�医Ξ繧ｷ繝ｼ繝郁ｧ｣譫辱NI繧､繝ｳ繧ｿ繝輔ぉ繝ｼ繧ｹ逕ｨ��
 */
public class ItemInfo implements Serializable {
	private static final long serialVersionUID = 0x5b4ee65ecb4139e8L;

	private byte[] sjisName;
	/**
	 * 蜩∫岼蜷�/ private int price; /** 驥鷹｡�
	 */
	private int price;

	public ItemInfo() {
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public String getName() {
		String name = null;

		if (this.sjisName == null) {
			return null;
		}
		try {
			name = new String(this.sjisName, 0, sjisName.length, "SJIS");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return name;
	}

	public void setName(String name) {
		byte[] bytes = null;

		if (name == null) {
			sjisName = null;
			return;
		}

		try {
			bytes = name.getBytes("SJIS");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		sjisName = bytes;
	}
}
