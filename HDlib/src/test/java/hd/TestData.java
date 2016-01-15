package hd;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class TestData
{
	
	String roses;
	String fish;
	String sugar;
	Integer number;

	public TestData(String roses, String fish, String sugar, Integer number) 
	{
		this.roses = roses;
		this.fish = fish;
		this.sugar = sugar;
		this.number = number;
	}

	@Override
	public boolean equals(Object obj) 
	{
		if (!(obj instanceof TestData))
			return false;

		TestData td = (TestData) obj;

		return 
				this.roses.equals(td.roses) &&
				this.fish.equals(td.fish) &&
				this.sugar.equals(td.sugar) &&
				this.number == td.number;

	}				
	
	static class TestDataDeserializer implements JsonDeserializer<TestData>
	{
	    @Override
	    public TestData deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
	                              throws JsonParseException
	    {
	        String roses = je.getAsJsonObject().get("roses").getAsString();
	        String fish = je.getAsJsonObject().get("fish").getAsString();
	        String sugar = je.getAsJsonObject().get("sugar").getAsString();
	        Integer number = je.getAsJsonObject().get("number").getAsInt();

	        TestData td = new TestData(roses, fish, sugar, number);
	        return td;
	    }
	}
	
}
