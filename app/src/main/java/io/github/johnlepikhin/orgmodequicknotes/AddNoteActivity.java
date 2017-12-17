package io.github.johnlepikhin.orgmodequicknotes;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

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
        String stateValue = MainAppWidgetConfigureActivity.getString(this, widgetID, "state_draft");

        final EditText title = findViewById(R.id.noteTitle);
        final EditText text = findViewById(R.id.noteText);
        final Spinner state = findViewById(R.id.noteState);

        Button buttonSave = findViewById(R.id.noteSave);
        title.setText(titleValue, TextView.BufferType.EDITABLE);
        text.setText(textValue, TextView.BufferType.EDITABLE);
        if (stateValue != null) {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.note_states, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            state.setAdapter(adapter);
            int spinnerPosition = adapter.getPosition(stateValue);
            state.setSelection(spinnerPosition);
        }

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
                    String stateVlue = state.getSelectedItem().toString();
                    if (stateVlue.equals("none")) {
                        stateVlue = "";
                    } else {
                        stateVlue = stateVlue + " ";
                    }
                    String output = String.format("* %s%s\n\n%s\n", stateVlue, titleValue, textValue);
                    String file_path = MainAppWidgetConfigureActivity.getString(AddNoteActivity.this, widgetID, "file_path");
                    if (file_path == null) {
                        Toast t = Toast.makeText(getApplicationContext(), "Empty file name. Copy note contents and re-add the widget.", Toast.LENGTH_LONG);
                        t.show();
                        return;
                    }
                    Log.d("SAVE", "Use file_path: " + file_path);
                    OutputStream stream = getContentResolver().openOutputStream(Uri.parse(file_path), "wa");
                    assert stream != null;
                    stream.write(output.getBytes());
                    stream.close();
                    title.getText().clear();
                    text.getText().clear();
                    state.setSelection(0);
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
        Spinner state = findViewById(R.id.noteState);

        MainAppWidgetConfigureActivity.putString(this, widgetID, "title_draft", title.getText().toString())
                .putString("text_draft", text.getText().toString())
                .putString("state_draft", state.getSelectedItem().toString())
                .apply();
    }
}
