package io.github.johnlepikhin.orgmodequicknotes;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

public class AddNoteActivity extends Activity {
    private int widgetID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        widgetID = getIntent().getIntExtra("widgetID", 0);

        setContentView(R.layout.activity_add_note);

        String titleValue = MainAppWidgetConfigureActivity.getString(this, widgetID, "title_draft");
        if (titleValue == null) {
            titleValue = "";
        }
        String textValue = MainAppWidgetConfigureActivity.getString(this, widgetID, "text_draft");
        if (textValue == null) {
            textValue = "";
        }

        final EditText title = findViewById(R.id.noteTitle);
        final EditText text = findViewById(R.id.noteText);
        Button buttonSave = findViewById(R.id.noteSave);
        title.setText(titleValue, TextView.BufferType.EDITABLE);
        text.setText(textValue, TextView.BufferType.EDITABLE);

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String titleValue = title.getText().toString();
                String textValue = text.getText().toString();

                if (titleValue.isEmpty() && textValue.isEmpty()) {
                    finish();
                    return;
                }

                try {
                    String output = String.format("* %s\n\n%s\n", titleValue, textValue);
                    String file_path = MainAppWidgetConfigureActivity.getString(AddNoteActivity.this, widgetID, "file_path");
                    if (file_path == null) {
                        Toast t = Toast.makeText(getApplicationContext(), "Empty file name. Copy note contents and re-add the widget.", Toast.LENGTH_LONG);
                        t.show();
                        return;
                    }
                    File file = new File(file_path);
                    FileOutputStream stream = new FileOutputStream(file, true);
                    stream.write(output.getBytes());
                    stream.close();
                    title.getText().clear();
                    text.getText().clear();
                    finish();
                } catch (Exception e) {
                    Toast t = Toast.makeText(getApplicationContext(), "Failed to save file", Toast.LENGTH_SHORT);
                    t.show();
                    Log.e("err", "Exception: " + Log.getStackTraceString(e));
                }
            }
        });
    }

    protected void onStop() {
        super.onStop();

        EditText title = findViewById(R.id.noteTitle);
        EditText text = findViewById(R.id.noteText);

        String titleValue = title.getText().toString();
        String textValue = text.getText().toString();

        MainAppWidgetConfigureActivity.putString(this, widgetID, "title_draft", titleValue)
                .putString("text_draft", textValue)
                .apply();
    }
}
