package com.github.ahoffer.sizeimage.support;

import com.sun.istack.internal.Nullable;
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
 * the the platform file system dependency, at least at the high level (Windows or non-Windows)
 */
public class FuzzyFile {

  String posixSearchPath;
  String windowsSearchPath;
  String posixExecutableName;
  String windowsExecutableName;
  List<Path> searchPathObjects;
  File file;
  boolean isInitialized;
  // TODO: Maybe this abstraction let's me decouple the non-java sizers from the vagaries of
  // filesystems. The idea is to create "a fuzzy file INDEPENDENTLY of the sizer and just pass the
  // thing to the sizer.
  void initialize() {
    String[] items =
        isWindows() ? getWindowsSearchPath().split(";") : getPosixSearchPath().split(":");
    setSearchPathObjects(Arrays.stream(items).map(Paths::get).collect(Collectors.toList()));

    Optional<File> opt =
        getSearchPathObjects()
            .stream()
            .map(Path::toFile)
            .map(f -> new File(f, getExecutableName()))
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

  public String getPosixSearchPath() {
    return posixSearchPath;
  }

  public void setPosixSearchPath(String posixSearchPath) {
    this.posixSearchPath = posixSearchPath;
  }

  public String getWindowsSearchPath() {
    return windowsSearchPath;
  }

  public void setWindowsSearchPath(String windowsSearchPath) {
    this.windowsSearchPath = windowsSearchPath;
  }

  List<Path> getSearchPathObjects() {
    return searchPathObjects;
  }

  void setSearchPathObjects(List<Path> searchPathObjects) {
    this.searchPathObjects = searchPathObjects;
  }

  public String getPosixExecutableName() {
    return posixExecutableName;
  }

  public void setPosixExecutableName(String posixExecutableName) {
    this.posixExecutableName = posixExecutableName;
  }

  public String getWindowsExecutableName() {
    return windowsExecutableName;
  }

  public void setWindowsExecutableName(String windowsExecutableName) {
    this.windowsExecutableName = windowsExecutableName;
  }

  public String getExecutableName() {
    return isWindows() ? getWindowsExecutableName() : getPosixExecutableName();
  }

  /** @return the directory of the file */
  public String getParent() {
    return getFile().getParent();
  }

  @Nullable
  File getFile() {
    if (!isInitialized) {
      initialize();
    }
    return file;
  }
}
