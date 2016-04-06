package mobi.esys.tasks;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mobi.esys.constants.UNLConsts;
import mobi.esys.fileworks.DirectoryWorks;
import mobi.esys.upnews_lite.UNLApp;

/**
 * Created by ZeyUzh on 01.04.2016.
 */
public class GetMACsTask implements Runnable {

    private final static String TAG = "unTag_GetArMACs";

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int NB_THREADS = CPU_COUNT * 2 + 1;

    private final static String MAC_RE = "^%s\\s+0x1\\s+0x2\\s+([:0-9a-fA-F]+)\\s+\\*\\s+\\w+$";
    private final static int BUF = 8 * 1024;

    private String thisDeviceIP = "";
    private String netIP = "";
    private List<String> macs;

    public GetMACsTask(String incNetIP) {
        thisDeviceIP = incNetIP;
        macs = new ArrayList<>();
    }

    @Override
    public void run() {
        if (thisDeviceIP != null && !thisDeviceIP.isEmpty()) {
            doScan();
        } else {
            Log.i(TAG, "Can't get this device IP");
        }
    }

    private void doScan() {
        Log.i(TAG, "Start scanning Network (" + NB_THREADS + " threads)");

        netIP = thisDeviceIP.substring(0, thisDeviceIP.lastIndexOf(".") + 1);

        ExecutorService executor = Executors.newFixedThreadPool(NB_THREADS);
        int MAX_RANGE = 255;
        for (int dest = 1; dest < MAX_RANGE; dest++) {
            String host = netIP + dest;
            if (!host.equals(thisDeviceIP)) {
                executor.execute(pingRunnable(host));
            }
        }

        Log.i(TAG, "Waiting for executor to terminate...");
        executor.shutdown();
        try {
            executor.awaitTermination(100 * 1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignored) {
        }

        Log.d(TAG, "Scan finished. MACs: " + macs.toString());

        //write data to file
        if (macs.size() > 0) {
            //prepare statistic file
            DirectoryWorks directoryWorks = new DirectoryWorks(UNLConsts.NETWORK_STATISTICS_DIR_NAME);
            File statisticFile = directoryWorks.checkLastNetworkStatisticFile();

            //get current time
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            String currentTime = sdf.format(new Date());

            //write
            try {
                UNLApp.setIsStatNetFileWriting(true);
                BufferedWriter output = new BufferedWriter(new FileWriter(statisticFile, true));
                for(int i=0;i<macs.size();i++){
                    output.newLine();
                    output.append(currentTime + "," + macs.get(i));
                }
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                UNLApp.setIsStatNetFileWriting(false);
            }
        }
    }

    private Runnable pingRunnable(final String host) {
        return new Runnable() {
            public void run() {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                try {
                    InetAddress inet = InetAddress.getByName(host);
                    boolean reachable = inet.isReachable(1000);
                    if (reachable) {
                        String mac = getHardwareAddress(host);
                        addMacToList(mac);

                        Log.d(TAG, host + " => Result: reachable. MAC: " + mac);

                        //String name = inet.getHostName();
                        //Log.d(TAG, host + " => Result: reachable. MAC: " + mac + " Host name: " + name);
                    } else {
                        //Log.d(TAG, host + " => Result: not reachable");
                    }
                } catch (UnknownHostException e) {
                    Log.e(TAG, host + " not found", e);
                } catch (IOException e) {
                    Log.e(TAG, host + " IO Error", e);
                }
            }
        };
    }

    private static String getHardwareAddress(String ip) {
        String hw = "UNKNOWN";
        BufferedReader bufferedReader = null;
        try {
            if (ip != null) {
                String ptrn = String.format(MAC_RE, ip.replace(".", "\\."));
                Pattern pattern = Pattern.compile(ptrn);
                bufferedReader = new BufferedReader(new FileReader("/proc/net/arp"), BUF);
                String line;
                Matcher matcher;
                while ((line = bufferedReader.readLine()) != null) {
                    //Log.e(TAG, ip + " line = " + line);
                    matcher = pattern.matcher(line);
                    if (matcher.matches()) {
                        hw = matcher.group(1);
                        break;
                    }
                }
            } else {
                Log.e(TAG, "ip is null");
            }
        } catch (IOException e) {
            Log.e(TAG, "Can't open/read file ARP: " + e.getMessage());
            return hw;
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        return hw;
    }

    private synchronized void addMacToList(String adding) {
        macs.add(adding);
    }

}
