/*
 * This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mymediadb.scrobbler.internal;

import org.apache.log4j.Logger;
import org.mymediadb.api.tmdb.api.TmdbApi;
import org.mymediadb.api.tmdb.model.Movie;
import org.mymediadb.scrobbler.ScannerService;
import org.mymediadb.scrobbler.internal.parsers.NfoParser;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ScannerServiceImpl implements ScannerService {

    private static final Logger log = Logger.getLogger(ScannerServiceImpl.class);
    private HashMap<File, Collection<Movie>> resultSet;
    private ArrayList<Movie> movies;
    private ScannerConfiguration config = ScannerConfiguration.getInstance();

    private static ScannerService instance = null;


    private TmdbApi tmdb;

    private ScannerServiceImpl() {
    }

    public Map<File, Collection<Movie>> findMovies(File[] files) {

        if (tmdb == null) {
            throw new IllegalStateException("tmdb api is null");
        }

        resultSet = new HashMap<File, Collection<Movie>>();

        if (files == null) {
            throw new IllegalArgumentException("files cannot be null");
        }

        log.info("Starting file scan! using configuration - " + config);
        for (File file : files) {
            if (file.isDirectory()) {
                loopThroughFileContentsOfDir(file);
            } else {
                scanFile(file);
            }
        }

        return resultSet;
    }

    private void scanFile(File file) {
        isValidFile(file);
        if (file.isDirectory()) {
            log.info("Dir: " + file.getAbsolutePath());
            if (config.isUseFolderNameForLookup()) {
                addFileToResultSet(file);
            }
            if (config.isScanRecursively()) {
                loopThroughFileContentsOfDir(file);
            }
        } else {
            checkValidMovieFile(file);
        }
    }

    private void addFileToResultSet(File file) {
        resultSet.put(file, getMovieListForFile(file));
    }

    private void checkValidMovieFile(File file) {
        if (isMovieFileExtension(file)) {
            log.info("Movie: " + file.getAbsolutePath());
            addFileToResultSet(file);
        } else if(isNfoFileExtension(file)){
	        log.info("NFO: " + file.getAbsolutePath());
	        addFileToResultSet( file );
        }
        else {
            log.info("File: " + file.getAbsolutePath());
        }
    }

	private boolean isNfoFileExtension( File file ) {
		 return file.getName().matches(config.getFileExtensionNfoRegex());
	}

	private void loopThroughFileContentsOfDir(File file) {
        for (File childFile : file.listFiles()) {
            scanFile(childFile);
        }
    }

    private boolean isMovieFileExtension(File file) {
        return file.getName().matches(config.getFileExtensionRegex());
    }

    private List<Movie> getMovieListForFile(File file) {
        try {
            movies = new ArrayList<Movie>();
            Movie movie = null;
            if (!file.isDirectory() && isMovieFileExtension( file )) {
                movie = doTmdbHashSearch(file);
                if (movie != null) {
                    movies.add(movie);
                }
            } else if(isNfoFileExtension( file )){
		        movie = doNfoSearch(file);
	        }
            if (movie == null) {
                doTmdbTextualSearch(file);
            }
            return movies;
        } catch (IOException x) {
            throw new RuntimeException(x);
        }
    }

	private Movie doNfoSearch( File file ) {
        String imdbid = new NfoParser(file).getImdbId();
        if(imdbid == null || "".equals(imdbid)) return null;
		return tmdb.getMovieApi().imdbLookup( imdbid );
	}

	private void doTmdbTextualSearch(File file) {
        String movieName = removeFileExtension(file);
        movieName = filterMovieNameForJunk(movieName);
        Collection<? extends Movie> searchResults = tmdb.getMovieApi().search(movieName);
        if (searchResults != null)
            movies.addAll(searchResults);
    }

    private String filterMovieNameForJunk(String movieName) {
        return movieName.replaceAll(config.getJunkRegex(), " ").trim();
    }

    private String removeFileExtension(File file) {
        String fileName = file.getName();
        fileName = reverseString(fileName);
        fileName = fileName.replaceFirst("^.*?\\.", "");
        fileName = reverseString(fileName);
        return fileName;
    }

    private String reverseString(String fileName) {
        return new StringBuffer(fileName).reverse().toString();
    }

    private Movie doTmdbHashSearch(File file) throws IOException {
        Movie movie;
        String hash = OpenSubtitlesHasher.computeHash(file);
        movie = tmdb.getMediaApi().getInfo(hash, file.length());
        return movie;
    }

    private void isValidFile(File file) {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("input file parameter must be a valid - " + file);
        }
    }

    public ScannerConfiguration getConfig() {
        return config;
    }

    public void setConfig(ScannerConfiguration config) {
        this.config = config;
    }

    public void setTmdbApi(TmdbApi tmdbApi) {
        this.tmdb = tmdbApi;
    }

    public static ScannerService getInstance() {
        if (instance == null)
            instance = new ScannerServiceImpl();
        return instance;
    }

}
