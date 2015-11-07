package mobi.esys.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import mobi.esys.constants.UNLConstants;
import mobi.esys.fileworks.DirectiryWorks;
import mobi.esys.playback.UNLPlayback;
import mobi.esys.upnews_server.UNLServer;
import mobi.esys.upnews_lite.FullscreenActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

/**
 * Background task for downloading broken files(defined by md5 sums)
 * 
 * @author �����
 * @since 1.0
 */
public class DeleteBrokeFilesTask extends AsyncTask<Void, Void, Void> {
	/**
	 * Instance for parent activity
	 */
	private transient final Context context;
	/**
	 * Instance for playback class
	 * 
	 * @see {@link UNLPlayback}
	 */
	private transient UNLPlayback playback;
	/**
	 * Instance for server class
	 * 
	 * @see {@link UNLServer}
	 */
	private transient final UNLServer k2Server;
	/**
	 * List of md5 sums
	 */
	private transient Set<String> md5set;
	/**
	 * Instance for access to secure settings
	 */
	private transient final SharedPreferences prefs;
	/**
	 * Instance for DirectoryWorks files
	 * 
	 * @see {@link DirectiryWorks}
	 */
	private transient DirectiryWorks directiryWorks;

	/**
	 * Constructor
	 * 
	 * @param context
	 * 
	 *            Instance for parent activity
	 *
	 */
	public DeleteBrokeFilesTask(final Context context) {
		super();
		this.context = context;
		this.k2Server = new UNLServer(context);
		this.prefs = context.getSharedPreferences(UNLConstants.APP_PREF,
				Context.MODE_PRIVATE);
	}

	/**
	 * Constructor
	 * 
	 * @param context
	 * 
	 *            Instance for parent activity
	 * 
	 * @param playback
	 * 
	 *            Instance for playback class
	 * 
	 * @see {@link UNLPlayback}
	 */
	public DeleteBrokeFilesTask(final Context context, final UNLPlayback playback) {
		super();
		this.context = context;
		this.k2Server = new UNLServer(context);
		this.prefs = context.getSharedPreferences(UNLConstants.APP_PREF,
				Context.MODE_PRIVATE);
		this.playback = playback;
	}

	/**
	 * 
	 */
	@Override
	protected Void doInBackground(final Void... params) {
		if (prefs.getBoolean("isDownload", false)) {
			cancel(true);
		} else {
			if (playback != null) {
				((FullscreenActivity) context).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(context, "�������� ������",
								Toast.LENGTH_SHORT).show();
					}
				});
			}
			Log.d("isDelete", "isDel");
			directiryWorks = new DirectiryWorks(UNLConstants.VIDEO_DIR);
			final List<String> folderMD5s = directiryWorks.getMD5Sums();
			final List<Integer> maskList = new ArrayList<Integer>();
			md5set = k2Server.getMD5FromServer();
			final List<String> md5sList = new ArrayList<String>();
			md5sList.addAll(md5set);
			Log.d("md5 list", md5sList.toString());
			Log.d("md5 folder list", folderMD5s.toString());
			if (md5sList.isEmpty()
					&& directiryWorks.getDirFileList("del").length > 0) {
				maskList.add(0);
			} else {
				for (int i = 1; i < folderMD5s.size(); i++) {
					if (!md5sList.contains(folderMD5s.get(i))) {
						maskList.add(i);
					}
				}
			}
			Log.d("mask list task", maskList.toString());

			final SharedPreferences.Editor editor = context
					.getSharedPreferences(UNLConstants.APP_PREF,
							Context.MODE_PRIVATE).edit();
			editor.putBoolean("isDeleting", true);
			editor.commit();

			directiryWorks.deleteFilesFromDir(maskList, context);

		}
		return null;
	}

}
