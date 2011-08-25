package net.microscraper.json;

public class JSONMEObjectTest extends JsonObjectTest {

	@Override
	protected JsonParser getJSONParser() {
		return new JsonMEParser();
	}

}
