package filescanner.test.com.androidfilescanner.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import filescanner.test.com.androidfilescanner.Model.FilesScanResult;

/**
 * Created by bharanicharan.ms on 2/16/2017.
 */

public class FileScannerService extends Service {


    private Context mContext;
    private ArrayList<File> fileList = new ArrayList<File>();
    private File root;
    private HashMap<String, Long> fileNameSizeMap = new HashMap<String, Long>();
    private ArrayList<String> fileNamesList = new ArrayList<String>();
    private HashMap<String, Integer> fileExtMap = new HashMap<String, Integer>();
    private HashMap<String, Integer> topFrequentExtensions = new HashMap<>();
    private HashMap<String, Long> topBiggestFiles = new HashMap<>();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new ScanTask().execute();
        return Service.START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class ScanTask extends AsyncTask<Void, Void, FilesScanResult> {

        @Override
        protected void onPostExecute(FilesScanResult resultObj) {

            Intent intent = new Intent("FileScanResult");
            // You can also include some extra data.
            intent.putExtra("resultObj", resultObj);
            LocalBroadcastManager.getInstance(FileScannerService.this).sendBroadcast(intent);
            FileScannerService.this.stopSelf();
        }

        @Override
        protected FilesScanResult doInBackground(Void... voids) {

            root = new File(Environment.getExternalStorageDirectory().getAbsolutePath());

            ArrayList<File> filesList = new ArrayList<File>();
            filesList = getFiles(root);

            int counter = 0;
            StringBuffer result = null;
            Long avgFileSize = Long.valueOf(0);

            for (File f : filesList) {
                result = new StringBuffer();
                //result = result.append(counter++ + " of " + filesList.size());
                fileNameSizeMap.put(f.getName(), f.length());
                fileNamesList.add(filesList.indexOf(f), f.getName());
                avgFileSize = avgFileSize + (Long.valueOf(f.length()));
                String fileExtension = getFileExtension(f.getName());
                if (!fileExtMap.containsKey(fileExtension)) {
                    fileExtMap.put(fileExtension, 1);
                } else {
                    fileExtMap.put(fileExtension, fileExtMap.get(fileExtension) + 1);
                }
            }
            avgFileSize = avgFileSize / Long.valueOf(filesList.size());
            fileNameSizeMap = sortFileBySize(fileNameSizeMap);
            fileExtMap = sortFileByExt(fileExtMap);
            topFrequentExtensions.clear();
            topFrequentExtensions = getTopFrequentExtensions(fileExtMap);
            topBiggestFiles = getTopBiggestFiles(fileNameSizeMap);

            FilesScanResult obj = new FilesScanResult(avgFileSize, topBiggestFiles, topFrequentExtensions);

            return obj;
        }

        private HashMap<String, Integer> getTopFrequentExtensions(HashMap<String, Integer> fileExtMap) {
            HashMap<String, Integer> hashMap = new HashMap<>(5);

            int length = fileExtMap.size() > 5 ? 5 : fileExtMap.size();

            for (Map.Entry<String, Integer> entry : fileExtMap.entrySet()) {

                if (hashMap.size() == length) {
                    break;
                }
                hashMap.put(entry.getKey(), entry.getValue());
            }
            return hashMap;
        }

        private HashMap<String, Long> getTopBiggestFiles(HashMap<String, Long> fileNameSizeMap) {

            HashMap<String, Long> resultMap = new HashMap<>();

            for (Map.Entry<String, Long> entry : fileNameSizeMap.entrySet()) {
                if (resultMap.size() == 10)
                    break;
                resultMap.put(entry.getKey(), entry.getValue());
            }
            return resultMap;
        }

        private String getFileExtension(String name) {
            try {
                return name.substring(name.lastIndexOf(".") + 1);
            } catch (Exception e) {
                return "";
            }
        }


        public HashMap<String, Integer> sortFileByExt(HashMap<String, Integer> unsortedMap) {

            List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(unsortedMap.entrySet());

            // Sorting the list based on values
            Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
                public int compare(Map.Entry<String, Integer> a1,
                                   Map.Entry<String, Integer> a2) {
                    return a2.getValue().compareTo(a1.getValue());
                }
            });

            // Maintaining insertion order with the help of LinkedList
            HashMap<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
            for (Map.Entry<String, Integer> entry : list) {
                sortedMap.put(entry.getKey(), entry.getValue());
            }
            return sortedMap;
        }

        public ArrayList<File> getFiles(File dir) {
            //Log.e("Dir :" + dir.getAbsolutePath(), "dir name : " + dir.getName());
            File listFile[] = dir.listFiles();
            if (listFile != null && listFile.length > 0) {
                for (int i = 0; i < listFile.length; i++) {
                    if (listFile[i].isDirectory()) {
                        fileList.add(listFile[i]);
                        getFiles(listFile[i]);
                    } else {
                        fileList.add(listFile[i]);
                    }
                }
            }
            return fileList;
        }

        public HashMap<String, Long> sortFileBySize(HashMap<String, Long> unsortedMap) {
            List<Map.Entry<String, Long>> list = new LinkedList<Map.Entry<String, Long>>(unsortedMap.entrySet());

            // Sorting the list based on values
            Collections.sort(list, new Comparator<Map.Entry<String, Long>>() {
                public int compare(Map.Entry<String, Long> a1,
                                   Map.Entry<String, Long> a2) {
                    return a2.getValue().compareTo(a1.getValue());
                }
            });

            // Maintaining insertion order with the help of LinkedList
            HashMap<String, Long> sortedMap = new LinkedHashMap<String, Long>();
            for (Map.Entry<String, Long> entry : list) {
                sortedMap.put(entry.getKey(), entry.getValue());
            }
            return sortedMap;
        }
    }
}
