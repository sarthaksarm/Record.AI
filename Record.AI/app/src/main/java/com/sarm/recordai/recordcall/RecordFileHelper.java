package com.sarm.recordai.recordcall;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class RecordFileHelper {
	/**
	 * returns absolute file directory
	 * 
	 * @return
	 * @throws Exception
	 */
	public static String getFilename(String phoneNumber) throws Exception {
		String filepath = null;
		String myDate = null;
		File file = null;
		if (phoneNumber == null)
			throw new Exception("Phone number can't be empty");
		try {
			filepath = getFilePath();

			file = new File(filepath, RecordConstants.FILE_DIRECTORY);

			if (!file.exists()) {
				file.mkdirs();
			}

			myDate = (String) DateFormat.format("yyyyMMddkkmmss", new Date());

			// Clean characters in file name
			phoneNumber = phoneNumber.replaceAll("[\\*\\+-]", "");
			if (phoneNumber.length() > 10) {
				phoneNumber.substring(phoneNumber.length() - 10,
						phoneNumber.length());
			}
		} catch (Exception e) {
			Log.e(RecordConstants.TAG, "Exception " + phoneNumber);
			e.printStackTrace();
		}

		return (file.getAbsolutePath() + "/d" + myDate + "p" + phoneNumber + ".3gp");
	}

	public static String getFilePath() {
		// TODO: Change to user selected directory
		return Environment.getExternalStorageDirectory().getAbsolutePath();
	}

	public static void deleteAllRecords(Activity caller) {
		String filepath = getFilePath() + "/" + RecordConstants.FILE_DIRECTORY;
		File file = new File(filepath);

		String listOfFileNames[] = file.list();

		for (int i = 0; i < listOfFileNames.length; i++) {
			File file2 = new File(filepath, listOfFileNames[i]);
			if (file2.exists()) {
				file2.delete();
			}
		}

		filepath = caller.getFilesDir().getAbsolutePath() + "/"
				+ RecordConstants.FILE_DIRECTORY;
		file = new File(filepath);

		String listOfFileNames2[] = file.list();

		for (int i = 0; i < listOfFileNames2.length; i++) {
			File file2 = new File(filepath, listOfFileNames2[i]);
			if (file2.exists()) {
				file2.delete();
			}
		}
	}

	/**
	 * Obtains the contact list for the currently selected account.
	 * 
	 * @return A cursor for for accessing the contact list.
	 */
	public static String getContactName(String phoneNum, Activity caller) {
		String res = phoneNum;
		Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
		String[] projection = new String[] {
				ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
				ContactsContract.CommonDataKinds.Phone.NUMBER };
		String selection = null;
		String[] selectionArgs = null;
		Cursor names = caller.getContentResolver().query(uri, projection,
				selection, selectionArgs, null);

		int indexName = names
				.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
		int indexNumber = names
				.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
		if (names.getCount() > 0) {
			names.moveToFirst();
			do {
				String name = names.getString(indexName);
				String number = names.getString(indexNumber).replaceAll(
						"[\\*\\+-]", "");

				if (number.compareTo(phoneNum) == 0) {
					res = name;
					break;
				}
			} while (names.moveToNext());
		}

		return res;
	}

	/**
	 * Fetches list of previous recordings
	 * 
	 * @param f
	 * @return
	 */
	public static List<Model> listDir2(File f, Activity caller) {
		File[] files = f.listFiles();
		List<Model> fileList = new ArrayList<Model>();
		try {
			for (File file : files) {
				if (!file.getName().matches(RecordConstants.FILE_NAME_PATTERN)) {
					Log.d(RecordConstants.TAG, String.format(
							"'%s' didn't match the file name pattern",
							file.getName()));
					continue;
				}

				Model mModel = new Model(file.getName());
				String phoneNum = mModel.getCallName().substring(16,
						mModel.getCallName().length() - 4);
				mModel.setUserNameFromContact(getContactName(phoneNum, caller));
				fileList.add(mModel);
			}

			Collections.sort(fileList);
			Collections.sort(fileList, Collections.reverseOrder());
		}
		catch(Exception e)
		{
			Toast.makeText(caller, "Error", Toast.LENGTH_SHORT).show();
		}
		return fileList;
	}

	public static List<Model> listFiles(Activity caller) {
		String filepath = RecordFileHelper.getFilePath();
		final File file = new File(filepath, RecordConstants.FILE_DIRECTORY);

		if (!file.exists()) {
			file.mkdirs();
		}

		final List<Model> listDir = RecordFileHelper.listDir2(file, caller);

		filepath = caller.getFilesDir().getAbsolutePath();
		final File file2 = new File(filepath, RecordConstants.FILE_DIRECTORY);

		if (!file2.exists()) {
			file2.mkdirs();
		}

		final List<Model> listDir2 = RecordFileHelper.listDir2(file2, caller);

		listDir.addAll(listDir2);

		return listDir;
	}

	public static void deleteFile(String fileName) {
		if (fileName == null)
			return;
		Log.d(RecordConstants.TAG, "FileHelper deleteFile " + fileName);
		try {
			File file = new File(fileName);

			if (file.exists()) {
				file.delete();
			}
		} catch (Exception e) {
			Log.e(RecordConstants.TAG, "Exception");
			e.printStackTrace();
		}
	}
}
