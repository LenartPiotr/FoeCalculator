package lenart.piotr.foecalc;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.text.Editable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.internal.TextWatcherAdapter;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class FloatingWindow extends Service {

    private ViewGroup floatView;
    private int LAYOUT_TYPE;
    private WindowManager.LayoutParams floatWindowLayoutParams;
    private WindowManager windowManager;

    private Button exitButton;
    private EditText input;
    private FlexboxLayout flexboxLayout;

    float[] values;

    public FloatingWindow() { }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getExtras() != null) {
            float[] copyValues = intent.getExtras().getFloatArray("values");
            if (copyValues == null) {
                values = new float[] { 1.9f };
            } else {
                values = new float[copyValues.length];
                for (int i=0; i<copyValues.length; i++) {
                    values[i] = copyValues[i];
                }
            }
        } else {
            values = new float[] { 1.9f };
        }
        updateHeight();
        return super.onStartCommand(intent, flags, startId);
    }

    @SuppressLint({"ClickableViewAccessibility", "RestrictedApi"})
    @Override
    public void onCreate() {
        super.onCreate();

        DisplayMetrics metrics = getApplicationContext().getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);

        floatView = (ViewGroup) inflater.inflate(R.layout.floating_layout, null);

        exitButton = floatView.findViewById(R.id.btExit);
        input = floatView.findViewById(R.id.inputValue);
        flexboxLayout = floatView.findViewById(R.id.outFlexBox);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_TYPE = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_TYPE = WindowManager.LayoutParams.TYPE_TOAST;
        }

        floatWindowLayoutParams = new WindowManager.LayoutParams(
                350, //(int)(width * 0.55f),
                400, //(int)(height * 0.59f),
                LAYOUT_TYPE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        floatWindowLayoutParams.gravity = Gravity.CENTER;
        floatWindowLayoutParams.x = 0;
        floatWindowLayoutParams.y = 0;

        windowManager.addView(floatView, floatWindowLayoutParams);

        updateHeight();

        floatView.setOnTouchListener(new View.OnTouchListener() {
            final WindowManager.LayoutParams floatWindowLayoutUpdateParam = floatWindowLayoutParams;
            double x;
            double y;
            double px;
            double py;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x = floatWindowLayoutUpdateParam.x;
                        y = floatWindowLayoutUpdateParam.y;
                        px = event.getRawX();
                        py = event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        floatWindowLayoutUpdateParam.x = (int) ((x + event.getRawX()) - px);
                        floatWindowLayoutUpdateParam.y = (int) ((y + event.getRawY()) - py);
                        windowManager.updateViewLayout(floatView, floatWindowLayoutUpdateParam);
                        break;
                }
                return false;
            }
        });

        exitButton.setOnClickListener(view -> {
            stopSelf();
        });

        input.setOnTouchListener((v, event) -> {
            input.setCursorVisible(true);
            WindowManager.LayoutParams floatWindowLayoutParamUpdateFlag = floatWindowLayoutParams;
            floatWindowLayoutParamUpdateFlag.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
            windowManager.updateViewLayout(floatView, floatWindowLayoutParamUpdateFlag);
            return false;
        });

        input.setOnEditorActionListener((view, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                WindowManager.LayoutParams floatWindowLayoutParamUpdateFlag = floatWindowLayoutParams;
                floatWindowLayoutParamUpdateFlag.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                windowManager.updateViewLayout(floatView, floatWindowLayoutParamUpdateFlag);
            }
            return false;
        });

        input.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(@NonNull Editable s) {
                int number = 0;
                try {
                    number = Integer.parseInt(s.toString());
                }catch (Exception ignored) {}
                calculate(number);
            }
        });
    }

    void updateHeight() {
        if (floatWindowLayoutParams == null || windowManager == null || values == null) return;
        floatWindowLayoutParams.height = 400 + values.length * 50;
        windowManager.updateViewLayout(floatView, floatWindowLayoutParams);
    }

    @SuppressLint("SetTextI18n")
    void calculate(int number) {
        flexboxLayout.removeAllViews();
        for (float value : values) {
            float result = Math.round(number * value);
            TextView textView = new TextView(this);
            textView.setText(value + " = " + (int)result);
            flexboxLayout.addView(textView);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
        windowManager.removeView(floatView);
    }
}
