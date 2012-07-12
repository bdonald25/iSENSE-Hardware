package edu.uml.cs.isense.collector;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MediaManager extends Activity {

	private Uri imageUri;
	private Uri videoUri;

	private static final int CAMERA_PIC_REQUESTED = 1;
	private static final int CAMERA_VID_REQUESTED = 2;

	private Waffle w;
	private static Context mContext;
	private TextView mediaCountLabel;
	private Button takePic;
	private Button takeVid;
	private Button back;

	public static int mediaCount;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.media_manager);

		mContext = this;
		w = new Waffle(mContext);

		mediaCountLabel = (TextView) findViewById(R.id.mediaCounter);
		mediaCountLabel.setText(mContext.getString(R.string.picAndVidCount)
				+ mediaCount);

		takePic = (Button) findViewById(R.id.mediaPicture);
		takePic.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String state = Environment.getExternalStorageState();
				if (Environment.MEDIA_MOUNTED.equals(state)) {

					ContentValues values = new ContentValues();

					imageUri = getContentResolver().insert(
							MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
							values);

					Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
					intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
					startActivityForResult(intent, CAMERA_PIC_REQUESTED);

				} else {
					w.make("Permission isn't granted to write to external storage.  Please enable to take pictures.",
							Toast.LENGTH_LONG, "x");
				}

			}
		});

		takeVid = (Button) findViewById(R.id.mediaVideo);
		takeVid.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String state = Environment.getExternalStorageState();
				if (Environment.MEDIA_MOUNTED.equals(state)) {

					ContentValues valuesVideos = new ContentValues();

					videoUri = getContentResolver().insert(
							MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
							valuesVideos);

					Intent intentVid = new Intent(
							MediaStore.ACTION_VIDEO_CAPTURE);
					intentVid.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);
					intentVid.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
					startActivityForResult(intentVid, CAMERA_VID_REQUESTED);

				} else {
					w.make("Permission isn't granted to write to external storage.  Please enable to record videos.",
							Toast.LENGTH_LONG, "x");
				}
			}
		});

		back = (Button) findViewById(R.id.mediaBack);
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == CAMERA_PIC_REQUESTED) {
			if (resultCode == RESULT_OK) {
				File f = convertImageUriToFile(imageUri, this);
				DataCollector.pictureArray.add(f);
				mediaCount++;
				mediaCountLabel.setText(getString(R.string.picAndVidCount)
						+ mediaCount);
				pushPicture(f);
			}
		} else if (requestCode == CAMERA_VID_REQUESTED) {
			if (resultCode == RESULT_OK) {
				File f = convertVideoUriToFile(videoUri, this);
				mediaCount++;
				mediaCountLabel.setText("" + getString(R.string.picAndVidCount)
						+ mediaCount);
				pushVideo(f);
			}
		}
	}

	// Converts the captured picture's uri to a file that is save-able to the SD
	// Card
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	public static File convertImageUriToFile(Uri imageUri, Activity activity) {

		int apiLevel = getApiLevel();
		if (apiLevel >= 11) {

			String[] proj = { MediaStore.Images.Media.DATA,
					MediaStore.Images.Media._ID,
					MediaStore.Images.ImageColumns.ORIENTATION };
			String selection = null;
			String[] selectionArgs = null;
			String sortOrder = null;

			CursorLoader cursorLoader = new CursorLoader(mContext, imageUri,
					proj, selection, selectionArgs, sortOrder);

			Cursor cursor = cursorLoader.loadInBackground();

			int file_ColumnIndex = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			int orientation_ColumnIndex = cursor
					.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.ORIENTATION);
			if (cursor.moveToFirst()) {
				@SuppressWarnings("unused")
				String orientation = cursor.getString(orientation_ColumnIndex);
				return new File(cursor.getString(file_ColumnIndex));
			}
			return null;

		} else {

			Cursor cursor = null;
			try {
				String[] proj = { MediaStore.Images.Media.DATA,
						MediaStore.Images.Media._ID,
						MediaStore.Images.ImageColumns.ORIENTATION };
				cursor = activity.managedQuery(imageUri, proj, // Which columns
																// to return
						null, // WHERE clause; which rows to return (all rows)
						null, // WHERE clause selection arguments (none)
						null); // Order-by clause (ascending by name)
				int file_ColumnIndex = cursor
						.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				int orientation_ColumnIndex = cursor
						.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.ORIENTATION);
				if (cursor.moveToFirst()) {
					@SuppressWarnings("unused")
					String orientation = cursor
							.getString(orientation_ColumnIndex);
					return new File(cursor.getString(file_ColumnIndex));
				}
				return null;
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
		}
	}

	// Converts the recorded video's uri to a file that is save-able to the SD
	// Card
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	public static File convertVideoUriToFile(Uri videoUri, Activity activity) {

		int apiLevel = getApiLevel();
		if (apiLevel >= 11) {

			String[] proj = { MediaStore.Video.Media.DATA,
					MediaStore.Video.Media._ID };
			String selection = null;
			String[] selectionArgs = null;
			String sortOrder = null;

			CursorLoader cursorLoader = new CursorLoader(mContext, videoUri,
					proj, selection, selectionArgs, sortOrder);

			Cursor cursor = cursorLoader.loadInBackground();
			int file_ColumnIndex = cursor
					.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
			if (cursor.moveToFirst()) {
				return new File(cursor.getString(file_ColumnIndex));
			}
			return null;

		} else {

			Cursor cursor = null;

			try {
				String[] proj = { MediaStore.Video.Media.DATA,
						MediaStore.Video.Media._ID };
				cursor = activity.managedQuery(videoUri, proj, // Which columns
																// to return
						null, // WHERE clause; which rows to return (all rows)
						null, // WHERE clause selection arguments (none)
						null); // Order-by clause (ascending by name)
				int file_ColumnIndex = cursor
						.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
				if (cursor.moveToFirst()) {
					return new File(cursor.getString(file_ColumnIndex));
				}
				return null;
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
		}

	}

	// Assists with differentiating between displays for dialogues
	@SuppressWarnings("deprecation")
	private static int getApiLevel() {
		return Integer.parseInt(android.os.Build.VERSION.SDK);
	}

	// Adds pictures to the SD Card
	public void pushPicture(File pic) {
		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy--HH-mm-ss");
		Date dt = new Date();
		String uploadSessionString;

		String dateString = sdf.format(dt);

		if (DataCollector.partialSessionName.equals(""))
			uploadSessionString = "Session Name Not Provided";
		else
			uploadSessionString = DataCollector.partialSessionName;

		File folder = new File(Environment.getExternalStorageDirectory()
				+ "/iSENSE");

		if (!folder.exists()) {
			folder.mkdir();
		}

		File newFile = new File(folder, uploadSessionString + " - "
				+ dateString + ".jpeg");
		pic.renameTo(newFile);
	}

	// Adds videos to the SD Card
	public void pushVideo(File vid) {
		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy--HH-mm-ss");
		Date dt = new Date();
		String uploadSessionString;

		String dateString = sdf.format(dt);

		if (DataCollector.partialSessionName.equals(""))
			uploadSessionString = "Session Name Not Provided";
		else
			uploadSessionString = DataCollector.partialSessionName;
			
		File folder = new File(Environment.getExternalStorageDirectory()
				+ "/iSENSE");

		if (!folder.exists()) {
			folder.mkdir();
		}

		File newFile = new File(folder, uploadSessionString + " - "
				+ dateString + ".3gp");
		vid.renameTo(newFile);

	}

}