package org.mymediadb.scrobbler.internal.parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NfoParser {

    private File file;
    private static String imdbidRegExp =  ".*(tt[0-9]{7}).*";
    private static String nfofileRegexp = "^.*((\\.nfo))$";
    private static Pattern imdbidPattern = Pattern.compile(imdbidRegExp);
    private static Pattern filepattern = Pattern.compile(nfofileRegexp);

    public static boolean isNfoFile(File file){
        return isNfoFile(file.getName());
    }

    public static boolean isNfoFile(String file){
        Matcher matches = filepattern.matcher(file);
        return matches.find();
    }

    public NfoParser(File file) {
        this.file = file;
    }

    public String getImdbId() {
        FileReader fr = null;
        BufferedReader br = null;
        try {
            fr = new FileReader(file);
            br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) {
                Matcher matches = imdbidPattern.matcher(line);
                if (matches.find()) {
                    return matches.group(1);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) br.close();
                if (fr != null) fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return null;
    }
}

