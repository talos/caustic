package net.caustic.executor;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultDesktopManager;

import mockit.Expectations;
import mockit.Mocked;
import net.caustic.database.Database;
import net.caustic.database.DatabaseException;
import net.caustic.database.DatabaseListener;
import net.caustic.database.DatabaseView;
import net.caustic.database.InMemoryDatabase;
import net.caustic.deserializer.DefaultJSONDeserializer;
import net.caustic.http.HttpBrowser;
import net.caustic.instruction.Find;
import net.caustic.instruction.Instruction;
import net.caustic.instruction.InstructionResult;
import net.caustic.instruction.SerializedInstruction;
import net.caustic.regexp.DefaultRegexpCompiler;
import net.caustic.regexp.RegexpCompiler;
import net.caustic.util.StringUtils;

import org.junit.Before;
import org.junit.Test;

public class AsyncExecutorTest {
	private static int NUM_THREADS = 10;
	private Database db;
	private RegexpCompiler compiler;
	@Mocked private HttpBrowser browser;
	@Mocked private DatabaseListener listener;
	//@Mocked private Instruction instruction;
	private InstructionResult success;
	private AsyncExecutor exc;
	private Map<String, String> input;
	/*
	@Before
	public void setUp() throws Exception {
		db = new InMemoryDatabase();
		db.addListener(listener);
		exc = new AsyncExecutor(NUM_THREADS, db);
		input = new HashMap<String, String>();
		success = InstructionResult.success("foo", new String[] { "bar" }, new Instruction[] { }, false);
		compiler = new DefaultRegexpCompiler();
	}

	@Test
	public void testExecutesInstruction() throws Exception {
		final Instruction instruction = new Find(compiler, compiler.newTemplate("foo"));
		new Expectations() {{
			db.newScope();
		}};
		exc.execute(instruction, input, "foo", browser);
		exc.join();
	}
	
	@Test
	public void testExecutesSuccessfulChildren() throws Exception {
		new Expectations() {
			Instruction child1, child2, child3;
			{
			instruction.execute("foo", (DatabaseView) any, browser);
				result = InstructionResult.success("foo", new String[] { "bar" },
						new Instruction[] { child1, child2, child3 }, false);
			child1.execute("bar", (DatabaseView) any, (HttpBrowser) any); result = success;
			child2.execute("bar", (DatabaseView) any, (HttpBrowser) any); result = success;
			child3.execute("bar", (DatabaseView) any, (HttpBrowser) any); result = success;
		}};
		
		exc.execute(instruction, input, "foo", browser);
		exc.join();
	}*/
}
