package net.microscraper.client.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import net.microscraper.client.Utils;
import net.microscraper.client.impl.CachedURILoader;
import net.microscraper.client.interfaces.Browser;
import net.microscraper.client.interfaces.BrowserDelayException;
import net.microscraper.client.interfaces.NetInterface;
import net.microscraper.client.interfaces.NetInterfaceException;
import net.microscraper.client.interfaces.URIInterface;

/**
 * This {@link CachedURILoader} supports 
 * @author john
 *
 */
public class UtilityURILoader extends CachedURILoader {
	private static final int SLEEP_TIME = 100;
	
	private final Browser browser;
	private final NetInterface netInterface;
	/**
	 * 
	 * @param netInterface the {@link NetInterface} to use.
	 */
	public UtilityURILoader(NetInterface netInterface) {
		this.netInterface = netInterface;
		this.browser = netInterface.getBrowser();
	}
	
	@Override
	protected String loadNew(URIInterface location) throws IOException {
		try {
			// URI is absolute if it has a scheme.
			if(location.isAbsolute()) {
				String scheme = location.getScheme();			
				if(scheme.equals("file")) {
					return loadFile(location);
				} else if(scheme.equals("http")) {
					return loadHTTP(location);
				} else {
					throw new IOException("Unrecognized URI scheme " + Utils.quote(scheme));				
				}
			} else { // Resolve local URI against file we are executing from.
				return loadFile(location);
			}
		} catch(NetInterfaceException e) {
			throw new IOException("Could not load " + Utils.quote(location.toString()), e);
		}
	}
	
	private String loadHTTP(URIInterface httpLocation) throws NetInterfaceException {
		try {
			return browser.get(false, netInterface.makeURL(httpLocation.toString()), null, null, null);
		} catch(BrowserDelayException delay) {
			//interfaces.log.e(interrupt);
			try {
				Thread.sleep(SLEEP_TIME);
			} catch(InterruptedException e) {
				throw new NetInterfaceException(e);
			}
			return loadHTTP(httpLocation);
		}
	}
	
	private String loadFile(URIInterface fileLocation) throws IOException {
		return loadFile(fileLocation.getSchemeSpecificPart());
	}
	
	private String loadFile(String path) throws IOException {
		//File file = new File(new URI(fileLocation.getScheme(), fileLocation.getSchemeSpecificPart(), null));
		File file = new File(path);
		FileInputStream fileInputStream = new FileInputStream(file);
		byte[] buffer = new byte[(int) file.length()];  
		fileInputStream.read(buffer);
		return new String(buffer);
	}
}
