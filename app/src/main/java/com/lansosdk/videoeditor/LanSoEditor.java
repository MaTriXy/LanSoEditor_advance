package com.lansosdk.videoeditor;

import jp.co.cyberagent.lansongsdk.gpuimage.LanSongBeautyAdvanceFilter;

import com.lansosdk.box.LanSoEditorBox;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Build;
import android.support.v4.content.PermissionChecker;
import android.util.Log;


public class LanSoEditor {

	  public static void initSDK(Context context,String str)
	  {
		  LoadLanSongSdk.loadLibraries();  //拿出来单独加载库文件.
		  LanSoEditor.initSo(context,str);
	  }
	  
	  /**
	   * 为了统一, 这里请不要调用, 直接调用initSDK即可.
	   * @param context
	   * @param str
	   */
	  @Deprecated
	  public static void initSo(Context context,String str)
	  {
	    	    nativeInit(context,context.getAssets(),str);
	    	    LanSoEditorBox.init(); 
	  }
	    
	  public static void unInitSo()
	  {
	    		nativeUninit();
	  }
	  /**
	   * 设置默认产生文件的文件夹, 默认是:/sdcard/lansongBox/
	   * 如果您要设置, 则需要改文件夹存在.
	   * 比如可以是:
	   * @param tmpDir
	   */
	  public static void setTempFileDir(String tmpDir)
	  {
		  LanSoEditorBox.setTempFileDir(tmpDir);
		  SDKDir.TMP_DIR=tmpDir;
	  }
	  
	  /**
	   * 获取当前cpu的性能, 我们是根据市面上流行的cpu型号做的一一测试,得到的结果值. 如果在这个是0,则认为CPU的处理速度还可以.
	   * 如果是-1,则一些复杂的, 比如{@link LanSongBeautyAdvanceFilter}这样的操作, 会有点卡顿;比如后台处理可能耗时较长.
	   * 如果是-2 则认为cpu性能很低, 基本不能做美颜磨皮操作, 会很卡顿, 后台处理耗时会更长.
	   * 
	   * 可能比较偏门或3年前的cpu可能没有测试过,请注意.
	   * @return
	   */
	  public static int getCPULevel()
	  {
		  return LanSoEditorBox.getCPULevel();
	  }
	  
	    public static native void nativeInit(Context ctx,AssetManager ass,String filename);
	    public static native void nativeUninit();
	    
}
