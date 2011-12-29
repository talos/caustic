package net.caustic.console;

import net.caustic.Response;

public interface Requester {

	public abstract void finished(RunnableRequest rRequest, Response.Done response);

	public abstract void loadQueue(RunnableRequest rRequest, Response.Wait response);

	public abstract void missingTagsQueue(RunnableRequest rRequest, Response.MissingTags response);

	public abstract void stuck(RunnableRequest rRequest, Response.MissingTags response);

	public abstract void failed(RunnableRequest rRequest, Response.Failed response);

	public abstract void interrupt(Throwable why);

}