package com.manvir.SnapColors;
// Please don't decompile my code if you want help please ask on the thread thanks =).
import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import java.io.File;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.XModuleResources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import org.apache.commons.io.FileUtils;
import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

@SuppressWarnings("UnusedDeclaration")
public class App implements IXposedHookLoadPackage, IXposedHookZygoteInit, IXposedHookInitPackageResources {
    static final String TAG = "SnapColors";
    static String MODULE_PATH;
    static String SnapChatPKG = "com.snapchat.android";
	static XSharedPreferences prefs;
	static Activity SnapChatContext;
	static Typeface defTypeFace;
	static boolean notFirstRun = false;
	static boolean DEBUG = true;
	static Random random = new Random();
    EditText editText;
    public static XModuleResources modRes;

    private void random(EditText textBox) {
        int colorBG = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
        int colorText = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
        textBox.setBackgroundColor(colorBG);
        textBox.setTextColor(colorText);
    }
	
	public void log(String text){
		if(DEBUG){
			XposedBridge.log(TAG + ": " + text);
		}
	}

    //For converting px's to dpi
    private int px(float dips)
    {
        float DP = SnapChatContext.getResources().getDisplayMetrics().density;
        return Math.round(dips * DP);
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        MODULE_PATH = startupParam.modulePath;
        prefs = new XSharedPreferences("com.manvir.SnapColors", "settings");
    }

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) throws Throwable {
        if (!resparam.packageName.equals(SnapChatPKG))
            return;
        modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);

        resparam.res.hookLayout(SnapChatPKG, "layout", "snap_preview", new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
                //Get Snapchats main layout.
                RelativeLayout layout = (RelativeLayout) liparam.view.findViewById(liparam.res.getIdentifier("snap_preview_relative_layout","id",SnapChatPKG));
                //Params for the "T" that shows tha main dialog when tapped.
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(liparam.view.findViewById(liparam.res.getIdentifier("drawing_btn","id",SnapChatPKG)).getLayoutParams());
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                params.topMargin = px(7);
                params.rightMargin = px(110);
                //The "T" that shows the options.
                ImageButton SnapColorsBtn = new ImageButton(SnapChatContext);
                SnapColorsBtn.setBackgroundColor(Color.TRANSPARENT);
                SnapColorsBtn.setImageDrawable(modRes.getDrawable(R.drawable.snapcolorsbtn));

                //Get the display params for our layout.
                Display display = SnapChatContext.getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                param.width = size.x;
                param.topMargin = px(70);
                //end of get display params.

                //Setup our layout here and add the views, buttons etc.
                final RelativeLayout ly = new RelativeLayout(SnapChatContext);
                ly.setBackgroundDrawable(modRes.getDrawable(R.drawable.bgviewdraw));
                ly.setVisibility(View.GONE);

                SButton btnRandomize = new SButton(SnapChatContext, R.drawable.randomize_btn, ly, 50, 70);
                btnRandomize.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Random random = new Random();
                        int colorBG = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
                        int colorText = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
                        editText.setBackgroundColor(colorBG);
                        editText.setTextColor(colorText);
                    }
                });

                SButton btnTextColor = new SButton(SnapChatContext, R.drawable.textcolor_btn, ly, 50, 200);
                btnTextColor.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        ColorPickerDialog colorPickerDialog = new ColorPickerDialog(SnapChatContext, Color.WHITE, new ColorPickerDialog.OnColorSelectedListener() {
                            @Override
                            public void onColorSelected(int color) {
                                editText.setTextColor(color);
                            }
                        });
                        colorPickerDialog.setButton( Dialog.BUTTON_NEUTRAL, "Default", new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int which) {
                                editText.setTextColor(Color.WHITE);
                            }
                        });
                        colorPickerDialog.setTitle("Text Color");
                        colorPickerDialog.show();
                    }
                });

                SButton btnBgColor = new SButton(SnapChatContext, R.drawable.bgcolor_btn, ly, 50, 330);
                btnBgColor.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        ColorPickerDialog colorPickerDialog = new ColorPickerDialog(SnapChatContext, Color.WHITE, new ColorPickerDialog.OnColorSelectedListener() {
                            @Override
                            public void onColorSelected(int color) {
                                editText.setBackgroundColor(color);
                            }
                        });
                        colorPickerDialog.setButton( Dialog.BUTTON_NEUTRAL, "Default", new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int which) {
                                editText.setBackgroundColor(-1728053248);
                            }
                        });
                        colorPickerDialog.setTitle("Background Color");
                        colorPickerDialog.show();
                    }
                });

                SButton btnSize = new SButton(SnapChatContext, R.drawable.size_btn, ly, 50, 460);
                btnSize.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder alert = new AlertDialog.Builder(SnapChatContext);
                        SeekBar seek = new SeekBar(SnapChatContext);
                        seek.setMax(300);
                        seek.setProgress((int) editText.getTextSize());
                        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                                editText.setTextSize(arg1);
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar arg0) {
                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar arg0) {
                            }
                        });
                        alert.setPositiveButton("Done", null);
                        alert.setView(seek);
                        alert.show();
                    }
                });

                SButton btnAlpha = new SButton(SnapChatContext, R.drawable.alpha_btn, ly, 50, 590);
                btnAlpha.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editText.setBackgroundColor(Color.TRANSPARENT);
                    }
                });

                //**Init -btnReset- button and add to view.
                RelativeLayout.LayoutParams btnResetParmas = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                btnResetParmas.addRule(RelativeLayout.CENTER_HORIZONTAL);
                btnResetParmas.topMargin = 750;
                btnResetParmas.width = 100;
                btnResetParmas.height = 100;
                ImageButton btnReset = new ImageButton(SnapChatContext);
                btnReset.setBackgroundDrawable(modRes.getDrawable(R.drawable.roundcorner));
                btnReset.setImageDrawable(modRes.getDrawable(R.drawable.alpha_btn));
                btnReset.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editText.setTextColor(Color.WHITE);
                        editText.setTextSize(21);
                        editText.setBackgroundColor(-1728053248);
                    }
                });
                ly.addView(btnReset, btnResetParmas);
                //**End

                //Add our layout to SnapChat's main layout.
                layout.addView(ly, param);
                //End of setting up our views.

                SnapColorsBtn.setOnClickListener(new View.OnClickListener() {
                    boolean SnapColorsBtnBool = true; //To see if the button is pressed agian
                    @Override
                    public void onClick(View v) {
                        if (SnapColorsBtnBool) {
                            ly.setVisibility(View.VISIBLE);
                            SnapColorsBtnBool = false;
                        } else {
                            ly.setVisibility(View.GONE);
                            SnapColorsBtnBool = true;
                        }
                    }
                });
                layout.addView(SnapColorsBtn, params);
            }
        });
    }


	@Override
	public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
		if (!lpparam.packageName.equals(SnapChatPKG))
	        return;
        ///// Find the caption box
		final Class<?> CaptionEditText = XposedHelpers.findClass("com.snapchat.android.ui.SnapCaptionView.CaptionEditText", lpparam.classLoader);
        XposedBridge.hookAllConstructors(CaptionEditText, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(final MethodHookParam param) throws NameNotFoundException {
                prefs.reload();
                editText = (EditText) param.thisObject;
                //final GestureDetector gestureDetector = new GestureDetector(SnapChatContext, new GestureDec(SnapChatContext, editText, defTypeFace));
                if (!notFirstRun) {
                    defTypeFace = editText.getTypeface();
                    notFirstRun = true;
                }

//                editText.setOnTouchListener(new View.OnTouchListener() {
//                    @Override
//                    public boolean onTouch(View arg0, MotionEvent arg1) {
//                        return gestureDetector.onTouchEvent(arg1);
//                    }
//                });

                // Get stuff from settings here
                editText.setTextColor(prefs.getInt("TextColor", Color.WHITE));
                editText.setBackgroundColor(prefs.getInt("BGColor", -1728053248));
                if (prefs.getBoolean("autoRandomize", false)) {
                    random(editText);
                }
                if (prefs.getBoolean("setFont", false)) {
                    final String fontsDir = SnapChatContext.getExternalFilesDir(null).getAbsolutePath();
                    Typeface face = Typeface.createFromFile(fontsDir + "/" + prefs.getString("Font", "0"));
                    editText.setTypeface(face);
                }
            }
        });

        // For showing the donation msg, and for getting snapchats main context
    	findAndHookMethod("com.snapchat.android.LandingPageActivity", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
			@Override
    		protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				prefs.reload();
                //Getting SnapChat's main context.
				SnapChatContext = (Activity) param.thisObject;
                File SnapColorsVer = new File(SnapChatContext.getExternalFilesDir(null).getAbsolutePath()+"/snapcolors");
                if(SnapColorsVer.createNewFile()){
                    FileUtils.writeStringToFile(SnapColorsVer, SnapChatContext.getPackageManager().getPackageInfo("com.manvir.SnapColors", 0).versionName);
                }else if(!FileUtils.readFileToString(SnapColorsVer).contentEquals(SnapChatContext.getPackageManager().getPackageInfo("com.manvir.SnapColors", 0).versionName)){
                    FileUtils.writeStringToFile(SnapColorsVer, SnapChatContext.getPackageManager().getPackageInfo("com.manvir.SnapColors", 0).versionName);
                }
    		}
    	});
    }
}