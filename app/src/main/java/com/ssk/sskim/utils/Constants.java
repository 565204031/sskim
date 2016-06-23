package com.ssk.sskim.utils;

import java.io.File;

import android.os.Environment;

public class Constants {

	//所有的音频文件存放的目录
	public static final File AUDIO_DIR = new File(Environment.getExternalStorageDirectory()+"/sskim","audio");


	//所有的图片文件存放的目录
	public static final File IMG_DIR = new File(Environment.getExternalStorageDirectory()+"/sskim","img");
	
}
