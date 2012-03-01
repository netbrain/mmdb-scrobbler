package org.mymediadb.scrobbler.internal.parsers;

import org.apache.log4j.LogSF;
import org.apache.log4j.Logger;
import org.mymediadb.scrobbler.internal.OpenSubtitlesHasher;

import javax.security.auth.login.FailedLoginException;
import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

public class VideoHashParser {

    public static String videoFileregexp = "^.*((\\.avi)|(\\.mkv)|(\\.iso))$";
    public static Pattern videofilePattern = Pattern.compile(videoFileregexp);

    private File file;
    private static final Logger log = Logger.getLogger(VideoHashParser.class);

    public VideoHashParser(File file) {
        this.file = file;
    }

    public static boolean isVideoHashFile(File file) {
        return isVideoHashFile(file.getName());
    }

    public static boolean isVideoHashFile(String file) {
        return videofilePattern.matcher(file).find();
    }

    public String getHash() {
        try {
            return OpenSubtitlesHasher.computeHash(file);
        } catch (IOException e) {
            log.info("Failed to parse file " + file);
        }
        return null;
    }
}
