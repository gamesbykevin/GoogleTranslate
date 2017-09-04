package com.gamesbykevin.googletranslate.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.eclipsesource.json.Json;

public class Main {
	
	//source file we are reading
	private final String XML_FILE = "C:\\Users\\Kevin\\Desktop\\strings.xml";

	//where do we write the file(s)
	private final String XML_DESTINATION = "C:\\Users\\Kevin\\Desktop\\Translations\\";
	
	//do we only want to translate a single word/sentence
	private final String SENTENCE = "Test sentence";
	
	public final static void main(String[] args) {
		
		try {
			
			//make sure no 2 language(s) have the same language code
			for (int i = 0; i < Language.values().length; i++) {
				for (int j = 0; j < Language.values().length; j++) {
	
					//don't check the same language
					if (i == j)
						continue;
					
					Language a1 = Language.values()[i];
					Language a2 = Language.values()[j];
					
					if (a1.getCode().toLowerCase().equals(a2.getCode().toLowerCase()))
						throw new Exception("Languages have the same code: " + a1.toString() + " , " + a2.toString());
					
				}
			}
			
			
			String begin = "Begin " + new Date();
			Main main = new Main();
			String completed = "Completed " + new Date();
			
			System.out.println(begin);
			System.out.println(completed);
			System.out.println(Language.values().length + " languages translated.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public enum Language {
		Arabic("ar"), Spanish("es"), German("de"), Chinese("zh"), Czech("cs"), 
		Dutch("nl"), Filipino("fil"), French("fr"), Italian("it"), Ukrainian("uk"),
		Japanese("ja"), Korean("ko"), Polish("pl"), Vietnamese("vi"),  
		Russian("ru"), Bulgarian("bg"), Croatian("hr"), Danish("da"), 
		Finnish("fi"), Hebrew("iw"), Hindi("hi"), Greek("el"), Hungarian("hu"), 
		Indonesian("in"), Latvian("lv"), Lithuanian("lt"), Malaysian("ms"), 
		Norwegian("no"), Portuguese("pt"), Romanian("ro"), Serbian("sr"), 
		Slovak("sk"), Slovenian("sl"), Swedish("sv"), Thai("th"), Turkish("tr"), 
		
		//new added
		Burmese("my"), Armenian("hy"), Estonian("et"), Georgian("ka"), Swahili("sw"), 
		Icelandic("is"), Lao("lo"), Mongolian("mn"), Nepali("ne"), Persian("fa"),  
		Kongo("kg"), Kurdish("ku"), Maltese("mt"), Punjabi("pa"), Pali("pi"), 
		Samoan("sm"), Somali("so"), Azerbaijani("az"), Albanian("sq"), Sundanese("su"),
		Tahitian("ty"), Yiddish("yi"), Zulu("zu"), 
		
		;private final String code;
		private Language(final String code) { this.code = code; }
		public String getCode() { return this.code; }
	}
	
	private final String USER_AGENT = "Mozilla/5.0";
	
	private final String xmlHeader = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n<resources>\r\n"; 
	private final String xmlFooter = "</resources>";
	
	public Main() {
		
		//loop through each language
		for (Language language : Language.values()) {
			try {
				
				//translate an xml file
				System.out.println("Translating: " + language.toString() + " - " + language.getCode() + ", " + new Date());
				createXML(language);
				System.out.println("");
				
				/*
				//translate a single word
				System.out.println(language.toString() + " - " + language.getCode());				
				byte[] byteText = getTranslationText(language, SENTENCE).getBytes(Charset.forName("UTF-8"));
				System.out.println("<string name=\"exit_prompt\">" + new String(byteText) + "</string>");
				*/
				
				/*
				//for new apk updates
				System.out.println("<" + language.getCode().toLowerCase() + "-" + language.getCode().toUpperCase() + ">");
				byte[] byteText = getTranslationText(language, SENTENCE).getBytes(Charset.forName("UTF-8"));
				System.out.println(new String(byteText));
				System.out.println("</" + language.getCode().toLowerCase() + "-" + language.getCode().toUpperCase() + ">");
				System.out.println("");
				*/
				
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}
	}
	
	private void createXML(Language language) {
		try {
			
			File fXmlFile = new File(XML_FILE);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
	
			//optional, but recommended
			//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();
	
			//System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
	
			NodeList nList = doc.getElementsByTagName("string");
	
			//create our xml
			String xml = "";
			
			for (int temp = 0; temp < nList.getLength(); temp++) {
	
				Node nNode = nList.item(temp);
	
				//System.out.println("\nCurrent Element :" + nNode.getNodeName());
				
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
	
					Element element = (Element) nNode;
					
					String word = null;
					String attribute = null;
					
					if (element.getAttribute("translatable") != null && element.getAttribute("translatable").length() > 0) {
						if (Boolean.parseBoolean(element.getAttribute("translatable"))) {
							word = element.getTextContent();
							attribute = element.getAttribute("name");
						}
					} else {
						word = element.getTextContent();
						attribute = element.getAttribute("name");
					}
					
					if (word != null && attribute != null) {
						xml += "<string name=\"" + element.getAttribute("name") + "\">" + getTranslationText(language, word) + "</string>\r\n";
						
						System.out.print(word + " [] ");
					}
				}
			}
			
			String fileDest = XML_DESTINATION + "values-" + language.getCode() + "\\strings.xml";
			
			File file = new File(fileDest);
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			
			PrintWriter writer = new PrintWriter(fileDest, "UTF-8");
		    writer.print(xmlHeader + xml + xmlFooter);
		    writer.flush();
		    writer.close();	
		    
	    } catch (Exception e) {
	    	System.out.println("Language: " + language.toString());
	    	e.printStackTrace();
	    }
	}
	
	private String getTranslationText(Language language, String word) throws Exception {
		
		String jsonString = getTranslation(getUrlText(language, word));
		
		String translation = Json.parse(jsonString).asArray().get(0).asArray().get(0).asArray().get(0).toString(); 
		
		//remove first character which is "
		translation = translation.substring(1);
				
		//remove last character which is "
		translation = translation.substring(0, translation.length() - 1);
				
		return translation;
	}
	
	private String getUrlText(Language language, String word) {
		try {
			return "https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=" + language.getCode() + "&dt=t&q=" + URLEncoder.encode(word, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	
	private String getTranslation(final String urlText) {
		
		try {
			URL obj = new URL(urlText);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// optional default is GET
			con.setRequestMethod("GET");

			//add request header
			con.setRequestProperty("User-Agent", USER_AGENT);

			///int responseCode = con.getResponseCode();
			//System.out.println("\nSending 'GET' request to URL : " + urlText);
			//System.out.println("Response Code : " + responseCode);

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
	
		    return response.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
}