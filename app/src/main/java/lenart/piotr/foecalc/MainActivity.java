package lenart.piotr.foecalc;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.flexbox.FlexboxLayout;

import java.util.ArrayList;
import java.util.function.Predicate;

public class MainActivity extends AppCompatActivity {

    Button minimizeButton;

    ArrayList<Float> values;
    FlexboxLayout flexboxLayout;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        minimizeButton = findViewById(R.id.btMinimize);
        flexboxLayout = findViewById(R.id.flexBoxContent);

        values = new ArrayList<>();

        if (isMyServiceRunning()) {
            stopService(new Intent(MainActivity.this, FloatingWindow.class));
        }

        minimizeButton.setOnClickListener((view) -> {
            if (checkOverlayDisplayPermission()) {
                Intent serviceIntent = new Intent(MainActivity.this, FloatingWindow.class);
                float[] floatArray = new float[values.size()];
                for (int i=0; i<values.size(); i++) {
                    floatArray[i] = values.get(i);
                }
                serviceIntent.putExtra("values", floatArray);
                startService(serviceIntent);
                finish();
            } else {
                requestOverlayDisplayPermission();
            }
        });

        buildCheckboxes();
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
    }

    @SuppressLint("SetTextI18n")
    void buildCheckboxes() {
        float[] allValues = new float[] { 1.9f, 1.92f, 1.93f, 1.94f, 1.95f, 1.96f, 1.97f, 1.98f, 1.99f, 2.0f };
        float[] defaultSelected = new float[] { 1.9f, 1.92f };

        for (float value : allValues) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            boolean defaultValue = false;
            for (float f : defaultSelected) {
                if (f == value) {
                    defaultValue = true;
                    values.add(value);
                    break;
                }
            }
            checkBox.setChecked(defaultValue);
            checkBox.setText(value + "");
            checkBox.setOnCheckedChangeListener((compoundButton, b) -> {
                if (b) {
                    values.add(value);
                } else {
                    values.remove(value);
                }
            });
            flexboxLayout.addView(checkBox);
        }
    }

    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (FloatingWindow.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void requestOverlayDisplayPermission() {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(this)
                        .setCancelable(true)
                        .setTitle("Screen Overlay Permission Needed")
                        .setMessage("Enable 'Display over other apps' from System Settings.")
                        .setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("Package:" + getPackageName()));
                                startActivityForResult(intent, RESULT_OK);
                            }
                        });
        Toast.makeText(this, "Enable 'Display over other apps' from System Settings", Toast.LENGTH_LONG).show();
        // AlertDialog dialog = builder.create();
        // dialog.show();
    }

    private boolean checkOverlayDisplayPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }
}