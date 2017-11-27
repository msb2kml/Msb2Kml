package org.js.msb2kml.BrowseLog;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by js on 2/6/17.
 */

public class filterMeta implements FilenameFilter{

    private Pattern mask;

    public filterMeta(String m) {
        mask=Pattern.compile(m);
    }

    public boolean accept(File dir, String name) {
        Matcher mat=mask.matcher(name);
        return mat.matches();
    }
}
