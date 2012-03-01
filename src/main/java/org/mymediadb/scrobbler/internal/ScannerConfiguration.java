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

public class ScannerConfiguration {
    private boolean useFolderNameForLookup;
    private boolean scanRecursively;
    private String junkRegex;
    private String fileExtensionRegex;
	private String fileExtensionNfoRegex;

	private ScannerConfiguration() {
        useFolderNameForLookup = true;
        scanRecursively = false;
        junkRegex = "(?ix)((\\.)|((DVDR|720p|x264|Bluray|PAL|NTSC|UNRATED|LIMITED|REPACK|1080p|EXTENDED|DVDSCR|XVID|BRRip|RERiP|SCREENER|DC|PROPER).*))";
        fileExtensionRegex = "^.*((\\.avi)|(\\.mkv)|(\\.iso))$";
		fileExtensionNfoRegex = "^.*((\\.nfo))$";
    }

    public String getJunkRegex() {
        return junkRegex;
    }

    public void setJunkRegex(String junkRegex) {
        this.junkRegex = junkRegex;
    }

    public boolean isScanRecursively() {
        return scanRecursively;
    }

    public void setScanRecursively(boolean scanRecursively) {
        this.scanRecursively = scanRecursively;
    }

    public boolean isUseFolderNameForLookup() {
        return useFolderNameForLookup;
    }

    public void setUseFolderNameForLookup(boolean useFolderNameForLookup) {
        this.useFolderNameForLookup = useFolderNameForLookup;
    }

    public String getFileExtensionRegex() {
        return fileExtensionRegex;
    }

    public void setFileExtensionRegex(String fileExtensionRegex) {
        this.fileExtensionRegex = fileExtensionRegex;
    }

    public static ScannerConfiguration getInstance() {
        return new ScannerConfiguration();
    }

    @Override
    public String toString() {
        return "useFolderNameForLookup=" + useFolderNameForLookup + ", " +
                "scanRecursively=" + scanRecursively + ", " +
                "junkRegex=" + junkRegex + ", " +
                "fileExtensionRegex=" + fileExtensionRegex;
    }

	public String getFileExtensionNfoRegex() {
		return fileExtensionNfoRegex;
	}
}
