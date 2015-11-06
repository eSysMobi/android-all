package mobi.esys.tasks;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import mobi.esys.constants.UNLConstants;
import mobi.esys.data.GDFile;
import mobi.esys.fileworks.DirectiryWorks;
import mobi.esys.upnews_server.UNLServer;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

/**
 * 
 * AsyncTask for control background video downloading proccess
 * 
 * @author Артем
 * @version 1.0
 * 
 */
public class DownloadVideoTask extends AsyncTask<Void, Void, Void> {
	private static final int BUFFER_SIZE = 1024;
	/**
	 * Instance of K2Server class
	 * 
	 * @see {@link UNLServer}
	 */
	private transient final UNLServer k2Server;
	/**
	 * Instance of the parent activity
	 */
	private transient final Context context;
	/**
	 * Instance of object to access to secured settings
	 */
	private transient final SharedPreferences prefs;
	/**
	 * Indicator of broken files deleting proccess
	 */
	private transient boolean isDelete;
	/**
	 * Instance of Google Drive SDK class
	 */
	private transient final Drive drive;
	/**
	 * I/O stream instance for download files
	 */
	private transient static FileOutputStream output;
	/**
	 * List of Google Drive files md5 sums
	 */
	private transient Set<String> serverMD5;
	/**
	 * Index of current downloading file
	 */
	private transient int downCount;
	/**
	 * List of Google Drive files without diplicate values
	 */
	private transient List<GDFile> listwoDuplicates;
	/**
	 * List of sd card upnewslite folder files md5 sums
	 */
	private transient List<String> folderMD5;
	/**
	 * Instance of Android SDK class which helps download files
	 * (<b>optional</b>)
	 */
	private transient final DownloadManager downManager;

	/**
	 * Constructor
	 * 
	 * @param context
	 *            Instance of the parent activity
	 *
	 */
	public DownloadVideoTask(final Context context) {
		super();
		k2Server = new UNLServer(context);
		this.downCount = 0;
		this.downManager = (DownloadManager) context
				.getSystemService(Context.DOWNLOAD_SERVICE);
		this.prefs = context.getSharedPreferences(UNLConstants.APP_PREF,
				Context.MODE_PRIVATE);
		final GoogleAccountCredential credential = GoogleAccountCredential
				.usingOAuth2(context, DriveScopes.DRIVE);
		credential.setSelectedAccountName(prefs.getString("accName", ""));
		drive = getDriveService(credential);
		this.context = context;

	}

	/**
	 * @see AsyncTask#doInBackground
	 */
	@Override
	protected Void doInBackground(final Void... params) {
		Log.d("down", "isDwonl");
		serverMD5 = k2Server.getMD5FromServer();
		final List<GDFile> gdFiles = k2Server.getGdFiles();
		final DirectiryWorks directiryWorks = new DirectiryWorks(
				UNLConstants.VIDEO_DIR);
		folderMD5 = directiryWorks.getMD5Sums();
		isDelete = context.getSharedPreferences(UNLConstants.APP_PREF,
				Context.MODE_PRIVATE).getBoolean("isDeleting", false);

		if (isDelete) {
			Log.d("md5", "all MD5");
			downCount++;
			if (downCount == listwoDuplicates.size() - 1) {
				cancel(true);
			}
		} else {
			final Set<String> urlSet = new HashSet<String>();
			urlSet.add("");
			final Set<String> urlSetRec = new HashSet<String>(
					Arrays.asList(context
							.getSharedPreferences(UNLConstants.APP_PREF,
									Context.MODE_PRIVATE).getString("urls", "")
							.replace("[", "").replace("]", "").split(",")));
			final SharedPreferences.Editor editor = context
					.getSharedPreferences(UNLConstants.APP_PREF,
							Context.MODE_PRIVATE).edit();
			editor.putBoolean("isDownload", true);
			editor.commit();

			final LinkedHashSet<GDFile> listToSet = new LinkedHashSet<GDFile>(
					gdFiles);

			listwoDuplicates = new ArrayList<GDFile>(listToSet);

			Log.d("drive files", String.valueOf(listwoDuplicates.size()));
			Log.d("md5", String.valueOf(serverMD5.size()));
			String[] urls = urlSetRec.toArray(new String[urlSetRec.size()]);
			for (int i = 0; i < urls.length; i++) {
				if (urls[i].startsWith(" ")) {
					urls[i] = urls[i].substring(0, urls[i].length() - 1);
				}
			}

			Collections.sort(listwoDuplicates, new Comparator<GDFile>() {
				/**
				 * @see Comparator<T>#compare(T lhs,T rhs)
				 */
				@Override
				public int compare(final GDFile lhs, final GDFile rhs) {
					return lhs.getGdFileName().compareTo(rhs.getGdFileName());
				}
			});
			Log.d("files", listwoDuplicates.toString());

			while (downCount < listwoDuplicates.size()) {
				try {
					Log.d("count", String.valueOf(downCount));
					downloadFile(drive, listwoDuplicates.get(downCount)
							.getGdFileInst());
				} catch (Exception e) {
					Log.d("exc", e.getLocalizedMessage());
					downCount++;
				}

			}

		}
		return null;
	}

	/**
	 * Method for dowloading files via FileOutputStream
	 * 
	 * @see {@link FileOutputStream}
	 * 
	 * @param service
	 *            Reference to Google Drive file
	 * @param file
	 *            Google Drive file instance for file which we want to download
	 */
	private void downloadFile(final Drive service, final File file) {
		final DirectiryWorks directiryWorks = new DirectiryWorks(
				UNLConstants.VIDEO_DIR);
		folderMD5 = directiryWorks.getMD5Sums();
		Log.d("down", "start down file");
		if (folderMD5.containsAll(serverMD5)
				&& folderMD5.size() == serverMD5.size()) {
			cancel(true);
			downCount++;
		} else {
			if (folderMD5.contains(file.getMd5Checksum())) {
				Log.d("already down", String.valueOf(downCount));
				downCount++;
				return;
			} else {
				if (file.getDownloadUrl() != null
						&& file.getDownloadUrl().length() > 0) {
					try {
						final HttpResponse resp = service
								.getRequestFactory()
								.buildGetRequest(
										new GenericUrl(file.getDownloadUrl()))
								.execute();
						final String rootSD = Environment
								.getExternalStorageDirectory().toString()
								+ java.io.File.separator
								+ UNLConstants.VIDEO_DIR;
						final String path = file.getTitle();

						final java.io.File downFile = new java.io.File(rootSD,
								path);
						Log.d("down", downFile.getAbsolutePath());
						if (!downFile.exists()) {
							output = new FileOutputStream(downFile);
							final int bufferSize = BUFFER_SIZE;
							final byte[] buffer = new byte[bufferSize];
							int len = 0;
							while ((len = resp.getContent().read(buffer)) != -1) {
								output.write(buffer, 0, len);
							}

							output.flush();
							output.close();
							downCount++;

							Log.d("count down complete",
									String.valueOf(downCount));
							return;

						}

					} catch (IOException e) {
						downCount++;
						Log.d("count exc", String.valueOf(downCount));
						Log.d("exc", e.getLocalizedMessage());
						return;
					}
				}
			}
		}
	}

	/**
	 * @see AsyncTask#onPostExecute()
	 */
	@Override
	protected void onPostExecute(final Void result) {
		super.onPostExecute(result);
		stopDownload();
		if (!isDelete) {
			final DeleteBrokeFilesTask brokeFilesTask = new DeleteBrokeFilesTask(
					context);
			brokeFilesTask.execute();
		}
	}

	/**
	 * @see AsyncTask#onCancelled()
	 */
	@Override
	protected void onCancelled() {
		super.onCancelled();
		stopDownload();
		if (!isDelete) {
			final DeleteBrokeFilesTask brokeFilesTask = new DeleteBrokeFilesTask(
					context);
			brokeFilesTask.execute();
		}
	}

	/**
	 * Stopping downloading proccess
	 */
	private void stopDownload() {
		final SharedPreferences.Editor editor = context.getSharedPreferences(
				UNLConstants.APP_PREF, Context.MODE_PRIVATE).edit();
		editor.putBoolean("isDownload", false);
		editor.commit();
	}

	/**
	 * Getter for Instance of Google Drive class
	 * 
	 * @param credential
	 *            Instance of class for work with Google Drive Account
	 * @return Instance of Google Drive class
	 * 
	 */
	private Drive getDriveService(final GoogleAccountCredential credential) {
		return new Drive.Builder(AndroidHttp.newCompatibleTransport(),
				new GsonFactory(), credential).build();
	}

	/**
	 * Download file via @see {@link DownloadManager} from Android SDK
	 * 
	 * @param url
	 *            Path to downloading file
	 * @param destanation
	 *            Path for saving file
	 */
	@SuppressWarnings("unused")
	@Deprecated
	private void downloadViaManager(final String url, final String destanation) {
		final Uri downUrl = Uri.parse(url);
		final Uri destUri = Uri.parse(destanation);

		final DownloadManager.Request request = new Request(downUrl);
		request.setVisibleInDownloadsUi(false);
		request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
		request.setDestinationUri(destUri);
		final long downID = downManager.enqueue(request);

		final Editor editor = prefs.edit();
		editor.putLong(UNLConstants.DOWN_ID, downID);
		editor.commit();
	}
}
