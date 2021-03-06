package com.example.advanceDemo.scene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;



import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageSwirlFilter;

import com.example.advanceDemo.VideoPlayerActivity;
import com.example.advanceDemo.view.PaintConstants;
import com.example.advanceDemo.view.ShowHeart;
import com.lansoeditor.demo.R;
import com.lansosdk.box.Animation;
import com.lansosdk.box.BitmapLayer;
import com.lansosdk.box.CanvasLayer;
import com.lansosdk.box.CanvasRunnable;
import com.lansosdk.box.DrawPad;
import com.lansosdk.box.DrawPadUpdateMode;
import com.lansosdk.box.MoveAnimation;
import com.lansosdk.box.ScaleAnimation;
import com.lansosdk.box.VideoLayer;
import com.lansosdk.box.ViewLayer;
import com.lansosdk.box.Layer;
import com.lansosdk.box.ViewLayerRelativeLayout;
import com.lansosdk.box.onDrawPadProgressListener;
import com.lansosdk.box.onDrawPadSizeChangedListener;
import com.lansosdk.videoeditor.CopyFileFromAssets;
import com.lansosdk.videoeditor.DrawPadView;
import com.lansosdk.videoeditor.MediaInfo;
import com.lansosdk.videoeditor.SDKDir;
import com.lansosdk.videoeditor.SDKFileUtils;
import com.lansosdk.videoeditor.VideoEditor;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Layout;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

/**
 * 采用自动刷新模式 
 *  前台转场.  
 *  先播放一个视频, 然后在10秒后,插入另一个视频.并增加进入动画.
 */
public class VideoLayerTransformActivity extends Activity{
    private static final String TAG = "VideoLayerTransformActivity";

    private String mVideoPath;
    private String videoPath2;

    private DrawPadView mDrawPad;
    
    private MediaPlayer mplayer=null;
    private MediaPlayer mplayer2=null;
    private MediaPlayer audioPlay=null;
    
    private BitmapLayer  bmpLayer=null;
    private CanvasLayer  canvasLayer=null;
    private VideoLayer  videoLayer1=null;
    private VideoLayer  videoLayer2=null;
    private MediaInfo  mInfo=null;
    private LinearLayout  playVideo;
    private String dstPath=null;
    private Context mContext;
    private String audioPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.videolay_transform_layout);
        
        mContext=getApplicationContext();
        
        initView();
        
        mVideoPath = getIntent().getStringExtra("videopath");
        mDrawPad = (DrawPadView) findViewById(R.id.id_videolayer_drawpad);
        
        //在手机的默认路径下创建一个文件名,用来保存生成的视频文件,(在onDestroy中删除)
        dstPath=SDKFileUtils.newMp4PathInBox();
	    
        audioPath=CopyFileFromAssets.copyAssets(mContext, "bgMusic20s.m4a");
        new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				 startPlayVideo();
			}
		}, 500);
        new Thread(new Runnable() {
			
			@Override
			public void run() {
				videoPath2=CopyFileFromAssets.copyAssets(getApplicationContext(), "ping25s.mp4");
			}
		}).start();
    }
    private void startPlayVideo()
    {
    		
    	mInfo=new MediaInfo(mVideoPath, false);
    	if (mInfo.prepare())
    	{

    		mplayer=new MediaPlayer();
        	  try {
				mplayer.setDataSource(mVideoPath);
			}  catch (IOException e) {
				e.printStackTrace();
			}
        	  mplayer.setOnPreparedListener(new OnPreparedListener() {
				
				@Override
				public void onPrepared(MediaPlayer mp) {
					// TODO Auto-generated method stub
					initDrawPad();
				}
			});
        	  mplayer.prepareAsync();
          
    	}else {
              Log.e(TAG, "Null Data Source\n");
              finish();
              return;
    	}
    }
    //Step1: 设置DrawPad 容器的尺寸.并设置是否实时录制容器上的内容.
    private void initDrawPad()
    {
    	/**
    	 * 设置录制的参数.
    	 */
    		mDrawPad.setRealEncodeEnable(480,480,1000000,(int)25,dstPath);
    		
    		mDrawPad.setUpdateMode(DrawPadUpdateMode.AUTO_FLUSH, 25);//25是帧率.
    		
        	mDrawPad.setDrawPadSize(480,480,new onDrawPadSizeChangedListener() {
    			
    			@Override
    			public void onSizeChanged(int viewWidth, int viewHeight) {
    				startDrawPad();
    			}
    		});
        	mDrawPad.setOnDrawPadProgressListener(new onDrawPadProgressListener() {
				
				@Override
				public void onProgress(DrawPad v, long currentTimeUs) {
					
					if(currentTimeUs>18*1000*1000){  //18秒的时候停止.
						stopDrawPad();
					}
					if(currentTimeUs>15*1000*1000){
						showFourLayer();
					}
					if(currentTimeUs>3*1000*1000 && bmpLayer==null){  //3秒的时候, 增加图片.
						showSecondLayer(currentTimeUs);
					}
					if(currentTimeUs>8*1000*1000 && videoLayer2==null){  //8秒的时候增加一个视频.
						showThreeLayer(currentTimeUs);
					}
				}
			});
    }
    /**
     * Step2: Drawpad设置好后, 开始容器线程运行,并增加一个ViewLayer图层
     */
    private void startDrawPad()
    {
    	if(mDrawPad.startDrawPad())
    	{
    		//增加一个背景
    		BitmapLayer layer=mDrawPad.addBitmapLayer(BitmapFactory.decodeResource(getResources(), R.drawable.pad_bg));
    		layer.setScaledValue(layer.getPadWidth(), layer.getPadHeight()); //让背景铺满整个容器.
    		
        	videoLayer1=mDrawPad.addMainVideoLayer(mplayer.getVideoWidth(),mplayer.getVideoHeight(),null);
    		if(videoLayer1!=null)
    		{
    			mplayer.setSurface(new Surface(videoLayer1.getVideoTexture()));
    		}
    		mplayer.setVolume(0.0f, 0.0f);  //禁止音量.
    		mplayer.start();
    		
    		playAudio();
        }
    }
    /**
     * Step3: 做好后, 停止容器, 因为容器里没有声音, 这里增加上原来的声音.
     */
    private void stopDrawPad()
    {
    	if(mDrawPad!=null && mDrawPad.isRunning()){
			mDrawPad.stopDrawPad();
			toastStop();
			
			if(SDKFileUtils.fileExist(dstPath)){
				playVideo.setVisibility(View.VISIBLE);
			}
			if(audioPlay!=null){
				audioPlay.stop();
				audioPlay.release();
				audioPlay=null;
			}
			if(mplayer!=null){
	    		mplayer.stop();
	    		mplayer.release();
	    		mplayer=null;
	    	}
	    	if(mplayer2!=null){
	    		mplayer2.stop();
	    		mplayer2.release();
	    		mplayer2=null;
	    	}
		}
    }
    /**
     * 增加图片
     */
    private void addBitmapLayer(long currentTimeUs)
	{
		String bmpPath=CopyFileFromAssets.copyAssets(getApplicationContext(), "girl.jpg");
		Bitmap bmp=BitmapFactory.decodeFile(bmpPath);
		bmpLayer=mDrawPad.addBitmapLayer(bmp, null);
		bmpLayer.setVisibility(Layer.INVISIBLE);
		ScaleAnimation	scaleAnim=new ScaleAnimation(currentTimeUs+1000*1000,2*1000*1000,0.0f, 1.2f);
		bmpLayer.addAnimation(scaleAnim);
	}
    /**
     * 增加canvas图层.
     */
    private void addCanvasLayer()
    {
    	canvasLayer=mDrawPad.addCanvasLayer();
		if(canvasLayer!=null)
		{
				canvasLayer.addCanvasRunnable(new CanvasRunnable() {
					@Override
					public void onDrawCanvas(CanvasLayer layer, Canvas canvas,
							long currentTimeUs) {
							Paint paint = new Paint();
			                paint.setColor(Color.RED);
		         			paint.setAntiAlias(true);
		         			paint.setTextSize(50);
		         			canvas.drawColor(Color.YELLOW); //背景设置为黄色.
		         			canvas.drawText("蓝松短视频演示之【转场】",20,canvasLayer.getPadHeight()/2, paint);
					}
				});
		}
    }
    private int rectFactor=0;
    /**
     * 停止第一个图层, 并开启第二个图层.
     */
    private void showSecondLayer(long currentTimeUs)
    {
    	if(videoLayer1!=null){
    		if(rectFactor>100)  //等到100时,结束动画, 删除视频图层,并增加图片图层.
    		{  //停止.
    			mDrawPad.removeLayer(videoLayer1);
    			videoLayer1=null;
    			if(mplayer!=null){
    	    		mplayer.stop();
    	    		mplayer.release();
    	    		mplayer=null;
    	    	}
    			rectFactor=0;
    			addBitmapLayer(currentTimeUs);
    		}else{  //有个动画效果
        		float rect= (100-rectFactor);  //因为java的小数点不是很精确, 这里用整数表示
        		rectFactor  =rectFactor + 5;
        		rect/=2;
        		rect/=100;//再次转换为0--1.0的范围
        		videoLayer1.setVisibleRect(0.5f -rect , 0.5f +rect, 0.0f,1.0f);
    		}
    	}
    }
    
    /**
     * 停止第二个图层,开启第三个图层
     * @param currentTimeUs
     */
    private void showThreeLayer(long currentTimeUs)
    {
    	if(bmpLayer!=null){
    		if(rectFactor>100) {
    			mDrawPad.removeLayer(bmpLayer);
    			bmpLayer=null;
    			addOtherVideoLayer(currentTimeUs);
    			rectFactor=0;
    		}else{  //淡淡的消失.
    			float rect= (100-rectFactor);  //因为java的小数点不是很精确, 这里用整数表示
    			rect/=100f;  //转换为0--1.0
    			
        		bmpLayer.setAlphaPercent(rect);
        		bmpLayer.setRedPercent(rect);
        		bmpLayer.setGreenPercent(rect);
        		bmpLayer.setBluePercent(rect);
        		rectFactor  =rectFactor + 5;
    		}
    	}
    }
    private GPUImageSwirlFilter swirlFilter=null;
    private void showFourLayer()
    {
    	if(videoLayer2!=null){
    		if(rectFactor>120) {
    			mDrawPad.removeLayer(videoLayer2);
    			videoLayer2=null;
    			rectFactor=0;
    			addCanvasLayer();
    		}else{  //增加滤镜
    			if(mplayer2!=null){
    				mplayer2.pause();	  //画面暂停.
    			}
    			
    			float rect= (float)rectFactor;  //因为java的小数点不是很精确, 这里用整数表示
    			rect/=100f;  //转换为0--1.0
    			
    			if(swirlFilter==null){
    				swirlFilter=new GPUImageSwirlFilter();
    				videoLayer2.switchFilterTo(swirlFilter);
    			}
    			swirlFilter.setAngle(rect);
    			swirlFilter.setRadius(1.0f);  //设置半径是整个纹理.
        		rectFactor  =rectFactor + 5;
    		}
    	}
    }
    private void addOtherVideoLayer(final long currentTimeUs)
    {
	    	  if(videoPath2==null){
	    		   videoPath2=CopyFileFromAssets.copyAssets(getApplicationContext(), "ping25s.mp4");
	    	  }
        	  mplayer2=new MediaPlayer();
        	  try {
				
        		  mplayer2.setDataSource(videoPath2);
        		  mplayer2.setVolume(0.0f, 0.0f);  //不要声音.
				
        	  }  catch (IOException e) {
				e.printStackTrace();
        	  }
        	  mplayer2.setOnPreparedListener(new OnPreparedListener() {
				
				@Override
				public void onPrepared(MediaPlayer mp) {
					if(mDrawPad!=null && mDrawPad.isRunning()){
						videoLayer2=mDrawPad.addVideoLayer(mplayer2.getVideoWidth(), mplayer2.getVideoHeight(),null);
						if(videoLayer2!=null)
						{
							mplayer2.setSurface(new Surface(videoLayer2.getVideoTexture()));
							
							Animation move=new MoveAnimation(currentTimeUs+1000*1000, 1*1000*1000, 0, 0, videoLayer2.getPadWidth()/2, videoLayer2.getPadHeight()/2);
							Animation scale=new ScaleAnimation(currentTimeUs+1000*1000, 1*1000*1000,0.0f,1.0f);
							videoLayer2.addAnimation(move);
							videoLayer2.addAnimation(scale);
							videoLayer2.setVisibility(Layer.INVISIBLE);
						}
						mplayer2.start();
					}
				}
        	  });
        	mplayer2.prepareAsync();
    }
    private void playAudio()
    {
    	audioPlay=new MediaPlayer();
    	try {
			audioPlay.setDataSource(audioPath);
			audioPlay.prepare();
	    	audioPlay.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    private void initView()
    {
    	  playVideo=(LinearLayout)findViewById(R.id.id_videoLayer_saveplay);
    	  playVideo.setOnClickListener(new OnClickListener() {
  			
  			@Override
  			public void onClick(View v) {
  				 if(SDKFileUtils.fileExist(dstPath)){
  		   			 	Intent intent=new Intent(VideoLayerTransformActivity.this,VideoPlayerActivity.class);
  		   			 	String str=VideoEditor.mp4AddAudio(dstPath, audioPath);
  		   			 	intent.putExtra("videopath", str);
  		   			 	startActivity(intent);
  		   		 }else{
  		   			 Toast.makeText(VideoLayerTransformActivity.this, "目标文件不存在", Toast.LENGTH_SHORT).show();
  		   		 }
  			}
  		});
    	  playVideo.setVisibility(View.GONE);
    }
    private void toastStop()
    {
    	Toast.makeText(getApplicationContext(), "录制已停止!!", Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	if(mplayer!=null){
    		mplayer.stop();
    		mplayer.release();
    		mplayer=null;
    	}
    	if(mplayer2!=null){
    		mplayer2.stop();
    		mplayer2.release();
    		mplayer2=null;
    	}
    	if(mDrawPad!=null){
    		mDrawPad.stopDrawPad();
    		mDrawPad=null;        		   
    	}
    	if(audioPlay!=null){
			audioPlay.stop();
			audioPlay.release();
			audioPlay=null;
		}
    	SDKFileUtils.deleteFile(dstPath);
    }
}
