package filescanner.test.com.androidfilescanner.Model;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by bharanicharan.ms on 2/17/2017.
 */

public class FilesScanResult implements Serializable {

    public Long aLongAvgFileSize;
    public HashMap<String, Long> aTop10BiggestFiles;
    public HashMap<String, Integer> aTopFrequentFiles;

    public FilesScanResult(Long avgFileSize, HashMap<String, Long> topBigFiles, HashMap<String, Integer> topFrequentfiles) {
        this.aLongAvgFileSize = avgFileSize;
        this.aTop10BiggestFiles = topBigFiles;
        this.aTopFrequentFiles = topFrequentfiles;
    }
}
