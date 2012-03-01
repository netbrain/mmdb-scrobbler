package org.mymediadb.scrobbler.internal.parsers;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class NfoParserTest {

    private NfoParser testObj;

    @Before
    public void setup() {
        testObj = new NfoParser(new File(NfoParserTest.class.getClassLoader().getResource("movies/testmovie/test.nfo").getFile()));
    }

    @Test
    public void testNfoFileParsing() throws Exception {
        assertEquals("tt0862467", testObj.getImdbId());
    }

    @Test
    public void testIsNfoFile(){
        String filename = "per.nfo";
        assertTrue(NfoParser.isNfoFile(filename));
    }
}
