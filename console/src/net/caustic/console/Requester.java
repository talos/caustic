package net.caustic.console;

import net.caustic.Response;

public interface Requester {

	public abstract void finishedLoad(RunnableRequest rRequest, Response.DoneLoad response);
	public abstract void finishedFind(RunnableRequest rRequest, Response.DoneFind response);
	public abstract void finishedReference(RunnableRequest rRequest, Response.Reference response);

	public abstract void loadQueue(RunnableRequest rRequest, Response.Wait response);

	public abstract void missingTagsQueue(RunnableRequest rRequest, Response.MissingTags response);

	public abstract void stuck(RunnableRequest rRequest, Response.MissingTags response);

	public abstract void failed(RunnableRequest rRequest, Response.Failed response);

	public abstract void interrupt(Throwable why);

}