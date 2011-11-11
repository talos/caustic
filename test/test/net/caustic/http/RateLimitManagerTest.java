package net.caustic.http;

import static org.junit.Assert.*;
import static net.caustic.http.RateLimitManager.*;
import static net.caustic.util.TestUtils.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import net.caustic.http.RateLimitManager;

import org.junit.Before;
import org.junit.Test;

public class RateLimitManagerTest {
	
	private RateLimitManager manager;
	
	/**
	 * Private class to test rate limiting between threads.  When {@link #run()},
	 * counts the number of consecutive delays from {@link #manager}.
	 * @author talos
	 *
	 */
	private static class RateLimitTestThread extends Thread {
		private final RateLimitManager manager;
		private final int delayTime;
		private final String url;
		private int numDelays = 0;
		private boolean interrupted = false;
		/**
		 * @param manager The {@link RateLimitManager} to use.
		 * @param delayTime The number of milliseconds to wait between
		 * tries.
		 * @param url The {@link String} url to use when checking.
		 */
		public RateLimitTestThread(RateLimitManager manager, int delayTime, String url) {
			this.manager = manager;
			this.delayTime = delayTime;
			this.url = url;
		}
		
		public void run() {
			try {
				while(manager.shouldDelay(url)) {
					numDelays++;
					Thread.sleep(delayTime);
				}
			} catch(InterruptedException e) {
				interrupted = true;
			}
		}
		
		/**
		 * 
		 * @return How many times {@link #manager} delayed.
		 * @throws InterruptedException If the thread was
		 * interrupted.
		 */
		public int getNumDelays() throws InterruptedException {
			if(interrupted == false) {
				return numDelays;
			} else {
				throw new InterruptedException();
			}
		}
	}
	
	@Before
	public void setUp() throws Exception {
		manager = new RateLimitManager(new JavaNetHttpUtils());
	}
	
	public void testRememberRequestDelaysForOneHost(int requestWait) throws Exception {
		String url = "http://www.host.com/";
		manager.setMinRequestWait(requestWait);
		
		assertFalse("First rate limit obey shouldn't cause delay.", manager.shouldDelay(url));
		//manager.rememberRequest(url);
		assertTrue("Second request to one host should cause delay.", manager.shouldDelay(url));
	}

	@Test
	public void testRememberRequestDelaysForOneHostShortWait() throws Exception {
		testRememberRequestDelaysForOneHost(10);
	}
	
	@Test
	public void testRememberRequestDelaysForOneHostDefaultWait() throws Exception {
		testRememberRequestDelaysForOneHost(DEFAULT_REQUEST_WAIT);
	}
	
	@Test
	public void testRememberRequestDelaysForOneHostThreaded () throws Exception {
		final String url = "http://www.host.com/";
		final int requestWait = 20;
		manager.setMinRequestWait(requestWait);
		
		RateLimitTestThread thread1 = new RateLimitTestThread(manager, requestWait, url);
		RateLimitTestThread thread2 = new RateLimitTestThread(manager, requestWait, url);
		RateLimitTestThread thread3 = new RateLimitTestThread(manager, requestWait, url);
		RateLimitTestThread thread4 = new RateLimitTestThread(manager, requestWait, url);
		
		thread1.start();
		thread2.start();
		thread3.start();
		thread4.start();
		
		thread1.join();
		thread2.join();
		thread3.join();
		thread4.join();
		
		Set<Integer> setResults = new HashSet<Integer>();
		setResults.addAll(Arrays.asList(
				thread1.getNumDelays(),
				thread2.getNumDelays(),
				thread3.getNumDelays(),
				thread4.getNumDelays()));
		assertEquals("Each thread should have been delayed a different number of times.",
				4, setResults.size());
	}
	
	public void testRememberResponseDelaysForOneHost(int rateLimit) throws Exception {
		String url = "http://www.google.com/";
		manager.setRateLimit(rateLimit);
				
		assertFalse("First rate limit obey shouldn't cause delay.", manager.shouldDelay(url));
		manager.rememberResponse(url, rateLimit);
		assertTrue("Response load from this host should be delayed.", manager.shouldDelay(url));
	}
	
	@Test
	public void testRememberResponseDelaysForOneHostTinyRateLimit() throws Exception {
		testRememberRequestDelaysForOneHost(1);
	}
	
	@Test
	public void testRememberResponseDelaysForOneHostDefaultRateLimit() throws Exception {
		testRememberRequestDelaysForOneHost(DEFAULT_RATE_LIMIT);
	}
	
	@Test
	public void testRememberResponseDelaysForHostWithDifferentPaths() throws Exception {
		String url = "http://www.google.com/";
		
		manager.shouldDelay(url);
		assertTrue("Response load from this host should be delayed.", manager.shouldDelay(url + randomString()));
	}
}
