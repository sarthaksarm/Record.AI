package com.sarm.recordai.recordcall;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.sarm.recordai.R;

import java.io.IOException;

public class RecordService extends Service {

	private MediaRecorder recorder = null;
	private String phoneNumber = null;

	private String fileName;
	private boolean onCall = false;
	private boolean recording = false;
	private boolean silentMode = false;
	private boolean onForeground = false;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(RecordConstants.TAG, "RecordService onStartCommand");
		if (intent != null) {
			int commandType = intent.getIntExtra("commandType", 0);
			if (commandType != 0) {
				if (commandType == RecordConstants.RECORDING_ENABLED) {
					Log.d(RecordConstants.TAG, "RecordService RECORDING_ENABLED");
					silentMode = intent.getBooleanExtra("silentMode", true);
					if (!silentMode && phoneNumber != null && onCall
							&& !recording)
						commandType = RecordConstants.STATE_START_RECORDING;

				} else if (commandType == RecordConstants.RECORDING_DISABLED) {
					Log.d(RecordConstants.TAG, "RecordService RECORDING_DISABLED");
					silentMode = intent.getBooleanExtra("silentMode", true);
					if (onCall && phoneNumber != null && recording)
						commandType = RecordConstants.STATE_STOP_RECORDING;
				}

				if (commandType == RecordConstants.STATE_INCOMING_NUMBER) {
					Log.d(RecordConstants.TAG, "RecordService STATE_INCOMING_NUMBER");
					startService();
					if (phoneNumber == null)
						phoneNumber = intent.getStringExtra("phoneNumber");

					silentMode = intent.getBooleanExtra("silentMode", true);
				} else if (commandType == RecordConstants.STATE_CALL_START) {
					Log.d(RecordConstants.TAG, "RecordService STATE_CALL_START");
					onCall = true;

					if (!silentMode && phoneNumber != null && onCall
							&& !recording) {
						startService();
						startRecording(intent);
					}
				} else if (commandType == RecordConstants.STATE_CALL_END) {
					Log.d(RecordConstants.TAG, "RecordService STATE_CALL_END");
					onCall = false;
					phoneNumber = null;
					stopAndReleaseRecorder();
					recording = false;
					stopService();
				} else if (commandType == RecordConstants.STATE_START_RECORDING) {
					Log.d(RecordConstants.TAG, "RecordService STATE_START_RECORDING");
					if (!silentMode && phoneNumber != null && onCall) {
						startService();
						startRecording(intent);
					}
				} else if (commandType == RecordConstants.STATE_STOP_RECORDING) {
					Log.d(RecordConstants.TAG, "RecordService STATE_STOP_RECORDING");
					stopAndReleaseRecorder();
					recording = false;
				}
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * in case it is impossible to record
	 */
	private void terminateAndEraseFile() {
		Log.d(RecordConstants.TAG, "RecordService terminateAndEraseFile");
		stopAndReleaseRecorder();
		recording = false;
		deleteFile();
	}

	private void stopService() {
		Log.d(RecordConstants.TAG, "RecordService stopService");
		stopForeground(true);
		onForeground = false;
		this.stopSelf();
	}

	private void deleteFile() {
		Log.d(RecordConstants.TAG, "RecordService deleteFile");
		RecordFileHelper.deleteFile(fileName);
		fileName = null;
	}

	private void stopAndReleaseRecorder() {
		if (recorder == null)
			return;
		Log.d(RecordConstants.TAG, "RecordService stopAndReleaseRecorder");
		boolean recorderStopped = false;
		boolean exception = false;

		try {
			recorder.stop();
			recorderStopped = true;
		} catch (IllegalStateException e) {
			Log.e(RecordConstants.TAG, "IllegalStateException");
			e.printStackTrace();
			exception = true;
		} catch (RuntimeException e) {
			Log.e(RecordConstants.TAG, "RuntimeException");
			exception = true;
		} catch (Exception e) {
			Log.e(RecordConstants.TAG, "Exception");
			e.printStackTrace();
			exception = true;
		}
		try {
			recorder.reset();
		} catch (Exception e) {
			Log.e(RecordConstants.TAG, "Exception");
			e.printStackTrace();
			exception = true;
		}
		try {
			recorder.release();
		} catch (Exception e) {
			Log.e(RecordConstants.TAG, "Exception");
			e.printStackTrace();
			exception = true;
		}

		recorder = null;
		if (exception) {
			deleteFile();
		}
		if (recorderStopped) {
			Toast toast = Toast.makeText(this,
					this.getString(R.string.receiver_end_call),
					Toast.LENGTH_SHORT);
			toast.show();
		}
	}

	@Override
	public void onDestroy() {
		Log.d(RecordConstants.TAG, "RecordService onDestroy");
		stopAndReleaseRecorder();
		stopService();
		super.onDestroy();
	}

	private void startRecording(Intent intent) {
		Log.d(RecordConstants.TAG, "RecordService startRecording");
		boolean exception = false;
		recorder = new MediaRecorder();

		try {
			recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			fileName = RecordFileHelper.getFilename(phoneNumber);
			recorder.setOutputFile(fileName);

			OnErrorListener errorListener = new OnErrorListener() {
				public void onError(MediaRecorder arg0, int arg1, int arg2) {
					Log.e(RecordConstants.TAG, "OnErrorListener " + arg1 + "," + arg2);
					terminateAndEraseFile();
				}
			};
			recorder.setOnErrorListener(errorListener);

			OnInfoListener infoListener = new OnInfoListener() {
				public void onInfo(MediaRecorder arg0, int arg1, int arg2) {
					Log.e(RecordConstants.TAG, "OnInfoListener " + arg1 + "," + arg2);
					terminateAndEraseFile();
				}
			};
			recorder.setOnInfoListener(infoListener);

			recorder.prepare();
			// Sometimes prepare takes some time to complete
			Thread.sleep(2000);
			recorder.start();
			recording = true;
			Log.d(RecordConstants.TAG, "RecordService recorderStarted");
		} catch (IllegalStateException e) {
			Log.e(RecordConstants.TAG, "IllegalStateException");
			e.printStackTrace();
			exception = true;
		} catch (IOException e) {
			Log.e(RecordConstants.TAG, "IOException");
			e.printStackTrace();
			exception = true;
		} catch (Exception e) {
			Log.e(RecordConstants.TAG, "Exception");
			e.printStackTrace();
			exception = true;
		}

		if (exception) {
			terminateAndEraseFile();
		}

		if (recording) {
			Toast toast = Toast.makeText(this,
					this.getString(R.string.receiver_start_call),
					Toast.LENGTH_SHORT);
			toast.show();
		} else {
			Toast toast = Toast.makeText(this,
					this.getString(R.string.record_impossible),
					Toast.LENGTH_LONG);
			toast.show();
		}
	}

	private void startService() {
		if (!onForeground) {
			Log.d(RecordConstants.TAG, "RecordService startService");
			Intent intent = new Intent(this, RecordActivity.class);
			// intent.setAction(Intent.ACTION_VIEW);
			// intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			PendingIntent pendingIntent = PendingIntent.getActivity(
					getBaseContext(), 0, intent, 0);

			Notification notification = new NotificationCompat.Builder(
					getBaseContext())
					.setContentTitle(
							this.getString(R.string.notification_title))
					.setTicker(this.getString(R.string.notification_ticker))
					.setContentText(this.getString(R.string.notification_text))
					.setSmallIcon(R.drawable.ic_launcher)
					.setContentIntent(pendingIntent).setOngoing(true)
					.getNotification();

			notification.flags = Notification.FLAG_NO_CLEAR;

			startForeground(1337, notification);
			onForeground = true;
		}
	}
}
