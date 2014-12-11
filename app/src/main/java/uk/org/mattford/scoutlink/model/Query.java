package uk.org.mattford.scoutlink.model;

public class Query extends Conversation {

	public Query(String name) {
		super(name);
		setType(TYPE_QUERY);
	}

}
