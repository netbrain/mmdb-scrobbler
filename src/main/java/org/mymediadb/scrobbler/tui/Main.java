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

package org.mymediadb.scrobbler.tui;


import org.mymediadb.api.mmdb.api.MmdbApi;
import org.mymediadb.api.mmdb.internal.api.MmdbApiImpl;
import org.mymediadb.api.mmdb.internal.model.UserMediaImpl;
import org.mymediadb.api.mmdb.model.UserMedia;
import org.mymediadb.api.tmdb.api.TmdbApi;
import org.mymediadb.api.tmdb.internal.api.TmdbApiImpl;
import org.mymediadb.api.tmdb.model.Movie;
import org.mymediadb.scrobbler.ScannerService;
import org.mymediadb.scrobbler.internal.ScannerConfiguration;
import org.mymediadb.scrobbler.internal.ScannerServiceImpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class Main {

    private static ScannerConfiguration config;
    private static ScannerService scanner = ScannerServiceImpl.getInstance();
    private static Map<File, Collection<Movie>> results = new HashMap<File, Collection<Movie>>();
    private static MmdbApi mmdbApi;
    private static boolean interactive = true;

    static {
        config = ScannerConfiguration.getInstance();
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
        } else {
            TmdbApi tmdbApi = TmdbApiImpl.getInstance();
            mmdbApi = MmdbApiImpl.getInstance();
            scanner.setTmdbApi(tmdbApi);

            String[] filePaths;
            filePaths = handleInputArguments(args, tmdbApi);
            if (filePaths.length == 0) {
                System.out.println("at least one file path should be specified!");
                printUsage();
            } else {
                scanFilePaths(filePaths);
                submitToMmdb();
            }
        }
    }

    private static String[] handleInputArguments(String[] args, TmdbApi tmdbApi) {
        String[] filePaths = new String[0];
        for (int x = 0; x < args.length; x++) {
            try {
                String arg = args[x];
                if (arg.equals("-r")) {
                    config.setScanRecursively(true);
                } else if (arg.equals("-f")) {
                    config.setUseFolderNameForLookup(false);
                } else if (arg.equals("-ext-regex")) {
                    config.setFileExtensionRegex(args[++x]);
                } else if (arg.equals("-junk-regex")) {
                    config.setJunkRegex(args[++x]);
                } else if (arg.equals("-api-key")) {
                    tmdbApi.setApiKey(args[++x]);
                } else if (arg.equals("-mmdb-auth")) {
                    mmdbApi.setBasicAuthentication(args[++x], args[++x]);
                } else if (arg.equals("-i")) {
                    interactive = false;
                } else {
                    filePaths = Arrays.copyOfRange(args, x, args.length);
                    break;

                }
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("insufficient number of arguments supplied");
            }
        }
        return filePaths;
    }

    private static void submitToMmdb() {
        System.out.println("\nConnecting to MMDB");
        for (File file : results.keySet()) {
            if (!interactive) {
                Movie movie = getFirstMovieResult(file);
                if (movie != null) {
                    setMovieToAcquired(movie);
                }
            } else {
                if (!results.get(file).isEmpty()) {
                    int x = 1;
                    System.out.println(file);

                    for (Movie movie : results.get(file)) {
                        System.out.println("\t[" + (x++) + "] - " + movie);
                    }
                    System.out.print("Which movie should i submit to mmdb? (s to skip, x to exit, 1 - " + (--x) + ")? : ");
                    interactivelySetMovieToAcquired(file);
                }
            }
        }
    }

    private static void interactivelySetMovieToAcquired(File file) {
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
        String inputLine;
        try {
            inputLine = inputReader.readLine();
            if (inputLine.equalsIgnoreCase("x")) {
                System.exit(0);
            } else if (inputLine.equalsIgnoreCase("s")) {
                inputLine = null;
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        if (inputLine != null) {
            try {
                int offset = Integer.parseInt(inputLine);
                Iterator<Movie> i = results.get(file).iterator();
                while (offset > 1) {
                    offset--;
                    i.next();
                }
                Movie movie = i.next();
                setMovieToAcquired(movie);
            } catch (NumberFormatException e) {
                System.out.println("Unknown input, must be a number Try again:");
                interactivelySetMovieToAcquired(file);
            } catch (NoSuchElementException e) {
                System.out.println("Error in input, out of range! Try again:");
                interactivelySetMovieToAcquired(file);
            } catch (Exception x) {
                System.out.println("Unknown error occured: " + x.getMessage());
                x.printStackTrace();
            }
        }
    }

    private static Movie getFirstMovieResult(File file) {
        Collection<Movie> movies = results.get(file);
        if (!movies.isEmpty()) {
            return movies.iterator().next();
        }
        return null;
    }

    private static void setMovieToAcquired(Movie movie) {
        System.out.println("Setting " + movie + " to [ACQUIRED]");
        UserMedia userMedia = new UserMediaImpl();
        userMedia.setAcquired(true);
        mmdbApi.putUserMedia(MmdbApi.MediaType.MOVIE, MmdbApi.IdType.TMDB, movie.getId(), userMedia);
    }

    private static void scanFilePaths(String[] stringPaths) {
        scanner.setConfig(config);
        File[] filePaths = new File[stringPaths.length];
        for (int x = 0; x < stringPaths.length; x++) {
            filePaths[x] = new File(stringPaths[x]);
        }
        results.putAll(scanner.findMovies(filePaths));
        System.out.println("\nRESULTS:");

        int numFiles = results.keySet().size();
        int numMediaFound = 0;
        double numPotentialFoundAvg = 0;

        for (File file : results.keySet()) {
            System.out.println(file);
            for (Movie movie : results.get(file)) {
                System.out.println("\t" + movie);
            }
            numMediaFound += results.get(file).size() > 0 ? 1 : 0;
            numPotentialFoundAvg += results.get(file).size();
        }
        numPotentialFoundAvg /= numFiles;
        System.out.println("\n" +
                "Files: " + numFiles + "\n" +
                "Files found media for: " + numMediaFound + "\n" +
                "Average media results per file: " + numPotentialFoundAvg);
    }

    private static void printUsage() {
        System.out.println("usage:\n\t" +
                " [options] filePath [filePath ...]\n\n");
        System.out.println("options:");
        System.out.println("-r\n\tscan recursively\n");
        System.out.println("-f\n\tuse folder names for lookup\n");
        System.out.println("-i\n\tsubmit to mmdb non-interactively\n");
        System.out.println("-ext-regex 'input-regex'\n\tjava regex for which file extensions to scan\n");
        System.out.println("-junk-regex 'input-regex'\n\tjava regex to strip junk from filenames\n");
        System.out.println("-api-key 'key'\n\ttmdb api key to use\n");
        System.out.println("-mmdb-auth 'username' 'password'\n\tusername and password to use\n");
    }
}
