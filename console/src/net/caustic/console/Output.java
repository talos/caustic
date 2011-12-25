package net.caustic.console;

import net.caustic.util.StringUtils;

public class Output {

	public Output() {
		print("scope", "source", "name", "value");
	}
	
	public void print(String scope, String source, String name, String value) {
		System.out.println(StringUtils.join(new String[] { scope, source, name, value }, "\t"));
	}
}
