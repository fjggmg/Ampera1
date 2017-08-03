package com.lifeform.main.data;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.List;
import java.util.Map;

/**
 * Created by Bryan on 7/17/2017.
 */
public class JSONManager {



    public static Map<String,String> parseJSONtoMap(String json) {
        JSONParser parser = new JSONParser();
        try {
            //System.out.println("Parsing: " + json);
            JSONObject jsonMap = (JSONObject) parser.parse(json);
            //may need to add some debug output here to ensure the object creation is going swimmingly
            return jsonMap;
        } catch (ParseException e) {

        }
        return null;
    }


    public static JSONObject parseMapToJSON(Map<String, String> map) {
        JSONObject obj = new JSONObject();
        for(String key:map.keySet())
        {
            obj.put(key,map.get(key));
        }
        return obj;
    }

    public static JSONArray parseListToJSON(List<String> array)
    {
        JSONArray jArray = new JSONArray();
        for(String s:array)
        {
            jArray.add(s);
        }

        return jArray;
    }
    public static List<String> parseJSONToList(String json)
    {
        try {
            JSONArray jsonArray = (JSONArray) (new JSONParser()).parse(json);

            return jsonArray;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

}
