package com.github.ahoffer.sizeimage.support;

import org.junit.Before;
import org.junit.Test;

public class ExecutableFileTest {

  @Before
  public void setup() {}

  @Test
  public void testWindowsPathsSpaces() {
    //    ExecutableFile spy = Mockito.spy(new ExecutableFile("test"));
    //    spy.setWindowsSearchPath("C:\foo;\"foo bar\";fizz;");
    //    Mockito.doReturn(true).when(spy).isWindows();
    //    Mockito.doReturn(true).when(spy).canExecute(Mockito.any(File.class));
    //    Optional<String> path = spy.getPath();
    // TODO: No idea what it should be if dir name has a space in it
    //    assertThat(spy.getPath(), equalTo(""));
  }

  @Test
  public void testWindowsPathsColon() {
    //    ExecutableFile spy = Mockito.spy(new ExecutableFile("test"));
    //    spy.setWindowsSearchPath("\foo:\bar");
    //    Mockito.doReturn(true).when(spy).isWindows();
    //
    //    Optional<String> path = spy.getPath();
    //    assertThat(spy.getPath().isPresent(), equalTo(false));
  }

  @Test
  public void testPosixPaths() {
    //    ExecutableFile spy = new Mockito().spy(new ExecutableFile("test"));
    //    spy.setPosixSearchPath("/foo/bar:fizz:bang/");
    //    Mockito.doReturn(false).when(spy).isWindows();
    //    Mockito.doReturn(true).when(spy).canExecute(Mockito.any(File.class));
    //    Optional<String> path = spy.getPath();
    //    assertThat(path.isPresent(), equalTo(true));
    //    assertThat(path.get(), equalTo("/foo/bar/test"));
  }
}
