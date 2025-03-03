package com.example.fluttershare;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import java.io.File;
import java.util.ArrayList;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.BinaryMessenger;

/** FlutterSharePlugin */
public class FlutterSharePlugin implements FlutterPlugin, MethodCallHandler {
    private Context context;
    private MethodChannel methodChannel;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        context = binding.getApplicationContext();
        methodChannel = new MethodChannel(binding.getBinaryMessenger(), "flutter_share");
        methodChannel.setMethodCallHandler(this);
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        methodChannel.setMethodCallHandler(null);
        methodChannel = null;
        context = null;
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        if (call.method.equals("shareFile")) {
            shareFile(call, result);
        } else if (call.method.equals("share")) {
            share(call, result);
        } else {
            result.notImplemented();
        }
    }

    private void share(MethodCall call, Result result) {
        try {
            String title = call.argument("title");
            String text = call.argument("text");
            String linkUrl = call.argument("linkUrl");
            String chooserTitle = call.argument("chooserTitle");

            if (title == null || title.isEmpty()) {
                Log.e("FlutterShare", "Title cannot be null or empty");
                result.error("ERROR", "Title cannot be null or empty", null);
                return;
            }

            ArrayList<String> extraTextList = new ArrayList<>();
            if (text != null && !text.isEmpty()) extraTextList.add(text);
            if (linkUrl != null && !linkUrl.isEmpty()) extraTextList.add(linkUrl);

            String extraText = extraTextList.isEmpty() ? "" : TextUtils.join("\n\n", extraTextList);

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, title);
            intent.putExtra(Intent.EXTRA_TEXT, extraText);

            Intent chooserIntent = Intent.createChooser(intent, chooserTitle);
            chooserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(chooserIntent);

            result.success(true);
        } catch (Exception ex) {
            Log.e("FlutterShare", "Error sharing text", ex);
            result.error("ERROR", ex.getMessage(), null);
        }
    }

    private void shareFile(MethodCall call, Result result) {
        try {
            String title = call.argument("title");
            String text = call.argument("text");
            String filePath = call.argument("filePath");
            String fileType = call.argument("fileType");
            String chooserTitle = call.argument("chooserTitle");

            if (filePath == null || filePath.isEmpty()) {
                Log.e("FlutterShare", "FilePath cannot be null or empty");
                result.error("ERROR", "FilePath cannot be null or empty", null);
                return;
            }

            File file = new File(filePath);
            Uri fileUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setType(fileType);
            intent.putExtra(Intent.EXTRA_SUBJECT, title);
            intent.putExtra(Intent.EXTRA_TEXT, text);
            intent.putExtra(Intent.EXTRA_STREAM, fileUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Intent chooserIntent = Intent.createChooser(intent, chooserTitle);
            chooserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(chooserIntent);

            result.success(true);
        } catch (Exception ex) {
            Log.e("FlutterShare", "Error sharing file", ex);
            result.error("ERROR", ex.getMessage(), null);
        }
    }
}
