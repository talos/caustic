package net.caustic.database;

import java.io.IOException;
import java.io.Writer;

/**
 * A {@link Writer} implementation that writes to {@link System#out}.
 * @author talos
 *
 */
public class SystemOutWriter extends Writer {

	@Override
	public void close() throws IOException { }

	@Override
	public void flush() throws IOException { }

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		System.out.print(new String(cbuf, off, len));
	}

}
