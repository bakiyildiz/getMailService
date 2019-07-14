/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package getmailservice;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Properties;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Flags;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Baki
 */
public class GetMailService {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        
        try {
            if((getConfigData("appSettings", "autoRun")).equals("0")){
           System.exit(0);
        }
        } catch (Exception e) {
        }
                 
        String host = getConfigData("mailservice","host");
        final String user = getConfigData("mailservice","mail");
        final String password = getConfigData("mailservice","password");
                
        getMailInbox(user, password, host);
        
        shutdownMe();
    }
    
    public static void shutdownMe(){
        try {
            Process pro = Runtime.getRuntime().exec("java -jar mailServiceStarter.jar");
        } catch (IOException ex) {
            try {
                Process pro = Runtime.getRuntime().exec("java -jar xmlCreator.jar exception getMailService EX_1 " + ex);
            } catch (Exception exe) {          
            }
        }
        
        System.exit(0);
    }
    
    public static String getConfigData(String tagName, String configName){
        String strXmlDate = null;
        
        try {            
            
            try {
                File inputFile = new File("../config/config.xml");
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(inputFile);
                doc.getDocumentElement().normalize();
                NodeList nList = doc.getElementsByTagName(tagName);

                for (int temp = 0; temp < nList.getLength(); temp++) {
                    Node nNode = nList.item(temp);
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                       Element eElement = (Element) nNode;
                       strXmlDate = eElement.getElementsByTagName(configName).item(0).getTextContent();
                    }
                }

            } catch (Exception e) {
                try {
                    Process pro = Runtime.getRuntime().exec("java -jar xmlCreator.jar exception getMailService EX_2 " + e);
                } catch (Exception ex) {          
                }
            }
            
        } catch (Exception e) {
            try {
                Process pro = Runtime.getRuntime().exec("java -jar xmlCreator.jar exception getMailService EX_3 " + e);
            } catch (Exception ex) {          
            }
        }               

        return strXmlDate;
    }
    
    static Store emailStore;
    static Folder emailFolder;
    
    public static void getMailServerConnection(String userName, String password, String host) throws MessagingException{
        Properties properties = new Properties();
        properties.put("mail.pop3.host", host);
        properties.put("mail.pop3.port", "995");
        properties.put("mail.pop3.starttls.enable", "true");
        properties.put("mail.smtp.debug", "true");
        Session emailSession = Session.getDefaultInstance(properties);
        try {
            emailStore = emailSession.getStore("imaps");
        } catch (NoSuchProviderException ex) {
        }
        emailStore.connect(host,userName,password);
        emailFolder = emailStore.getFolder("INBOX");
    }
    
    public static void getMailInbox(String userName, String password, String host){
        try {
            
            if(emailStore == null || !emailStore.isConnected()){
                getMailServerConnection(userName,password,host);
            }
            
            emailFolder.open(Folder.READ_WRITE);
            Message messages[] = emailFolder.getMessages();
            Message message;
            
            if(messages.length > 0){
                for (int i = 0; i < messages.length; i++) {
                    message = messages[i];
                    
                    String strFrom = message.getFrom()[0].toString();
                    strFrom = strFrom.replace(">", "");
                    String[] parts = strFrom.split("<");
                    
                    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss_dd.MM.yyyy");
                    String format = formatter.format(message.getSentDate());
                    
                    System.out.println(parts[parts.length-1].toString());
                    System.out.println(format);
                    System.out.println(message.getSubject().toString());
                    
                    runXmlCreateService(parts[parts.length-1].toString(), message.getSubject().toString(),format);
                }               
            }
            
            if(messages.length > 0){
                deleteAllMail(userName, password, host);
            }else{
                if(emailFolder.isOpen())
                    emailFolder.close(false);
                
                if(emailStore.isConnected())
                    emailStore.close();
            }
            
        } catch(NoSuchProviderException nspe){
            System.out.println("asd1");
            try {
                Process pro = Runtime.getRuntime().exec("java -jar xmlCreator.jar exception getMailService EX_4 " + nspe);
            } catch (Exception ex) {          
            }
        } catch(MessagingException me){
            System.out.println("asd2");
            try {
                Process pro = Runtime.getRuntime().exec("java -jar xmlCreator.jar exception getMailService EX_5 " + me);
            } catch (Exception ex) {          
            }
        } catch (Exception e) {
            System.out.println("asd3");
            try {
                Process pro = Runtime.getRuntime().exec("java -jar xmlCreator.jar exception getMailService EX_6 " + e);
            } catch (Exception ex) {          
            }
        }
    }
    
    public static void deleteAllMail(String userName, String password, String host){        
        try {
            if(emailStore == null || !emailStore.isConnected()){
                getMailServerConnection(userName,password,host);
            }
                
            Message[] messages = emailFolder.getMessages();
            for (int i = 0; i < messages.length; i++) {
               Message message = messages[i];
               message.setFlag(Flags.Flag.DELETED, true);
            }
            
            if(emailFolder.isOpen())
                emailFolder.close(true);
                        
            if(emailStore.isConnected())
                emailStore.close();            
         
        } catch (NoSuchProviderException e) {
            try {
                Process pro = Runtime.getRuntime().exec("java -jar xmlCreator.jar exception getMailService EX_7 " + e);
            } catch (Exception ex) {          
            }
            e.printStackTrace();
        } catch (MessagingException e) {
            try {
                Process pro = Runtime.getRuntime().exec("java -jar xmlCreator.jar exception getMailService EX_8 " + e);
            } catch (Exception ex) {          
            }
             e.printStackTrace();
        } catch(Exception e){
            try {
                Process pro = Runtime.getRuntime().exec("java -jar xmlCreator.jar exception getMailService EX_9 " + e);
            } catch (Exception ex) {          
            }
      }
    }
    
    public static void runXmlCreateService(String from, String talep, String talepDate){
        try {
            Process pro = Runtime.getRuntime().exec("java -jar xmlCreator.jar talep " + talepDate + " " + from + " " + talep);
        } catch (IOException ex) {
            try {
                Process pro = Runtime.getRuntime().exec("java -jar xmlCreator.jar exception getMailService EX_10 " + ex);
            } catch (Exception exe) {          
            }        
        }
    }    
}