
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Iterator;
import java.util.ArrayList;
import java.io.StringReader;
import java.text.BreakIterator;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.SentenceUtils;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException; 


class ParserDemo {

  public static void main(String[] args) {
    String parserModel = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
    if (args.length > 0) {
      parserModel = args[0];
    }
    LexicalizedParser lp = LexicalizedParser.loadModel(parserModel);

    if (args.length == 0) {
    	//read JSON file including fishing email data
    	JSONParser parser = new JSONParser();
    	String filename = System.getProperty("user.dir") + "\\src\\fishing_data.json";
    	System.out.println(filename);
    	try {
    		Object file = parser.parse(new FileReader(filename));
    		JSONObject jsonObject = (JSONObject)file;
    		
    		for(Object key: jsonObject.keySet()) {
    			//key : email title
    			//value : email contents
    			String keyStr = (String)key;
    			String value = (String)jsonObject.get(keyStr);
    			
    			//split string into sentences
    			BreakIterator it = BreakIterator.getSentenceInstance(Locale.US);
    			it.setText(value);
    			int start = it.first();
    			for(int end = it.next(); end != BreakIterator.DONE; start = end, end = it.next()) {
    				String sentence = value.substring(start,end);
    			    System.out.println(sentence);
    				demoAPI(lp,sentence);
    			}
    		}
    	}
    	catch (FileNotFoundException e){
    		e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    } catch (ParseException e) {
	        e.printStackTrace();
	    }
    } else {
      String textFile = (args.length > 1) ? args[1] : args[0];
      demoDP(lp, textFile);
    }
  }


  /*
   example code
   */
  public static void demoDP(LexicalizedParser lp, String filename) {
    // This option shows loading, sentence-segmenting and tokenizing
    // a file using DocumentPreprocessor.
    TreebankLanguagePack tlp = lp.treebankLanguagePack(); // a PennTreebankLanguagePack for English
    GrammaticalStructureFactory gsf = null;
    if (tlp.supportsGrammaticalStructures()) {
      gsf = tlp.grammaticalStructureFactory();
    }
    // You could also create a tokenizer here (as below) and pass it
    // to DocumentPreprocessor
    for (List<HasWord> sentence : new DocumentPreprocessor(filename)) {
      Tree parse = lp.apply(sentence);
      //parse.pennPrint();

      System.out.println();

      if (gsf != null) {
        GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
        Collection tdl = gs.typedDependenciesCCprocessed();
        System.out.println(tdl);
        System.out.println();
      }
    }
  }
  
  public static void demoAPI(LexicalizedParser lp, String sent) {
	   
	TokenizerFactory<CoreLabel> tokenizerFactory =
        PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
    Tokenizer<CoreLabel> tok =
        tokenizerFactory.getTokenizer(new StringReader(sent));
    List<CoreLabel> rawWords = tok.tokenize();
    Tree parse = lp.apply(rawWords);

    TreebankLanguagePack tlp = lp.treebankLanguagePack(); // PennTreebankLanguagePack for English
    GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
    GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
    List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
    //extracting nsubj, nsubjpass, dobj
    for(int i = 0; i < tdl.size(); i++) {
    	String typeDepen = tdl.get(i).reln().toString();
    	
    	//nsubj
    	if(( typeDepen.equals("nsubj")) || ( typeDepen.equals("nsubjpass")) ){
    		System.out.println(typeDepen + ">>");
    		System.out.println(tdl.get(i).gov().originalText());
    	}
    	//obj
    	else if( typeDepen.equals("dobj")) {
    		System.out.println(typeDepen + ">>");
    		System.out.println(tdl.get(i).dep().originalText());
    	}
    }
    System.out.println();
  }

}
