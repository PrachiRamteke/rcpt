package jp.co.isp21.sample.receipt.util;

import java.util.Timer;
import java.util.TimerTask;

import android.os.Handler;

public class TimerTaskManager
{
	private Handler mHandler;
	private TimerTaskListener mListener;
	private Timer mTimer;

	public interface TimerTaskListener
	{
		public void run();
	}

	public TimerTaskManager(TimerTaskListener listener)
	{
		mHandler = new Handler();
		mListener = listener;
		mTimer = null;
	}

	public void startTimer(long delay)
	{
		MyTask task = new MyTask();
		mTimer = new Timer(false);
		mTimer.schedule(task, delay);
	}

	public void startTimer(long delay, long period)
	{
		MyTask task = new MyTask();
		mTimer = new Timer(false);
    	mTimer.schedule(task, delay , period);

	}

	public void killTimer()
	{
		if( mTimer != null ){
			mTimer.cancel();
			mTimer = null;
		}
	}

	public boolean isTimer()
	{
		if( mTimer != null ){
			return true;
		}else{
			return false;
		}
	}

	private class MyTask extends TimerTask
	{
		public void run()
		{
			mHandler.post( new Runnable()
			{
				public void run()
				{
					mListener.run();
				}
			});
		}
	}
}
