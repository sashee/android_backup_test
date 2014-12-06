package hu.advancedweb.androidbackuptest.app;

import android.app.Activity;
import android.app.backup.BackupManager;
import android.app.backup.RestoreObserver;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Ordering;

import java.util.HashSet;


public class MainActivity extends Activity {

    public static final String RUN_SIZE_CHECK_ON_NEXT_BACKUP="runSizeCheckOnNextBackup";

    public static final String SIZE_CHECK_DATA_NUM="sizeCheckDataNum";
    public static final int SIZE_CHECK_DATA_NUM_DEFAULT=30;

    public static final String SIZE_CHECK_BYTE_NUM="sizeCheckByteNum";
    public static final int SIZE_CHECK_BYTE_NUM_DEFAULT=100000;

    public static final String LOGS="Logs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private void refreshTexts(){
        SharedPreferences sp=getSharedPreferences("pref", MODE_PRIVATE);
        ((ToggleButton) findViewById(R.id.toggleButton)).setChecked(sp.getBoolean(RUN_SIZE_CHECK_ON_NEXT_BACKUP, false));

        ((TextView)findViewById(R.id.logText)).setText(Joiner.on("\n").join(FluentIterable.from(sp.getStringSet(LOGS, new HashSet<String>())).toSortedSet(Ordering.natural())));

        ((EditText)findViewById(R.id.sizeCheckDataNum)).setText(sp.getInt(SIZE_CHECK_DATA_NUM,SIZE_CHECK_DATA_NUM_DEFAULT)+"");

        ((EditText)findViewById(R.id.sizeCheckByteNum)).setText(sp.getInt(SIZE_CHECK_BYTE_NUM,SIZE_CHECK_BYTE_NUM_DEFAULT)+"");

        System.out.println(((TextView)findViewById(R.id.logText)).getText());
    }

    @Override
    protected void onStart() {
        super.onStart();
        refreshTexts();

        ((EditText)findViewById(R.id.sizeCheckDataNum)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!s.toString().trim().equals("")) {
                    getSharedPreferences("pref", MODE_PRIVATE).edit().putInt(SIZE_CHECK_DATA_NUM, Integer.parseInt(s.toString())).apply();
                }

            }
        });

        ((EditText)findViewById(R.id.sizeCheckByteNum)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!s.toString().trim().equals("")) {
                    getSharedPreferences("pref", MODE_PRIVATE).edit().putInt(SIZE_CHECK_BYTE_NUM, Integer.parseInt(s.toString())).apply();
                }

            }
        });
    }

    public void onToggleSizeCheckOnNextBackup(View view){
        boolean on = ((ToggleButton) view).isChecked();
        getSharedPreferences("pref", MODE_PRIVATE).edit().putBoolean(RUN_SIZE_CHECK_ON_NEXT_BACKUP, on).apply();
    }

    public void onClickClearLog(View v){
        getSharedPreferences("pref", MODE_PRIVATE).edit().putStringSet(LOGS,new HashSet<String>()).apply();
        refreshTexts();
    }

    public void onClickTriggerDataChanged(View v){
        BackupManager backupManager = new BackupManager(getApplicationContext());
        backupManager.dataChanged();
        System.out.println("DATACHANGED!");
    }

    public void onClickRequestRestore(View v){
        BackupManager backupManager = new BackupManager(getApplicationContext());
        backupManager.requestRestore(new RestoreObserver() {
            @Override
            public void restoreStarting(int numPackages) {
                super.restoreStarting(numPackages);
            }

            @Override
            public void onUpdate(int nowBeingRestored, String currentPackage) {
                super.onUpdate(nowBeingRestored, currentPackage);
            }

            @Override
            public void restoreFinished(int error) {
                super.restoreFinished(error);
                refreshTexts();
                Toast.makeText(MainActivity.this,"Update successful",Toast.LENGTH_SHORT).show();
            }
        });
    }
}
