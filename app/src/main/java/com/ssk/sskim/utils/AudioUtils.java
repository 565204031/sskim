package com.ssk.sskim.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

public class AudioUtils {

	
	/**
	 * 播放一个音频文件
	 * @param path
	 * @param context
	 */
	public static void play(String path,Context context){
		final SoundPool sp = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
		//音频管理器
		final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		
		//加载音频，优先权
		final int soundID = sp.load(path, 1);
		//加载完成的监听 
		sp.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
			@Override
			public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
				//播放
				//多媒体音量
				int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
				//音量、优先级、循环次数、速率
				sp.play(soundID, volume, volume, 1, 0, 1);
			}
		});
	}
	
}
