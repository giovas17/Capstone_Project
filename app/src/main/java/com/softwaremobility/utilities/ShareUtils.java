package com.softwaremobility.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;

import com.facebook.FacebookSdk;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.GridHolder;
import com.orhanobut.dialogplus.Holder;
import com.orhanobut.dialogplus.OnItemClickListener;
import com.softwaremobility.adapters.SimpleAdapter;
import com.softwaremobility.objects.Share;

import java.util.List;

public class ShareUtils {

    private Context context;
    private Activity activity;
    public static String FACEBOOK = "com.facebook.katana";
    public static String INSTAGRAM = "com.instagram.android";
    public static String TWITTER = "com.twitter";
    public static String PINTEREST = "com.pinterest";
    public static String WEIBO = "com.sina.weibo";
    public static String GOOGLE_PLUS = "com.google.android.apps.plus";
    public static String WECHAT = "com.tencent.mm";

    public ShareUtils(Context context, Activity activity){
        this.context = context;
        this.activity = activity;
    }

    public void newShareDialog(final String textToShare,@Nullable final Uri imageUriToShare, @Nullable final Uri contentToShare, @Nullable final Uri localImageUri, final List<Share> apps){
        Holder holder = new GridHolder(3);
        SimpleAdapter adapter = new SimpleAdapter(context,apps);
        final DialogPlus dialog = DialogPlus.newDialog(context)
                .setContentHolder(holder)
                .setCancelable(true)
                .setGravity(Gravity.BOTTOM)
                .setAdapter(adapter)
                .setOnClickListener(null)
                .setOnItemClickListener(new OnItemClickListener() {
                    @Override public void onItemClick(DialogPlus dialog, Object item, View view, int position) {
                        Share share = apps.get(position);
                        switch (share.getAplicacionName()) {
                            case "com.facebook.katana"://Facebook
                                setupFacebookShareIntent(textToShare,imageUriToShare,contentToShare);
                                break;
                            /*case "com.pinterest"://Pinterest;
                                Intent intent = new Intent(context,PinterestShare.class);
                                if(imageUriToShare != null) {
                                    intent.putExtra("imageUriToShare", String.valueOf(imageUriToShare));
                                }
                                if(contentToShare != null){
                                    intent.putExtra("contentToShare",String.valueOf(contentToShare));
                                }
                                intent.putExtra("textToShare",textToShare);
                                context.startActivity(intent);
                                break;*/
                            default:
                                Intent intent = getShareIntent(share.getAplicacionName(),localImageUri,contentToShare,textToShare);
                                context.startActivity(intent);
                                break;
                        }
                        dialog.dismiss();
                    }
                })
                .setExpanded(false)
                .create();
        dialog.show();
    }

    public void setupFacebookShareIntent(String name, Uri imageUriToShare,@Nullable Uri contentToShare) {
        ShareDialog shareDialog;
        FacebookSdk.sdkInitialize(context);
        shareDialog = new ShareDialog(activity);
        ShareLinkContent linkContent;
        if(contentToShare != null){
            linkContent = new ShareLinkContent.Builder()
                    .setContentTitle(name)
                    .setContentDescription(contentToShare.toString())
                    .setImageUrl(imageUriToShare)
                    .setContentUrl(imageUriToShare)
                    .build();
        }else{
             linkContent = new ShareLinkContent.Builder()
                    .setContentTitle(name)
                    .setContentDescription(name)
                    .setImageUrl(imageUriToShare)
                     .setContentUrl(imageUriToShare)
                     .build();
        }

        shareDialog.show(linkContent);
    }

    public Intent getShareIntent(String applicationName, @Nullable Uri localImageUri, @Nullable Uri contentToShare, String textToShare) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        boolean haveTheApp = false;
        if(localImageUri != null){
            intent.setType("image/*");
        }else{
            intent.setType("text/plain");
        }
        List<ResolveInfo> matches = context.getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo info : matches) {
            if (info.activityInfo.packageName.toLowerCase().contains(applicationName)) {
                intent.setPackage(info.activityInfo.packageName);
                intent.setClassName(info.activityInfo.packageName, info.activityInfo.name);
                if(contentToShare != null){
                    intent.putExtra(Intent.EXTRA_TEXT, textToShare + " #MoninApp" + "\n" + contentToShare.toString());
                }else{
                    intent.putExtra(Intent.EXTRA_TEXT, textToShare + " #MoninApp");
                }
                if(localImageUri != null){
                   intent.putExtra(Intent.EXTRA_STREAM, localImageUri);
               }
                haveTheApp = true;
                break;
            }
        }
        if(!haveTheApp){
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=" + applicationName));
        }
        return intent;
    }

}
