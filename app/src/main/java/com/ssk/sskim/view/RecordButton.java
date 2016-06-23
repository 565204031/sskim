package com.ssk.sskim.view;

import java.io.File;

import android.app.Dialog;
import android.content.Context;
import android.media.MediaRecorder;
import android.media.MediaRecorder.AudioSource;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.TableRow.LayoutParams;

import com.ssk.sskim.R;
import com.ssk.sskim.utils.Constants;
import com.ssk.sskim.utils.UUIDUtils;

public class RecordButton extends Button {

	private Dialog dialog;
	private MediaRecorder recorder;
	private File audioFile;
	private ImageView imageView;
	
	private int[] volumeImgs = {
			R.mipmap.ease_record_animate_01,
			R.mipmap.ease_record_animate_02,
			R.mipmap.ease_record_animate_03,
			R.mipmap.ease_record_animate_04,
			R.mipmap.ease_record_animate_05,
			R.mipmap.ease_record_animate_06,
			R.mipmap.ease_record_animate_07,
			R.mipmap.ease_record_animate_08,
	};
	private MicVolumnPicker micVolumnPicker;
	private OnRecordFinishedListener onRecordFinishedListener; 
	
	private long startTime;
	
	public RecordButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			//更新音量图片
			int index=msg.what;
			if(msg.what<0){
				index=0;
			}else if(msg.what>7){
				index=7;
			}
			imageView.setImageResource(volumeImgs[index]);
		}
	};
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		//按下，开始录音
		case MotionEvent.ACTION_DOWN:
			startRecord();
			break;
		//松开，停止录音
		case MotionEvent.ACTION_UP:
			stopRecord();
			break;
		//取消，手指移开了按钮
		case MotionEvent.ACTION_CANCEL:
			stopRecord();
			break;
		default:
			break;
		}
		return true;
	}
	/**
	 * 停止录音
	 */
	private void stopRecord() {
		//关闭对话框
		if (dialog != null) {
			dialog.dismiss();
		}
		//停止拾取麦克风音量（此操作应在停止录音之前）
		if (micVolumnPicker != null) {
			micVolumnPicker.setRunning(false);
		}
		//停止录音
		if (recorder != null) {
			recorder.stop();
			recorder.release();
			recorder = null;
			
			//判断录音的时长是否太短
			if (System.currentTimeMillis() - startTime < 1000) {
				Toast.makeText(getContext(), "录音的时间太短！", Toast.LENGTH_SHORT).show();
				//删除录音文件
				if (audioFile.exists()) {
					audioFile.delete();
				}
				return;
			}
		}
		
		//录音完成
		if (onRecordFinishedListener != null) {
			onRecordFinishedListener.onFinished(audioFile,(int)(System.currentTimeMillis() - startTime));
		}
		
	}

	/**
	 * 开始录音
	 */
	private void startRecord() {
		//开始录制的时间
		startTime = System.currentTimeMillis();
		
		//弹出对话框
		dialog = new Dialog(getContext(), R.style.like_toast_dialog_style);
		imageView = new ImageView(getContext());
		imageView.setImageResource(R.mipmap.ease_record_animate_04);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		//居中
		params.gravity = Gravity.CENTER;
		//将图片添加到对话框显示
		dialog.addContentView(imageView, params);
		
		//开始使用MediaRecorder录音
		recorder = new MediaRecorder();
		//音源
		recorder.setAudioSource(AudioSource.MIC);
		//文件格式 amr
		recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
		//编码格式
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		
		//输出文件的路径
		if (!Constants.AUDIO_DIR.exists()) {
			Constants.AUDIO_DIR.mkdirs();
		}
		audioFile = new File(Constants.AUDIO_DIR, UUIDUtils.generate()+".amr");
		recorder.setOutputFile(audioFile.getAbsolutePath());
		//缓冲
		try {
			recorder.prepare();
			recorder.start();

			dialog.show();
			
			//启动拾取麦克风音量的线程
			micVolumnPicker = new MicVolumnPicker();
			micVolumnPicker.start();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	/**
	 * 开始录音之后，一个不断拾取麦克风音量的线程
	 * @author Jason
	 * QQ: 2904048107
	 * @date 2015年8月12日
	 * @version 1.0
	 */
	class MicVolumnPicker extends Thread{
		private boolean isRunning = true;
		
		@Override
		public void run() {
			while(isRunning){
				SystemClock.sleep(200);
				if (recorder == null) {
					return;
				}
				//根据分贝大小计算得到一个音量的相对值
				//根据这个相对值，不断替换显示的图片
				int x = recorder.getMaxAmplitude();
				if (x != 0) {
					int f =  (int) (10 * Math.log(x) / Math.log(10)) - 32;
					handler.sendEmptyMessage(f);
				}
				
			}
		}
		
		public void setRunning(boolean isRunning) {
			this.isRunning = isRunning;
		}
	}

	/**
	 * 此监听用户监听录音完成
	 * @author Jason
	 * QQ: 2904048107
	 * @date 2015年8月12日
	 * @version 1.0
	 */
	public interface OnRecordFinishedListener{
		/**
		 * 录音完成之后的回调方法
		 * @param audioFile 录制的音频文件
		 * @param duration 音频的时长，毫秒数
		 */
		void onFinished(File audioFile, int duration);
	}
	
	public void setOnRecordFinishedListener(OnRecordFinishedListener onRecordFinishedListener) {
		this.onRecordFinishedListener = onRecordFinishedListener;
	}
}
