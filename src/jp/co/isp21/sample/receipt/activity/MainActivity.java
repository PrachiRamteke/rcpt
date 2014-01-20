package jp.co.isp21.sample.receipt.activity;

import jp.co.isp21.sample.receipt.R;
import jp.co.isp21.sample.receipt.activity.camera.CameraActivity;
import jp.co.isp21.sample.receipt.data.ItemInfo;
import jp.co.isp21.sample.receipt.data.ReceiptInfo;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class MainActivity extends Activity {
	private static final int REQUESTCODE_CAMERA = 0;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        findViewById(R.id.btn_camera).setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
		        Intent intent = new Intent(getApplicationContext() ,  CameraActivity.class);
		        startActivityForResult( intent , REQUESTCODE_CAMERA );
			}
		});

        Intent intent = new Intent(getApplicationContext() ,  CameraActivity.class);
        startActivityForResult( intent , REQUESTCODE_CAMERA );
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data)
    {
    	if( requestCode == REQUESTCODE_CAMERA ){
    		if( resultCode == RESULT_OK ){
    			ReceiptInfo info = (ReceiptInfo)data.getSerializableExtra( ReceiptInfo.KEY );
    			int result = data.getIntExtra(CameraActivity.KEY_RESULT, 0);
				testResultView(info,result);
    		}else{
    			this.finish();
    		}
    	}
    }

    /* 邨先棡陦ｨ遉ｺ */
    private void testResultView(ReceiptInfo info , int result)
    {
    	StringBuffer sb = new StringBuffer();
        TextView text = (TextView)findViewById(R.id.TextView01);

        sb.append("隗｣譫千ｵ先棡+ result "+ "\n" +
        		+ info.getYear() + "/" + info.getMonth() + "/" + info.getDay() + " "
        		+ info.getHour() + ":" + info.getMin() + ":" + info.getSec()+ "\n");
        sb.append("髮ｻ隧ｱ逡ｪ蜿ｷ縲" + info.getTel() + "\n");
        sb.append("蜷郁ｨ･" + info.getTotal() + "\n\n");

        ItemInfo[] item = info.getItems();

        if( item != null ){
	        for(int i=0;i < item.length; i++){
	        	if( item[i].getName() != null ){
	                sb.append("item name" + (i + 1) + " ="+ item[i].getName() + " ");
	        	}else{
	        		sb.append("dont know" + (i + 1) + " what      ");
	        	}

	        	sb.append("price" + item[i].getPrice() + "\n");
	        }
        }
        text.setText(sb.toString());
	 }
}