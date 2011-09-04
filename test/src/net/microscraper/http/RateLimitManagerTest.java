package net.microscraper.http;

import static org.junit.Assert.*;
import static net.microscraper.http.RateLimitManager.*;

import java.util.Date;

import net.microscraper.log.Logger;
import net.microscraper.log.SystemOutLogger;
import net.microscraper.util.JavaNetHttpUtils;

import org.junit.Before;
import org.junit.Test;

public class RateLimitManagerTest {
	
	//private @Mocked Logger log;
	private Logger log = new SystemOutLogger();
	private RateLimitManager manager;
	
	@Before
	public void setUp() throws Exception {
		log.open();
		manager = new RateLimitManager(new JavaNetHttpUtils());
	}
	
	public void testRememberRequestDelaysForOneHost(int requestWait) throws Exception {
		String url = "http://www.host.com/";
		manager.setMinRequestWait(requestWait);
		
		long start;
		
		start = new Date().getTime();
		manager.obeyRateLimit(url, log);
		assertTrue("First rate limit obey shouldn't cause delay.",
				new Date().getTime() - start < requestWait);
		manager.rememberRequest(url);
		
		start = new Date().getTime();
		manager.obeyRateLimit(url, log);
		assertTrue("Second request to one host should cause delay.",
				new Date().getTime() - start > requestWait);
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
	public void testRememberRequestDelaysForOneHostLongWait() throws Exception {
		testRememberRequestDelaysForOneHost(4000);
	}

	@Test
	public void testRememberRequestDelaysForOneHostThreaded () throws Exception {
		final String url = "http://www.host.com/";
		final int requestWait = 2000;
		manager.setMinRequestWait(requestWait);
		
		final long start = new Date().getTime();
		
		Thread thread1 = new Thread(new Runnable() {
			public void run() {
				try {
					manager.obeyRateLimit(url + "thread1", log);
					assertTrue("First rate limit obey shouldn't cause delay.",
							new Date().getTime() - start < requestWait);
					manager.rememberRequest(url + "thread1");
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		
		Thread thread2 = new Thread(new Runnable() {
			public void run() {
				try {
					manager.obeyRateLimit(url + "thread2", log);
					assertTrue("Should be delayed.",
							new Date().getTime() - start > requestWait);
					manager.rememberRequest(url + "thread2");
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		
		thread1.start();
		Thread.sleep(1);
		thread2.start();
	}
	
	public void testRememberResponseDelaysForOneHost(int rateLimit) throws Exception {
		String url = "http://www.google.com/";
		manager.setRateLimit(rateLimit);
		
		long start;
		
		start = new Date().getTime();
		manager.obeyRateLimit(url, log);
		assertTrue("First rate limit obey shouldn't cause delay.",
				new Date().getTime() - start < DEFAULT_SLEEP_TIME);
		manager.rememberResponse(url, rateLimit);

		start = new Date().getTime();
		manager.obeyRateLimit(url, log);
		assertTrue("Response load from this host shouldn't be delayed, tiny amount of data loaded.",
				new Date().getTime() - start < DEFAULT_SLEEP_TIME);
		manager.rememberResponse(url, rateLimit * 1000);
		
		start = new Date().getTime();
		manager.obeyRateLimit(url, log);
		assertTrue("Response load from this host should be delayed.",
				new Date().getTime() - start > DEFAULT_SLEEP_TIME);
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
	public void testRememberResponseDelaysForOneHostBigRateLimit() throws Exception {
		testRememberRequestDelaysForOneHost(1000);
	}
}
