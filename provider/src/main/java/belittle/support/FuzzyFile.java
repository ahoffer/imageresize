package belittle.support;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.SystemUtils;

/**
 * You're out there. Somewhere. On a shell environment PATH variable. Maybe, anyway. Abstract away
 * the the platform file system dependency, at least at the high level (Windows or non-Windows) The
 * idea is to create a fuzzy file INDEPENDENTLY of the sizer and just pass the // thing to the
 * sizer.
 */
// TODO: Test glob patterns. Implement if necessary.
public class FuzzyFile {

  String posixSearchPath;
  String windowsSearchPath;
  String posixFilename;
  String windowsFilename;
  List<Path> searchPathObjects;
  File file;
  boolean isInitialized;

  public FuzzyFile(String posixPath, String posixExec, String windowsPath, String windowsExec) {
    posixSearchPath = posixPath;
    posixFilename = posixExec;
    windowsSearchPath = windowsPath;
    windowsFilename = windowsExec;
  }

  // TODO: Maybe this abstraction let's me decouple the non-java sizers from the vagaries of
  // filesystems.
  void initialize() {
    String[] items =
        isWindows() ? getWindowsSearchPath().split(";") : getPosixSearchPath().split(":");
    setSearchPathObjects(Arrays.stream(items).map(Paths::get).collect(Collectors.toList()));

    Optional<File> opt =
        getSearchPathObjects()
            .stream()
            .map(Path::toFile)
            .map(f -> new File(f, getExecutableName()))
            .filter(File::exists)
            .findFirst();

    if (opt.isPresent()) {
      file = opt.get();
    }
    isInitialized = true;
  }

  public boolean canExecute() {
    return exists() && getFile().canExecute();
  }

  public boolean exists() {
    return getFile() != null && getFile().exists();
  }

  public String getPath() {
    if (getFile() != null) {
      try {
        return getFile().getCanonicalPath();
      } catch (IOException e) {
        return null;
      }
    }
    return null;
  }

  boolean isWindows() {
    return SystemUtils.IS_OS_WINDOWS;
  }

  /*
   * Colon delimited search path for *NIX systems. Although a colon is a valid character for a POSIX filename, the colon can only be used to separate search paths.
   * @return
   */
  public String getPosixSearchPath() {
    return posixSearchPath;
  }

  public String getWindowsSearchPath() {
    return windowsSearchPath;
  }

  List<Path> getSearchPathObjects() {
    return searchPathObjects;
  }

  void setSearchPathObjects(List<Path> searchPathObjects) {
    this.searchPathObjects = searchPathObjects;
  }

  public String getPosixFilename() {
    return posixFilename;
  }

  public String getWindowsFilename() {
    return windowsFilename;
  }

  public String getExecutableName() {
    return isWindows() ? getWindowsFilename() : getPosixFilename();
  }

  /** @return the directory of the file */
  public String getParent() {
    return getFile().getParent();
  }

  File getFile() {
    if (!isInitialized) {
      initialize();
    }
    return file;
  }
}
