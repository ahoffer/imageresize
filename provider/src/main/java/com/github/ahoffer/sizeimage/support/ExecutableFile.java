package com.github.ahoffer.sizeimage.support;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.SystemUtils;

public class ExecutableFile {

  String posixSearchPath;
  String windowsSearchPath;
  String posixExecutableName;
  String windowsExecutableName;
  List<Path> searchPathObjects;
  File file;
  boolean isInitialized;

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
    if (!isInitialized) {
      initialize();
    }
    return file != null && file.canExecute();
  }

  public String getPath() {
    if (!isInitialized) {
      initialize();
    }
    if (file != null) {
      try {
        return file.getCanonicalPath();
      } catch (IOException e) {
        return null;
      }
    }
    return null;
  }

  boolean canExecute(File e) {
    return e.canExecute();
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

  public List<Path> getSearchPathObjects() {
    return searchPathObjects;
  }

  public void setSearchPathObjects(List<Path> searchPathObjects) {
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
}
