package com.ssk.sskim.utils;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * 上传图片
 * @author 杀死凯 QQ565204031
 * @creation 2015-11-9
 */
public class PictureUploading {

	private static final int MEDIASTORE=100;
	private static final int IMAGE=200;
	private static final int TAILOR=300;
	public static Intent intent;
	public static int index=0;
	public static void showSelect(final Activity at,int _index) {
		index=_index;
		new AlertDialog.Builder(at)
				.setItems(new String[] { "拍照", "图片" }, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if(which==0){
							//拍照
							intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
							intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
							at.startActivityForResult(intent, MEDIASTORE);
						}else if(which==1){
							intent = new Intent(Intent.ACTION_GET_CONTENT);
							intent.setType("image/*");
							at.startActivityForResult(intent, IMAGE);
						}
					}
				})
				.show();
	}
	public static void onActivityResult(final Activity at,OnBackCall listen,int q, int r, Intent data){
		if(r==at.RESULT_OK){
			if(q==MEDIASTORE){
				//拍照
				  Uri mImageCaptureUri = intent.getData();  
	                //返回的Uri不为空时，那么图片信息数据都会在Uri中获得。如果为空，那么我们就进行下面的方式获取  
	                if (mImageCaptureUri != null) {  
	                	  intent=getImageClipIntent(mImageCaptureUri);  
	                	  at.startActivityForResult(intent, TAILOR);  
	                } else {  
	                    Bundle extras = data.getExtras();  
	                    if (extras != null) {  
	                        //这里是有些拍照后的图片是直接存放到Bundle中的所以我们可以从这里面获取Bitmap图片  
	                        Bitmap image = extras.getParcelable("data");  
	                        if (image != null) {  
	                        	listen.onSucceed(image,index);
	                        }else{
	                        	listen.onFail();
	                        }  
	                        
	                    }  
	                }  
		         
			}else if(q==IMAGE){
				//图片
				  intent=getImageClipIntent(data.getData());  
				  at.startActivityForResult(intent, TAILOR);  
			}else if(q==TAILOR){
			   //裁剪
				Bitmap bitmap=data.getParcelableExtra("data");
				if(bitmap==null){
					try {
						bitmap = MediaStore.Images.Media.getBitmap(at.getContentResolver(), intent.getData());
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (bitmap != null) {
					listen.onSucceed(bitmap,index);
				} else {
					listen.onFail();
				}
			}
			
		}
	}
	/*
	    * 获取剪切后的图片 
	    */  
	   public static Intent getImageClipIntent(Uri uri) {  
//		   intent = new Intent(Intent.ACTION_GET_CONTENT, null); 
		   intent = new Intent("com.android.camera.action.CROP");
		   intent.setDataAndType(uri, "image/*");
//	       intent.setType("image/*");  
	       intent.putExtra("crop", "true");   
	       intent.putExtra("aspectX", 1);//裁剪框比例  
	       intent.putExtra("aspectY", 1);  
	       intent.putExtra("outputX", 500);//输出图片大小
	       intent.putExtra("outputY", 500);
	       intent.putExtra("return-data", true);  
	       return intent;  
	   } 
	   public interface OnBackCall
	   {
		    void onSucceed(Bitmap bmp, int index);
		    void onFail();
	   }
	   
}
