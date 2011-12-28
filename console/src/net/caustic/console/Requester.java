package net.caustic.console;

import net.caustic.Response;

public interface Requester {

	public abstract void finished(RunnableRequest rRequest, Response response);

	public abstract void loadQueue(RunnableRequest rRequest, Response response);

	public abstract void missingTagsQueue(RunnableRequest rRequest, Response response);

	public abstract void stuck(RunnableRequest rRequest, Response response);

	public abstract void failed(RunnableRequest rRequest, Response response);

	public abstract void interrupt(Throwable why);

}