package kzr.frontale;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import java.util.NoSuchElementException;

import java.net.URL;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.io.InputStreamReader;

import javax.swing.text.html.HTML;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.parser.ParserDelegator;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.TagElement;

import java.util.ArrayList;
import java.util.HashMap;

import javax.enterprise.context.ApplicationScoped;
  
@ApplicationScoped
public class Frontale {
  // list of each player's URL
  ArrayList<String> playerURL = new ArrayList<>();

  // map between player's number and Plyaer Object
  HashMap<String, Player> playerMap = new HashMap<>();

  public HashMap<String, Player> getMap() {
    return playerMap;
  }

  /**
     Set proxy information , if needed
     Read from configurations
  */
  private void setProxy(Config config) {
    boolean proxySet = false;
    try {
      proxySet = config.getValue("frontale.proxySet", Boolean.class);
    } catch (NoSuchElementException e) {
      // fall through
    }

    if (proxySet) {
      try {
	String proxyHost = config.getValue("frontale.proxyHost", String.class);
	String proxyPort = config.getValue("frontale.proxyPort", String.class);
	String proxyUser = config.getValue("frontale.proxyUser", String.class);
	String proxyPass = config.getValue("frontale.proxyPass", String.class);
    
	System.setProperty("proxySet", "true");
	System.setProperty("proxyHost", proxyHost);
	System.setProperty("proxyPort", proxyPort);
	if (proxyUser != null && proxyPass != null) {
	  Authenticator.setDefault(new Authenticator() {
	      protected PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(proxyUser, proxyPass.toCharArray());
	      }});
	}
      } catch (NoSuchElementException e) {
	System.err.println("no proxy information");
      }
    }
  }
  
  Frontale() {
    //MicorProfile Config API
    Config config = ConfigProvider.getConfig();

    setProxy(config);

    String url = null;
    try {
      url = config.getValue("frontale.url", String.class);
    } catch (NoSuchElementException e) {
      System.err.println("no url information");
      System.exit(1);
    }

    try {
      ParserDelegator dg = new ParserDelegator();
      dg.parse(new InputStreamReader(new URL(url).openStream()), new Parser(), true);

      for (String p : playerURL)
	parsePlayer("http://www.frontale.co.jp" + p);
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  void parsePlayer(String url) throws Exception {
    ParserDelegator dg = new ParserDelegator();
    Parser2 parser = new Parser2();
    dg.parse(new InputStreamReader(new URL(url).openStream(), "utf-8"), parser, true);
    Player p = parser.getPlayer();
    playerMap.put(p.number, p);
  }

  /**
     read html file by using HTMLEditorKit
     this code is highly dependent on specific html file
  */
  class Parser2 extends HTMLEditorKit.ParserCallback {
    boolean parsingPosition = false;
    boolean parsingNumber = false;
    boolean parsingName = false;
    boolean parsingSns = false;
    Player player = new Player();

    public void handleText(char[] data, int pos) {
      if (parsingPosition) {
	player.position = new String(data);
	parsingPosition = false;
	parsingNumber = true;
      }
      else if (parsingNumber) {
	player.number = new String(data);
	parsingNumber = false;
	parsingName = true;
      }
      else if (parsingName) {
	player.name = new String(data);
	parsingName = false;
      }
      else if (parsingSns) {
	player.sns = new String(data);
	parsingSns = false;
      }
    }
    
    public void handleStartTag(HTML.Tag tag, MutableAttributeSet att, int pos) {
      if (tag == HTML.Tag.P) {
	Object value  = att.getAttribute(HTML.Attribute.CLASS);
	if (value == null)
	  return;
	if (value.equals("typo_position rbt_r")) {
	  parsingPosition = true;
	}
      }
      else if (tag == HTML.Tag.STRONG) {
	Object value  = att.getAttribute(HTML.Attribute.CLASS);
	if (value == null)
	  return;
	if (value.equals("rbt_r")) {
	  parsingSns = true;
	}
      }
    }

    Player getPlayer() {
      return player;
    }
    
  }
  
  /**
     read html file by using HTMLEditorKit
     this code is highly dependent on specific html file
  */
  class Parser extends HTMLEditorKit.ParserCallback {
    public void handleStartTag(HTML.Tag tag, MutableAttributeSet att, int pos) {
      if (tag == HTML.Tag.A) {
	Object value  = att.getAttribute(HTML.Attribute.HREF);
	if (value == null)
	  return;
	String href  = (String)value;
	if (href.startsWith("/profile/2017/mem")) {
	  playerURL.add(href);
	}
      }
    }
  }
}
