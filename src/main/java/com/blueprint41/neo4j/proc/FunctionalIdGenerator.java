package com.blueprint41.neo4j.proc;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.PerformsWrites;
import org.neo4j.procedure.Procedure;

public class FunctionalIdGenerator {
	private static final String PROP_SEQUENCE = "Sequence";
	private static final String PROP_LABEL = "Label";
	private static final String PROP_PREFIX = "Prefix";
	private static final String PROP_UID = "Uid";
	private static final int MIN_FREESPACE = 10000;
	private static final Label FUNCTIONALID_LABEL = new Label() {
		
		@Override
		public String name() {
			return "FunctionalId";
		}
	};
	private static final int MAX_BATCHSIZE = 100000;
    /*
     * The functional Id are generated for a given entity
     * The functional Id's and the latest generated value is stored in the db
     * These procedures only work when there is a corresponding FunctionalId node in the database
     * :FunctionalId
     * .Label  -- The Label/entity were the functional id is created for
     * .Prefix  -- The prefix used in the UID
     * .Sequence -- The latest number used
     * .Uid -- The latest generated functional id
     * 
     */
	@Context public GraphDatabaseAPI dbs;
	@Context public Log log;
	

	@Procedure("blueprint41.functionalid.create")
	@PerformsWrites
	public Stream<FunctionalIdStateResult> create(@Name("Label") final String entity, @Name("prefix") final String prefix, @Name("startFrom") final long startFrom) throws Exception {
		
		return createFunctionalId(entity,prefix,startFrom);
		
	}

	private synchronized Stream<FunctionalIdStateResult> createFunctionalId(final String entity,final String prefix, final long startFrom ) throws Exception {
		Node n = getEntityNode(entity);
		if (n != null) throw new Exception("There is already a FunctionalId generator defined for Label " + entity);
		if (Integer.MAX_VALUE - startFrom < MIN_FREESPACE) throw new Exception("The start value " + startFrom + " is to big there must be at least " + MIN_FREESPACE + " positions left to generate functional ids from");
	    if (prefix == null || prefix.trim().isEmpty()) throw new Exception("Prefix may not be empty");
	    n = dbs.createNode(FUNCTIONALID_LABEL);
	    n.setProperty(PROP_LABEL, entity);
	    n.setProperty(PROP_PREFIX, prefix);
	    n.setProperty(PROP_SEQUENCE, startFrom);
	    n.setProperty(PROP_UID, prefix + Hashing.encodeIdentifier(startFrom));
		FunctionalIdStateResult res = new FunctionalIdStateResult(entity,prefix,prefix + Hashing.encodeIdentifier(startFrom),startFrom);
		return Stream.of(res);
	}
	
	@Procedure("blueprint41.functionalid.next")
	@PerformsWrites
	public synchronized Stream<StringResult> nextId(@Name("Label") final String entity) throws Exception {
		
		return generateId(entity, 1, false);
		
	}
	
	@Procedure("blueprint41.functionalid.nextNumeric")
	@PerformsWrites
	public synchronized Stream<StringResult> nextNumeric(@Name("Label") final String entity) throws Exception {
		
		return generateId(entity, 1, true);
		
	}

	@Procedure("blueprint41.functionalid.nextBatch")
	@PerformsWrites
	public synchronized Stream<StringResult> nextIdBatch(@Name("Label") final String entity, @Name("batchSize") long batchSize) throws Exception {
		if (batchSize > MAX_BATCHSIZE) throw new Exception("The batchsize cannot be bigger then " + MAX_BATCHSIZE);
		
		return generateId(entity, batchSize, false);
		
	}
	
	@Procedure("blueprint41.functionalid.nextBatchNumeric")
	@PerformsWrites
	public synchronized Stream<StringResult> nextIdBatchNumeric(@Name("Label") final String entity, @Name("batchSize") long batchSize) throws Exception {
		if (batchSize > MAX_BATCHSIZE) throw new Exception("The batchsize cannot be bigger then " + MAX_BATCHSIZE);
		
		return generateId(entity, batchSize, true);
		
	}
	
	@Procedure("blueprint41.functionalid.setSequenceNumber")
	@PerformsWrites
	public synchronized Stream<StringResult> setSequenceNumber(@Name("Label") final String entity, @Name("number") long number, @Name("isNumeric") Boolean isNumeric) throws Exception {
		StringResult res = null;
		try (Transaction tx = dbs.beginTx()) {
			Node n = getEntityNode(entity);
			tx.acquireWriteLock(n);
			if (n == null) throw new Exception("No Functional Id generator is defined for Label " + entity);
			String prefix = (String) n.getProperty(PROP_PREFIX);
			n.setProperty(PROP_SEQUENCE, number);
			String uid = prefix + Hashing.encodeIdentifier(number);
			n.setProperty(PROP_UID, uid);
			
			if(isNumeric == false)
				res = new StringResult(uid);
			else
				res = new StringResult(number + "");
			tx.success();
		}
		return Stream.of(res);
	}

	
	private synchronized Stream<StringResult> generateId(final String entity, long batchSize, Boolean numeric) throws Exception {
		List<StringResult> list = new ArrayList<StringResult>();
		
		try (Transaction tx = dbs.beginTx()) {
			Node n = getEntityNode(entity);
			tx.acquireWriteLock(n);
			if (n == null) throw new Exception("No Functional Id generator is defined for Label " + entity);
			long seq = (Long) n.getProperty(PROP_SEQUENCE);
			// extra number
			String prefix = "";
			String Uid = "";
			for (long i = 0; i < batchSize; i++) {
			
				if (seq == Long.MAX_VALUE) throw new Exception("The sequence is exhausted cannot be bigger than Integer.MAX_VALUE ");
				seq++;
				
				// prefix
				StringResult res = null;
				if(numeric == false)
				{
					prefix = (String) n.getProperty(PROP_PREFIX);
					Uid = prefix + Hashing.encodeIdentifier(new Long(seq));
					res = new StringResult(Uid);
				}
				else{
					res = new StringResult(seq + "");
				}
				list.add(res);
			}
			// only the latest
			n.setProperty(PROP_SEQUENCE, seq);
			n.setProperty(PROP_UID, Uid);
			tx.success();
		}
		return list.stream();
	}
	
	
	private Node getEntityNode(final String entity) {
		Node n = dbs.findNode(FUNCTIONALID_LABEL, PROP_LABEL, entity);
		return n;
	}

	
	@Procedure("blueprint41.functionalid.current")
	public Stream<FunctionalIdStateResult> showId(@Name("Label") final String entity) {
		Node n = getEntityNode(entity);
		
		FunctionalIdStateResult res = new FunctionalIdStateResult();
		if (n != null) {
			res.Label = (String) n.getProperty(PROP_LABEL);
			res.Prefix = (String) n.getProperty(PROP_PREFIX);
			res.Sequence = (Long) n.getProperty(PROP_SEQUENCE);
			res.Uid = (String) n.getProperty(PROP_UID);
		}
		
		return Stream.of(res);	
	}
	
	@Procedure("blueprint41.hashing.decode")
	public Stream<NumericResult> decode(@Name("encodedString") final String encodedString)  throws Exception{
		NumericResult result = new NumericResult(Hashing.decodeIdentifier(encodedString));
		return Stream.of(result);
	}
	
	@Procedure("blueprint41.functionalid.list")
	public Stream<FunctionalIdStateResult> showAll() {
		
		return dbs.findNodes(FUNCTIONALID_LABEL).stream().map(new Function<Node, FunctionalIdStateResult>() {

			@Override
			public FunctionalIdStateResult apply(Node n) {
				FunctionalIdStateResult res = new FunctionalIdStateResult();
				if (n != null) {
					res.Label = (String) n.getProperty(PROP_LABEL);
					res.Prefix = (String) n.getProperty(PROP_PREFIX);
					res.Sequence = (Long) n.getProperty(PROP_SEQUENCE);
					res.Uid = (String) n.getProperty(PROP_UID);
				}
				return res;
			}
		});
	}
	
	
	@Procedure("blueprint41.functionalid.dropdefinition")
	@PerformsWrites
	public Stream<StringResult> drop(@Name("Label") final String entity) {
		StringResult res = new StringResult("");
		Node n = getEntityNode(entity);
		if (n == null) {
			res.value = "No functional id generator defined for label " + entity + ", nothing to delete ";
		} else {
			n.delete();
			res.value = "Functional id generator defined for label " + entity + " is deleted.";
		}
		
		return Stream.of(res);	
	}

}
