package org.mymediadb.scrobbler.internal.parsers;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class VideoHashParserTest {
    private VideoHashParser testObj;

    @Before
    public void setup() {
        testObj = new VideoHashParser(new File(NfoParserTest.class.getClassLoader().getResource("movies/hashmovie/breakdance.avi").getFile()));
    }

    @Test
    public void testVideoHashParsing() throws Exception {
        assertEquals("8e245d9679d31e12", testObj.getHash());
    }

    @Test
    public void testIsVideoFile(){
        String filename = "breakdance.avi";
        assertTrue(VideoHashParser.isVideoHashFile(filename));
    }
}
