package net.microscraper.file;

public class JavaIOFileLoaderTest extends FileLoaderTest {

	@Override
	protected FileLoader getFileLoader() throws Exception {
		return new JavaIOFileLoader();
	}

}
