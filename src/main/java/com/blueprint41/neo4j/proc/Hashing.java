package com.blueprint41.neo4j.proc;

public class Hashing {
	private static final int[] map = new int[] { 4, 2, 3, 5, 7, 1, 0, 6, 15, 8, 13, 11, 14, 9, 12, 10 };
    private static final int[] rmap = new int[] { 6, 5, 1, 2, 0, 3, 7, 4, 9, 13, 15, 11, 14, 10, 12, 8 };
    private static final String base36Chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";


	public static String encodeIdentifier(long value) throws InvalidIdentifierException {
		if (value < 0) throw new InvalidIdentifierException("The identifier is invalid " + value);
		// scramble id space
		long output = 0;
		long input = value & 0x00000000FFFFFFFFL; // making it unsigned 
		for (int index = 7; index >= 0; index--)
        {
            int pos = index * 4;
            output = output << 4;
            int mapindex = (int)((input >> pos) & 0xf);
            output |= map[mapindex];
        }
        output ^= 0x3364abf7;	
        for (int index = 6; index > 1; index--)
        {
            int pos = index * 4;
            output ^= (output & 0x7f) << pos;
        }
        
        long output2 = output | (value & 0xFFFFFFFF00000000L);

        // convert integer to base 36 string
        char[] returnValue = new char[] { '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0' };
        for (int index = 0; index < 13; index++)
        {
        	int pos = (int)(output2 % 36);
            returnValue[12 - index] = base36Chars.charAt(pos);
            output2 = output2 / 36;
        }
        
        int skip = 0;
        while (returnValue[skip] == '0' && skip < 7)
        {
            skip++;
        }
        
        return new String(returnValue, skip, 13 - skip);

	}
	public static long decodeIdentifier(String value) throws InvalidIdentifierException {
		if (value == null)
            return -1;

        if (value.length() < 6 || value.length() > 13)
            return -1;

        // convert base 36 string to integer
        long input2 = 0;
        String upper = value.toUpperCase();
        for (int index = 0; index < value.length(); index++)
        {
            int valueindex = base36Chars.indexOf(upper.charAt(index));
            if (valueindex == -1)
            	return -1;

            input2 += (long)(valueindex * Math.pow(36, value.length() - index - 1));
        }
        
        long input = input2 & 0x00000000FFFFFFFFL;

        // unscramble ID space
        long output = 0;
        for (int index = 6; index > 1; index--)
        {
            int pos = index * 4;
            input ^= (input & 0x7f) << pos;
        }
        input ^= 0x3364abf7;
        for (int index = 7; index >= 0; index--)
        {
            int pos = index * 4;
            output = output << 4;
            int mapindex = (int)((input >> pos) & 0xf);
            output |= rmap[mapindex];
        }
        
        return (long)(output | (input2 & 0xFFFFFFFF00000000L));
	}
	public static void main(String[] args) {
		TestCase(0, "JQHF3N"); // Minimum valid value
        TestCase(1, "I40P11");
        TestCase(2, "HUP2NO");
        TestCase(3, "JH5SQA");
        TestCase(4, "IW0B00");
        TestCase(5, "IFUKDY");
        TestCase(104, "2GM4FS");
        TestCase(667, "3HXBB0");
        TestCase(1833, "GTDTCF");
        TestCase(3512, "GMJRYG");
        TestCase(3988, "03F04W");
        TestCase(4441, "8IVT2N");
        TestCase(373803344, "WTFWTF");
        TestCase(1661196915, "AAAAAA"); // Proof that a 6 character, all A's hash is valid...
        TestCase(1661196915, "aaaaaa"); // Proof that lower case works as well...
        TestCase(1461692503, "IDHASH");
        TestCase(1507801795, "FREAKY");
        TestCase(2135615189, "FUCKME");
        TestCase(2147483647, "0CQOV1"); // int.MaxValue
        TestCase(2147483649L, "1UY18DX"); // int.MaxValue + 2
        TestCase(4294967294L, "1J91FMJ"); // int.MaxValue * 2
        TestCase(9223372036854775807L, "1Y2P0IILNUWRH"); // long.MaxValue
        TestDecodeCase("1Y2P0IILNUWRHa", -1); // long.MaxValue
		System.out.println("finished");
	}
	
	public static void TestCase(long value, String expectedHash){
		try
		{
			String encoded = encodeIdentifier(value);
			long decodedValue = decodeIdentifier(encoded);
			if(!encoded.contentEquals(expectedHash.toUpperCase()))
				System.out.format("Expected : %s but was %s. \r\n", expectedHash, encoded);
			if(value != decodedValue)
				System.out.format("Expected : %d but was %d. \r\n", value, decodedValue);
		}
		catch(Exception ex)
		{
			System.out.println(ex.getMessage());
		}
	}
	
	public static void TestDecodeCase(String hash, long expectedValue){
		try
		{
			long decodedValue = decodeIdentifier(hash);
			if(expectedValue != decodedValue)
				System.out.format("Expected : %d but was %d. \r\n", expectedValue, decodedValue);
		}
		catch(Exception ex)
		{
			System.out.println(ex.getMessage());
		}
	}

}
