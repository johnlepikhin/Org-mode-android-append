package io.github.johnlepikhin.orgmodequicknotes;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.OutputStream;

/**
 * The configuration screen for the {@link MainAppWidget MainAppWidget} AppWidget.
 */
public class MainAppWidgetConfigureActivity extends Activity {
    private static final int REQUEST_FILE_CODE = 42;
    private static final String PREFS_NAME = "io.github.johnlepikhin.orgmodequicknotes.MainAppWidget";
    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private EditText mAppWidgetText;
    private EditText mFilePath;
    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = MainAppWidgetConfigureActivity.this;

            String file_path = mFilePath.getText().toString();
            try {
                OutputStream stream = getContentResolver().openOutputStream(Uri.parse(file_path), "wa");
                assert stream != null;
                stream.close();
            } catch (Exception e) {
                Toast t = Toast.makeText(getApplicationContext(), "Cannot open file for appending", Toast.LENGTH_SHORT);
                t.show();
                Log.e("err", "Exception: " + Log.getStackTraceString(e));
                return;
            }

            putString(context, mAppWidgetId, "widget_title", mAppWidgetText.getText().toString())
                    .putString("file_path", mFilePath.getText().toString())
                    .apply();

            // It is the responsibility of the configuration activity to update the app widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            MainAppWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId);

            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };

    public MainAppWidgetConfigureActivity() {
        super();
    }

    static private SharedPreferences getPrefs(Context context, int appWidgetId) {
        return context.getSharedPreferences(PREFS_NAME + ":" + appWidgetId, 0);
    }

    static public String getString(Context context, int appWidgetId, String name) {
        return getPrefs(context, appWidgetId).getString(name, null);
    }

    static public SharedPreferences.Editor putString(Context context, int appWidgetId, String name, String value) {
        return getPrefs(context, appWidgetId).edit().putString(name, value);
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        setContentView(R.layout.main_app_widget_configure);
        mAppWidgetText = findViewById(R.id.appwidget_text);
        mFilePath = findViewById(R.id.appwidget_file_path);
        findViewById(R.id.add_button).setOnClickListener(mOnClickListener);

        mFilePath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performFileSearch();
            }
        });

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        String titleValue = getString(MainAppWidgetConfigureActivity.this, mAppWidgetId, "widget_title");
        if (titleValue == null) {
            titleValue = "Org note";
        }
        mAppWidgetText.setText(titleValue);

        performFileSearch();
    }

    private void performFileSearch() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, REQUEST_FILE_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        if (requestCode == REQUEST_FILE_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                if (uri == null) {
                    Toast t = Toast.makeText(getApplicationContext(), "Cannot open file for appending", Toast.LENGTH_SHORT);
                    t.show();
                    return;
                }
                String file_path = uri.toString();
                if (file_path == null) {
                    Toast t = Toast.makeText(getApplicationContext(), "Cannot open file for appending", Toast.LENGTH_SHORT);
                    t.show();
                    return;
                }
                Log.i("FILE", "Uri: " + file_path);
                mFilePath.setText(file_path);
            }
        }
    }
}
