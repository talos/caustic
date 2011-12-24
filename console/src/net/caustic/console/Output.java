package net.caustic.console;

import net.caustic.util.StringUtils;

class Output {

	static void print(String parentId, String id, String name, String value) {
		System.out.println(StringUtils.join(new String[] { parentId, id, name, value }, "\t"));
	}
}
