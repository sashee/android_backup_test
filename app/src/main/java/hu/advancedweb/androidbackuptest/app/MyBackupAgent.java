package hu.advancedweb.androidbackuptest.app;

import android.app.backup.BackupAgent;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.util.Pair;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by sashee on 11/28/14.
 */
public class MyBackupAgent extends BackupAgent{

	@Override
	public void onCreate() {
		super.onCreate();
	}

	private String getLogMessage(String text){
		return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US).format(new Date()) + ":" + text;
	}

	@Override
	public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) throws IOException {
		SharedPreferences sp = getSharedPreferences("pref", Context.MODE_PRIVATE);
		HashSet<String> log = new HashSet<>(sp.getStringSet(MainActivity.LOGS, new HashSet<String>()));
		SharedPreferences.Editor edit = sp.edit();
		try {
			log.add(getLogMessage("Backup called"));

			if (getSharedPreferences("pref", MODE_PRIVATE).getBoolean(MainActivity.RUN_SIZE_CHECK_ON_NEXT_BACKUP, false)) {
				edit.putBoolean(MainActivity.RUN_SIZE_CHECK_ON_NEXT_BACKUP, false);
				log.add(getLogMessage("Performing size test..."));
				data.writeEntityHeader("data", -1);
				data.writeEntityHeader("header1", -1);
				Random random=new Random();
				int i = 0;
				try {
					for (; i < getSharedPreferences("pref", MODE_PRIVATE).getInt(MainActivity.SIZE_CHECK_DATA_NUM,MainActivity.SIZE_CHECK_BYTE_NUM_DEFAULT); i++) {
						byte[] b = new byte[getSharedPreferences("pref", MODE_PRIVATE).getInt(MainActivity.SIZE_CHECK_BYTE_NUM, MainActivity.SIZE_CHECK_BYTE_NUM_DEFAULT)];
						random.nextBytes(b);
						data.writeEntityHeader("data:" + i, b.length);
						data.writeEntityData(b, b.length);
					}
					for(;i < 100;i++) {
						data.writeEntityHeader("data:" + i, -1);
					}
				}catch(Exception e) {
					log.add(getLogMessage("Max iterations reached before exception:" + i));
				}
				log.add(getLogMessage("Size test completed"));
			}

			BackupManager backupManager = new BackupManager(getApplicationContext());
			backupManager.dataChanged();
		} catch(Exception e) {
			e.printStackTrace();
			log.add(getLogMessage("Error occured when backing up"));
		} finally {
			edit.putStringSet(MainActivity.LOGS,log);
			edit.apply();
		}
	}

	@Override
	public void onRestore(BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState) throws IOException {
		System.out.println("RESTORING!");
		SharedPreferences sp = getSharedPreferences("pref", Context.MODE_PRIVATE);
		HashSet<String> log = new HashSet<>(sp.getStringSet(MainActivity.LOGS, new HashSet<String>()));
		SharedPreferences.Editor edit = sp.edit();
		try {
			log.add(getLogMessage("Restore called"));
			ImmutableList.Builder<Pair<String, Integer>> keys = ImmutableList.builder();
			while (data.readNextHeader()) {
				String key = data.getKey();
				int dataSize = data.getDataSize();
				keys.add(Pair.create(key, dataSize));
				data.skipEntityData();
			}
			log.add(getLogMessage("Restore data contained [" + Joiner.on(",").join(FluentIterable.from(keys.build()).transform(new Function<Pair<String, Integer>, String>() {
				@Override
				public String apply(Pair<String, Integer> input) {
					return input.first + "(" + input.second + ")";
				}
			})) + "]"));
		} finally {
			edit.putStringSet(MainActivity.LOGS, log);
			edit.apply();
		}
	}
}
