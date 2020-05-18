package com.example.demo.api;
import com.example.demo.ranker.Trends;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.example.demo.Stemmer.Stemmer;
import com.example.demo.ranker.Ranker;
import com.example.demo.ranker.Ranker.DocumentResult;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.ArrayList;

@RestController
@RequestMapping("/search")
public class APIs {

    Ranker ranker =null;
    Trends trends=null;
    public APIs()
    {
        ranker =new Ranker();
        trends=new Trends();
    }
    ArrayList<String> listOfWords;
    ArrayList<String> names;
    @GetMapping(value="document")
    public String getDocument( @RequestParam(value = "search_query",defaultValue = "google") String search_query,
                                 @RequestParam(value = "region", required = true, defaultValue = "Egypt") String region) throws SQLException, JSONException {

        names=trends.getNames(search_query);
        ranker.addSearchQuery(search_query);
        if(names.size()>0)
        {
            for(String name : names)
            {
                trends.addFamousNames(name,region);
            }

        }
        listOfWords = queryPreProcessor(search_query);
        ArrayList<DocumentResult> results=ranker.makeRank(listOfWords,false);
        for(DocumentResult result : results)
        {
            System.out.println(result.hyper_link);
            System.out.println(result.brief);
            System.out.println(result.title);
        }
        JSONArray jsonArray= new JSONArray();
        for(int i=0;i<results.size();i++)
        {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("TITLE",results.get(i).title);
            jsonObject.put("URL", results.get(i).hyper_link);
            jsonObject.put("BRIEF",results.get(i).brief);
            jsonArray.put(jsonObject) ;
        }
        return  jsonArray.toString();
    }

    private ArrayList<String> queryPreProcessor(String search_query) {
        ArrayList<String> result=new ArrayList<>();
        Stemmer S = new Stemmer();
        String queryList[] = search_query.split("\"");
        int itr=0;
        for(int i=0;i<queryList.length;i++)
        {
            if(queryList[i].length()!=0&&queryList[i]!=" ") {
                if (i % 2 == 0) {
                    //String stemmed = S.stem(search_query);
                    String splitWords[] = search_query.split(" ");
                    for (int j = 0; j < splitWords.length; j++) {
                        result.add(splitWords[j]);
                    }
                } else {
                    result.add(queryList[i]);
                }
            }
        }

    return result;
    }

    @GetMapping(value="image")
    public String getImage( @RequestParam(value = "search_query") String search_query,
                                 @RequestParam(value = "region", required = true, defaultValue = "honey") String region) throws SQLException, JSONException {
        listOfWords = queryPreProcessor(search_query);
        ArrayList<DocumentResult> results=ranker.makeRank(listOfWords,true);
        for(DocumentResult result : results)
        {
            System.out.println(result.hyper_link);
            System.out.println(result.brief);
            System.out.println(result.title);
        }
        JSONArray jsonArray= new JSONArray();
        for(int i=0;i<results.size();i++)
        {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("IMAGE_CAPTION",results.get(i).title);
            jsonObject.put("IMAGE_URL", results.get(i).hyper_link);
            jsonArray.put(jsonObject) ;
        }
        return  jsonArray.toString();
    }


    @GetMapping(value="trend")
    public String getTrend( @RequestParam(value = "region") String region) throws SQLException, JSONException {
        ArrayList<Trends.TrendsData> results=trends.famousPersons(region);
        JSONArray jsonArray= new JSONArray();
        for(int i=0;i<results.size();i++)
        {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("PERSON_NAME",results.get(i).personName);
            jsonObject.put("COUNT", results.get(i).count);
            jsonArray.put(jsonObject) ;
        }
        return  jsonArray.toString();
    }

    @GetMapping(value="suggest")
    public String getSuggestion( @RequestParam(value = "search_query") String search_query) throws SQLException, JSONException {

        ArrayList<String> suggestions=ranker.getSuggestions(search_query);
        JSONArray jsonArray= new JSONArray();
        for(int i=0;i<suggestions.size();i++)
        {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("SUGGESTION", suggestions.get(i));
            jsonArray.put(jsonObject) ;
        }
        return jsonArray.toString();
    }
}
