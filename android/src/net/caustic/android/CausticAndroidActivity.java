package net.caustic.android;

import java.util.HashMap;
import java.util.Map;

import net.caustic.Scraper;
import net.caustic.log.AndroidLogger;
import android.app.Activity;
import android.os.Bundle;

public class CausticAndroidActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Map<String, String> input = new HashMap<String, String>();
        input.put("query", "bleh");
        Scraper scraper = new Scraper();
        scraper.register(new AndroidLogger(getApplicationContext()));
        scraper.scrape("https://raw.github.com/talos/caustic/master/fixtures/json/simple-google.json",
        		input);
    }
}