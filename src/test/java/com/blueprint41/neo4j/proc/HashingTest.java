package com.blueprint41.neo4j.proc;

import static org.junit.Assert.*;

import org.junit.Test;


public class HashingTest {
	
	@Test
    public void testEncodeHash() {
        try {
        	assertEquals("JQHF3N", Hashing.encodeIdentifier(0) );
        	assertEquals("I40P11", Hashing.encodeIdentifier(1) );
        	assertEquals("HUP2NO", Hashing.encodeIdentifier(2) );
        	assertEquals("JH5SQA", Hashing.encodeIdentifier(3) );
        	assertEquals("IW0B00", Hashing.encodeIdentifier(4) );
        	assertEquals("IFUKDY", Hashing.encodeIdentifier(5) );
        	assertEquals("2GM4FS", Hashing.encodeIdentifier(104) );
        	assertEquals("3HXBB0", Hashing.encodeIdentifier(667) );
        	assertEquals("GTDTCF", Hashing.encodeIdentifier(1833) );
        	assertEquals("GMJRYG", Hashing.encodeIdentifier(3512) );
        	assertEquals("03F04W", Hashing.encodeIdentifier(3988) );
        	assertEquals("8IVT2N", Hashing.encodeIdentifier(4441) );
        	assertEquals("WTFWTF", Hashing.encodeIdentifier(373803344) );
        	assertEquals("AAAAAA", Hashing.encodeIdentifier(1661196915) );
        	assertEquals("IDHASH", Hashing.encodeIdentifier(1461692503) );
        	assertEquals("FREAKY", Hashing.encodeIdentifier(1507801795) );
        	assertEquals("0CQOV1", Hashing.encodeIdentifier(2147483647) );
        	
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // testing now the errors
        try {
        	Hashing.encodeIdentifier(-10);
        	fail(" Negative identifiers should fail but didn't");
        } catch (InvalidIdentifierException e) {
        	
        }
        try {
        	Hashing.encodeIdentifier(-2147483648);
        	fail(" Negative identifiers should fail but didn't");
        } catch (InvalidIdentifierException e) {
        	
        }
    }
	
	@Test
	public void testDecodeHash() {
        try {
        	assertEquals(Hashing.decodeIdentifier("JQHF3N"), 0 );
        	assertEquals(Hashing.decodeIdentifier("I40P11"), 1 );
        	assertEquals(Hashing.decodeIdentifier("HUP2NO"), 2 );
        	assertEquals(Hashing.decodeIdentifier("JH5SQA"), 3 );
        	assertEquals(Hashing.decodeIdentifier("IW0B00"), 4 );
        	assertEquals(Hashing.decodeIdentifier("IFUKDY"), 5 );
        	assertEquals(Hashing.decodeIdentifier("2GM4FS"), 104 );
        	assertEquals(Hashing.decodeIdentifier("3HXBB0"), 667 );
        	assertEquals(Hashing.decodeIdentifier("GTDTCF"), 1833 );
        	assertEquals(Hashing.decodeIdentifier("GMJRYG"), 3512 );
        	assertEquals(Hashing.decodeIdentifier("03F04W"), 3988 );
        	assertEquals(Hashing.decodeIdentifier("8IVT2N"), 4441 );
        	assertEquals(Hashing.decodeIdentifier("WTFWTF"), 373803344 );
        	assertEquals(Hashing.decodeIdentifier("AAAAAA"), 1661196915 );
        	assertEquals(Hashing.decodeIdentifier("aaaaaa"), 1661196915 );
        	assertEquals(Hashing.decodeIdentifier("IDHASH"), 1461692503 );
        	assertEquals(Hashing.decodeIdentifier("FREAKY"), 1507801795 );
        	assertEquals(Hashing.decodeIdentifier("0CQOV1"), 2147483647 );
        	
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
		// error checking
        String[] cases = new String[] {"","A", "AA","AAA","AAAAA","AAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA","@AAAAA","AAAAA:","AA/AAA","[AAAAA","❶❷❸❹❺❻"};
        for (String cse:cases) {
	        try {
	        	Hashing.decodeIdentifier(cse);
	        	fail(" Decoding Identifier >>" + cse + "<< should fail but didn't");
	        } catch (InvalidIdentifierException e) {}
        }
	}
}
