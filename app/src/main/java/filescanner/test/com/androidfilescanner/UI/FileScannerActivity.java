package filescanner.test.com.androidfilescanner.UI;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import filescanner.test.com.androidfilescanner.Model.FilesScanResult;
import filescanner.test.com.androidfilescanner.R;
import filescanner.test.com.androidfilescanner.Services.FileScannerService;

public class FileScannerActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String SCAN_RESULTS = "ScanResults";
    FrameLayout resultsFrame;
    HashMap<String, Long> hashMapTopBig = new HashMap<>();
    HashMap<String, Integer> hashMapTopExt = new HashMap<>();
    private Button btnStartScan, btnStopScan, btnShareData;
    private Intent startServiceIntent;
    private ListView filesList;
    private ResultsAdapter adapter = null;
    private StringBuffer sTopFileExt;
    private TextView txtAvgSize, txtTopExt;
    private ProgressBar showProgress;
    private boolean isConfigChanged = false;
    private Bundle handleConfigChange;
    private FilesScanResult resultObj;

    private BroadcastReceiver mFileScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            sTopFileExt.setLength(0);
            resultObj = (FilesScanResult) intent.getSerializableExtra("resultObj");
            hashMapTopBig = resultObj.aTop10BiggestFiles;
            hashMapTopExt = resultObj.aTopFrequentFiles;
            adapter = new ResultsAdapter(hashMapTopBig);
            filesList.setAdapter(adapter);
            resultsFrame.setVisibility(View.VISIBLE);

            for (Map.Entry<String, Integer> hashMap : hashMapTopExt.entrySet()) {
                sTopFileExt = sTopFileExt.append(hashMap.getKey() + "(" + hashMap.getValue() + ")" + ",");
            }
            txtTopExt.setText(sTopFileExt.toString());
            txtAvgSize.setText(resultObj.aLongAvgFileSize.toString() + " bytes");
            showProgress.setVisibility(View.GONE);

            btnShareData.setOnClickListener(FileScannerActivity.this);
            //handleConfigChange.putSerializable("ConfigChanged", resultObj);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_file_scanner);
        //handleConfigChange = new Bundle();
        btnStartScan = (Button) findViewById(R.id.btn_start_scan);
        btnStopScan = (Button) findViewById(R.id.btn_stop_scan);
        resultsFrame = (FrameLayout) findViewById(R.id.show_results);
        filesList = (ListView) findViewById(R.id.list_results);
        txtAvgSize = (TextView) findViewById(R.id.txt_avg_file_size_result);
        txtTopExt = (TextView) findViewById(R.id.txt_extn_result);
        showProgress = (ProgressBar) findViewById(R.id.scan_progress);
        btnShareData = (Button) findViewById(R.id.btn_share_result);
        sTopFileExt = new StringBuffer();
        btnStopScan.setOnClickListener(this);
        btnStartScan.setOnClickListener(this);
        startServiceIntent = new Intent(this, FileScannerService.class);

        int MyVersion = Build.VERSION.SDK_INT;
        if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (!checkIfAlreadyhavePermission()) {
                requestForSpecificPermission();
            } else {

            }
        }

        if (savedInstanceState != null) {
            resultObj = (FilesScanResult) savedInstanceState.getSerializable(SCAN_RESULTS);
            if (null != resultObj) {
                adapter = new ResultsAdapter(resultObj.aTop10BiggestFiles);
                resultsFrame.setVisibility(View.VISIBLE);
                filesList.setAdapter(adapter);
            }
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(mFileScanReceiver, new IntentFilter("FileScanResult"));
    }

    private void requestForSpecificPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 101:

                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private boolean checkIfAlreadyhavePermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public void shareResults() {
        Intent results = new Intent(Intent.ACTION_SEND);
        results.setType("message/rfc822");
        results.putExtra(Intent.EXTRA_EMAIL, new String[]{"Recipient Mail"});
        results.putExtra(Intent.EXTRA_SUBJECT, "Files Scanned");
        startActivity(Intent.createChooser(results, "Send mail..."));

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btn_start_scan:
                showProgress.setVisibility(View.VISIBLE);
                startService(startServiceIntent);
                break;

            case R.id.btn_stop_scan:
                stopService(startServiceIntent);
                break;

            case R.id.btn_share_result:
                shareResults();
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(SCAN_RESULTS, resultObj);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        stopService(startServiceIntent);
    }

    class ResultsAdapter extends BaseAdapter {

        private HashMap<String, Long> top10Files = new HashMap<>();
        private String[] mKeys;

        public ResultsAdapter(HashMap<String, Long> dataMap) {
            this.top10Files = dataMap;
            mKeys = top10Files.keySet().toArray(new String[dataMap.size()]);
        }

        @Override
        public int getCount() {
            return top10Files.size();
        }

        @Override
        public Object getItem(int i) {
            return top10Files.get(mKeys[i]);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            View v = null;
            TextView txtName = null;
            TextView txtSize = null;

            v = LayoutInflater.from(FileScannerActivity.this).inflate(R.layout.result_list_item, viewGroup, false);
            txtName = (TextView) v.findViewById(R.id.txt_name);
            txtSize = (TextView) v.findViewById(R.id.txt_size_value);

            String key = mKeys[i];
            txtName.setText(key);
            txtSize.setText("" + getItem(i).toString());

            return v;
        }
    }
}
