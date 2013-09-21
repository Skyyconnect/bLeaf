package com.elle.BLeaf;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xmlpull.v1.XmlPullParser;

//import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
//import com.hp.hpl.jena.rdf.model.Model;
//import com.hp.hpl.jena.rdf.model.ModelFactory;
//import com.hp.hpl.jena.rdf.model.Property;
//import com.hp.hpl.jena.rdf.model.Resource;

import android.app.Activity;
import android.os.Bundle;
import android.util.Xml;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;


public class BLeafActivity extends Activity {
	/** Called when the activity is first created. */
	static final String PUB_DATE = "pubDate";
	static final String DESCRIPTION = "description";
	static final String LINK = "link";
	static final String TITLE = "title";
	static final String ITEM = "item";
	static final String CHANNEL = "channel";
	
	ArrayList<Message> messages = null;
	ArrayList<Item> items = null;
	public ArrayList<String> history;
	public ListView mList;
	public ArrayList<String> myCauses;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);   
			
//		Model m = ModelFactory.createDefaultModel();
//		String NS = "http://elleconnect.com/andrewdo/BLeaf/";
//		Resource r = m.createResource(NS + "r");
//		Property p = m.createProperty(NS + "p");
//		r.addProperty(p,"hello world", XSDDatatype.XSDstring);
//		// m.write(System.out,"Turtle");
		myCauses = new ArrayList<String>();
		myCauses.add("123");
		myCauses.add("789");
		
		final TextView text = (TextView) findViewById(R.id.textView4);
		final Button button2 = (Button) findViewById(R.id.button2);
		button2.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// look at the data structures for the matching
				String manufacturer = "12345";
				ArrayList<Assertion> scanResults = Scan(manufacturer);
				Iterator<Assertion> itr = scanResults.iterator();
				while (itr.hasNext()) {
					Assertion assertion = itr.next();
					Display("Boycott this manufacturer (" + manufacturer + "):\n" + 
							assertion.toString() + "\n" + 
							assertion.getContainingItem().printReasons() + "\n");
				}
			}
		});
		
		final Button button = (Button) findViewById(R.id.button1);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// pretend that the url of the RSS feed was filled out in the GUI
				// pretend that the rss feed was downloaded, parsed, and BLeaf tag extracted and url got
				String link = "http://elleconnect.com/andrewdo/bleaf/sample.rss";
				try {
					URL url = new URL(link);                	    
					try {
						InputStream stream = url.openConnection().getInputStream();
						try {
							// FIXME: need to do validation of the XML against the DTD of RSS
							XmlPullParser parser = Xml.newPullParser();
							try {
								// auto-detect the encoding from the stream
								parser.setInput(stream, null);
								int eventType = parser.getEventType();
								Message currentMessage = null;
								boolean done = false;
								while (eventType != XmlPullParser.END_DOCUMENT && !done){
									String name = null;
									switch (eventType){
									case XmlPullParser.START_DOCUMENT:
										messages = new ArrayList<Message>();
										break;
									case XmlPullParser.START_TAG:
										name = parser.getName();
										if (name.equalsIgnoreCase(ITEM)){
											currentMessage = new Message();
										} else if (currentMessage != null){
											if (name.equalsIgnoreCase(LINK)){
												currentMessage.setLink(parser.nextText());
											} else if (name.equalsIgnoreCase(DESCRIPTION)){
												currentMessage.setDescription(parser.nextText());
											} else if (name.equalsIgnoreCase(PUB_DATE)){
												currentMessage.setDate(parser.nextText());
											} else if (name.equalsIgnoreCase(TITLE)){
												currentMessage.setTitle(parser.nextText());
											}    
										}
										break;
									case XmlPullParser.END_TAG:
										name = parser.getName();
										if (name.equalsIgnoreCase(ITEM) && 
												currentMessage != null){
											messages.add(currentMessage);
										} else if (name.equalsIgnoreCase(CHANNEL)){
											done = true;
										}
										break;
									}
									eventType = parser.next();
								}
							} catch (Exception e) {
								throw new RuntimeException(e);
							}
						} catch (Exception e) {
							throw new RuntimeException(e);
						} 
					} catch (IOException e) {
						// throw new RuntimeException(e);
						text.setText(text.getText()+"\nCould not access file " + url.toString() + "\n");
					}
				} catch (MalformedURLException e) {
					throw new RuntimeException(e);
				}
				if (messages != null) {
					Iterator<Message> itr = messages.iterator();
					while (itr.hasNext()) {
						Message mesg = itr.next();
						// text.setText(text.getText()+"\n"+mesg.getDescription());
						// find a match of the URL information for the XML File
						Pattern p = Pattern.compile("StartBLeaf (.+?) EndBLeaf",Pattern.DOTALL);
						Matcher m = p.matcher(mesg.getDescription());						
						boolean matchFound = m.find();
						if (matchFound) {
						    // Get all groups for this match
						    for (int i=0; i<=m.groupCount(); i++) {
						        if (i == 1) {
						        	String link2 = m.group(i);						        
						        	text.setText(text.getText()+"\nExtracted Announcement Link "+link2);
						        	try {
										URL url2 = new URL(link2);
										ProcessAnnouncement(url2);
						    		} catch (MalformedURLException e) {
										throw new RuntimeException(e);
									}
						        }
						    }
						}
					}
				}
			}
		});
	}
	
	private void Display(String string) {
		final TextView text = (TextView) findViewById(R.id.textView4);
		text.setText(text.getText() + string);
	}

	public void ProcessAnnouncement(URL url) {
		// need to create an object representing the Boycott
		final TextView text = (TextView) findViewById(R.id.textView4);
		try {
			InputStream stream = url.openConnection().getInputStream();
			try {
				// FIXME: need to do validation of the XML against the DTD
				XmlPullParser parser = Xml.newPullParser();
				try {
					// auto-detect the encoding from the stream
					parser.setInput(stream, null);
					int eventType = parser.getEventType();
					Item currentItem = null;
					boolean done = false;
					boolean inside = false;
					boolean insidereasons = false;
					String source = null;
					String reasons_source = null;
					String reasons_content = null;
					Assertion assertion = null;
					while (eventType != XmlPullParser.END_DOCUMENT && !done){
						String name = null;
						switch (eventType){
						case XmlPullParser.START_DOCUMENT:
							items = new ArrayList<Item>();
							break;
						case XmlPullParser.START_TAG:
							name = parser.getName();
							if (name.equalsIgnoreCase("source") && ! inside) {
								source = parser.nextText();
							} else if (name.equalsIgnoreCase("item")){
								inside = true;
								currentItem = new Item();
							} else if (currentItem != null){
								if (name.equalsIgnoreCase("title")){
									currentItem.setTitle(parser.nextText());
									currentItem.setSource(source);
									text.setText(text.getText()+"\n"+currentItem.getTitle()+"\n");
								} else if (name.equalsIgnoreCase("category")){
									currentItem.setCategory(parser.nextText());
								} else if (name.equalsIgnoreCase("assert")){
									assertion = new Assertion(currentItem,com.elle.BLeaf.Assertion.Action.ASSERT);							
								} else if (name.equalsIgnoreCase("retract")){
									assertion = new Assertion(currentItem,com.elle.BLeaf.Assertion.Action.RETRACT);
								} else if (name.equalsIgnoreCase("cause")){
									assertion.setCause(parser.nextText());									
								} else if (name.equalsIgnoreCase("manufacturer")){
									assertion.addManufacturer(parser.nextText());
								} else if (name.equalsIgnoreCase("reasons")){									
									// currentItem.setTitle(parser.nextText());
									insidereasons = true;
								} else if (name.equalsIgnoreCase("source") && insidereasons == true) {
									reasons_source = parser.nextText();
								} else if (name.equalsIgnoreCase("content") && insidereasons == true) {
									reasons_content = parser.nextText();
								}
							}
							break;
						case XmlPullParser.END_TAG:
							name = parser.getName();
							if (name.equalsIgnoreCase("item")) {
								inside = false;
								if (currentItem != null){
									items.add(currentItem);
								}
							} else if (name.equalsIgnoreCase("announcements")){
								done = true;
							} else if (currentItem != null) {
								if (name.equalsIgnoreCase("reasons")) {
									insidereasons = false;
									currentItem.addReason(new Reason(reasons_source,reasons_content));
								} else if (name.equalsIgnoreCase("assert") || name.equalsIgnoreCase("retract"))	{
									currentItem.addLogic(assertion);
								}
							}
							break;
						}
						eventType = parser.next();
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			} 
		} catch (IOException e) {
			// throw new RuntimeException(e);
			text.setText(text.getText()+"\nCould not access file " + url.toString() + "\n");
		}
		if (items != null) {
			Iterator<Item> itr = items.iterator();
			while (itr.hasNext()) {
				Item item = itr.next();
				Display("Added item\n" + item.printLogic() + "\n");
			}
		}
	}
	
	ArrayList<Assertion> Scan (String manufacturer) {
		// look at every item
		ArrayList<Assertion> results = new ArrayList<Assertion>();
		if (items != null) {
			// iterate over it
			Iterator<Item> itr = items.iterator();
			while (itr.hasNext()) {
				Item item = itr.next();
				Iterator<Assertion> itr2 = item.getLogic().iterator();
				while (itr2.hasNext()) {
					Assertion assertion = itr2.next();
					if (myCauses.contains(assertion.getCause())) {
						if (assertion.getManufacturers().contains(manufacturer)) {
							// add the assertion to the results
							results.add(assertion);
						}
					}
				}
				
			}
		} else {
			Display("First populate your items\n");
		}
		return results;
	}
}