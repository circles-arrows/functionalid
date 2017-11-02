package com.blueprint41.neo4j.proc;


public class FunctionalIdStateResult {
	public String  Label;
	public String  Prefix;
	public String  Uid;
	public long    Sequence;
	public FunctionalIdStateResult() {
	}

	public FunctionalIdStateResult(String label, String prefix, String uid, long seq ) {
		this.Label = label;
		this.Prefix = prefix;
		this.Uid = uid;
		this.Sequence = seq;
	}
}
