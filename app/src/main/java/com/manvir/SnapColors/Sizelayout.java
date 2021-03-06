package com.manvir.SnapColors;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

@SuppressWarnings("deprecation")
public class Sizelayout extends LinearLayout implements SeekBar.OnSeekBarChangeListener {
    private EditText editText;

    public Sizelayout(Context context, final EditText editText, int textSize, final HorizontalScrollView f, final ImageButton SnapColorsBtn) {
        super(context);
        this.editText = editText;
        f.setVisibility(View.GONE);
        SnapColorsBtn.setClickable(false);
        setClickable(true);
        setBackgroundDrawable(App.modRes.getDrawable(R.drawable.bgviewdraw));
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.addView(inflater.inflate(App.modRes.getLayout(R.layout.size_layout), null));
        SeekBar seekBarSize = (SeekBar) findViewById(R.id.seekBarSize);
        seekBarSize.setOnSeekBarChangeListener(this);
        seekBarSize.setMax(300);// So people don't go crazy with the size.
        seekBarSize.setProgress(textSize);

        Button btnDone = (Button) findViewById(R.id.done);
        btnDone.getLayoutParams().width = (App.size.x / 2) - 40;
        btnDone.setOnClickListener(v -> {
            ((RelativeLayout) Sizelayout.this.getParent()).removeView(Sizelayout.this);
            f.setVisibility(View.VISIBLE);
            SnapColorsBtn.setClickable(true);
        });

        Button btnCancel = (Button) findViewById(R.id.cancel);
        btnCancel.getLayoutParams().width = (App.size.x / 2) - 40;
        btnCancel.setOnClickListener(v -> {
            editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, 57.0f);
            ((RelativeLayout) Sizelayout.this.getParent()).removeView(Sizelayout.this);
            f.setVisibility(View.VISIBLE);
            SnapColorsBtn.setClickable(true);
        });
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
