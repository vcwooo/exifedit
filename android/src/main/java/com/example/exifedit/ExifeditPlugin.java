package com.example.exifedit;

import android.media.ExifInterface;

import androidx.annotation.NonNull;

import java.lang.reflect.Field;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/** ExifeditPlugin */
public class ExifeditPlugin implements FlutterPlugin, MethodCallHandler {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel channel;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "exifedit");
    channel.setMethodCallHandler(this);
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    if (call.method.equals("getPlatformVersion")) {
      result.success("Android " + android.os.Build.VERSION.RELEASE);
    } else if (call.method.equals("setExif")) {
      setExif(call,result);
    } else if (call.method.equals("getExif")) {
      getExif(call, result);
    } else {
      result.notImplemented();
    }
  }

  public void setExif(MethodCall call, Result result) {
    String filepath = call.argument("path");
    Map<String,String> map = call.argument("exif");
    try {
      ExifInterface exif = new ExifInterface(filepath); // 根据图片的路径获取图片的Exif
      for(String key:map.keySet()){
        Field staticfield = ExifInterface.class.getDeclaredField(key);
        exif.setAttribute(staticfield.get(null).toString(), map.get(key));
      }
      // 把纬度写进MODEL
      exif.saveAttributes();
      result.success(null); // 最后保存起来
    } catch (Exception e) {
      result.error("error", "IOexception", e);
      e.printStackTrace();
    }
  }


  public void getExif(MethodCall call, Result result) {
    String filepath = call.argument("path");
    String key = call.argument("key");
    try {
      ExifInterface exif = new ExifInterface(filepath);
      Field staticfield = ExifInterface.class.getDeclaredField(key);
      String value = exif.getAttribute(staticfield.get(null).toString());
      result.success(value);
    } catch (Exception e) {
      result.error("error", "IOexception", null);
      e.printStackTrace();
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }
}
