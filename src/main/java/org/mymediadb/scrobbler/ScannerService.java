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

package org.mymediadb.scrobbler;


import org.mymediadb.api.tmdb.api.TmdbApi;
import org.mymediadb.api.tmdb.model.Movie;
import org.mymediadb.scrobbler.internal.ScannerConfiguration;

import java.io.File;
import java.util.Collection;
import java.util.Map;


public interface ScannerService {
    Map<File, Collection<Movie>> findMovies(File[] file);

    ScannerConfiguration getConfig();

    void setConfig(ScannerConfiguration config);

    void setTmdbApi(TmdbApi tmdbApi);
}
