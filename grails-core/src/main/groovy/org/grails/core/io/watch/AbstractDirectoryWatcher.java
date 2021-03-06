package org.grails.core.io.watch;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.util.StringUtils;

/**
 * Backend for {@link DirectoryWatcher}
 * @author Craig Andrews
 * @since 2.4
 * @see WatchServiceDirectoryWatcher
 * @see PollingDirectoryWatcher
 * @see DirectoryWatcher
 */
abstract class AbstractDirectoryWatcher implements Runnable {
    private List<DirectoryWatcher.FileChangeListener> listeners = new ArrayList<DirectoryWatcher.FileChangeListener>();
    volatile protected boolean active = true; //must be volatile as it's read by multiple threads and the value should be reflected in all of them
    protected long sleepTime = 3000;

    /**
     * Sets whether to stop the directory watcher
     *
     * @param active False if you want to stop watching
     */
    public void setActive(boolean active) {
    	this.active = active;
    }

    /**
     * Sets the amount of time to sleep between checks
     *
     * @param sleepTime The sleep time
     */
    public void setSleepTime(long sleepTime) {
        this.sleepTime = sleepTime;
    }

    /**
     * Adds a file listener that can react to change events
     *
     * @param listener The file listener
     */
    public void addListener(DirectoryWatcher.FileChangeListener listener) {
        listeners.add(listener);
    }

    /**
     * Adds a file to the watch list
     *
     * @param fileToWatch The file to watch
     */
    public abstract void addWatchFile(File fileToWatch);

    /**
     * Adds a directory to watch for the given file and extensions.
     * No String in the fileExtensions list can start with a dot (DirectoryWatcher guarantees that)
     *
     * @param dir The directory
     * @param fileExtensions The extensions
     */
    public abstract void addWatchDirectory(File dir, List<String> fileExtensions);

    protected void fireOnChange(File file) {
        for (DirectoryWatcher.FileChangeListener listener : listeners) {
            listener.onChange(file);
        }
    }

    protected void fireOnNew(File file) {
        for (DirectoryWatcher.FileChangeListener listener : listeners) {
            listener.onNew(file);
        }
    }

    protected boolean isValidDirectoryToMonitor(File file){
    	return file.isDirectory() && ! file.isHidden() && !DirectoryWatcher.SVN_DIR_NAME.equals(file.getName());
    }

    protected boolean isValidFileToMonitor(File file, Collection<String> fileExtensions) {
        String name = file.getName();
        String path = file.getAbsolutePath();
        boolean isSvnFile = path.indexOf(File.separator + DirectoryWatcher.SVN_DIR_NAME + File.separator) > 0;
        return !isSvnFile &&
        		!file.isDirectory() &&
                !file.isHidden() &&
                !file.getName().startsWith(".") &&
                (fileExtensions.contains("*") || fileExtensions.contains(StringUtils.getFilenameExtension(name)));
    }

}
