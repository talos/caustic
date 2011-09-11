package net.microscraper.database.csv;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;

/**
 * A basic {@link Writer} implementation that pipes output to stdout using
 * {@link System#out}.
 * @author realest
 *
 */
class SystemOutWriter extends Writer {

	@Override
	public void close() throws IOException { }

	@Override
	public void flush() throws IOException {
		System.out.println();
	}
	
	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		System.out.print(new String(Arrays.copyOfRange(cbuf, off, off+len)));
	}
}