package com.google.code.shim.email;

import java.util.Properties;

import javax.mail.AuthenticationFailedException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Manager class that uses the JavaMail APIs to send email.  Useful for constructing simple email messages to be sent on a singleton basis.
 * 
 * 
 * @author dgau
 *
 */
public class MailManager implements Runnable{

	static Logger logger = LogManager.getLogger(MailManager.class);

	Properties props = new Properties();
	private InternetAddress[] recipients;
	private String[] subjects;
	private String[] contents;
	private Authenticator authenticator;
	/**
	 * Configures the mailing properties.
	 * 
	 * Expected properties are:
	 * <ul>
	 * <li>mail.transport.protocol= _protocol_ ('smtp'/'smtps')</li>
	 *	<li>mail._protocol_.from= from address</li>
	 *	<li>mail._protocol_.host= mailer host (smtp.gmail.com, etc)</li>
	 *  <li>mail._protocol_.port= mailer host (smtp.gmail.com, etc)</li>
	 *	<li>mail._protocol_.auth= boolean indicating whether authentication is required</li>
	 *	<li>mail._protocol_.user= if auth is required, the username</li>
	 *	<li>mail._protocol_.password= if auth is required, the password</li>
	 * </ul>
	 * @param toSet
	 */
	public void setProperties(Properties toSet) {
		props = toSet;
	}
	
	/**
	 * Helper method to allow an individual mailer property to be set.
	 * @param propName
	 * @param value
	 */
	public void setProperty(String propName, String value){
		if(props==null) props = new Properties();
		props.put(propName, value);
	}
	
 
	/**
	 * Sets the recipients for the message.
	 * 
	 * @param emails
	 * @throws AddressException
	 */
	public void setRecipients(String... emails) throws AddressException {
		int i = 0;
		recipients = new InternetAddress[emails.length];
		for (String email : emails) {
			recipients[i++] = new InternetAddress(email);
		}
	}

	/**
	 * Sets subject content for the different recipients (if it varies)
	 * A subject is required.  If you specify one subject, it will be used
	 * for all recipients.  If you specify multiple subjects, make sure that
	 * you specify one per recipient, otherwise the mailer will just use the 
	 * first subject.
	 * @param subjectsToUse
	 */
	public void setSubject(String... subjectsToUse) {
		int i = 0;
		subjects = new String[subjectsToUse.length];
		for (String sub : subjectsToUse) {
			subjects[i++] = sub;
		}
	}

	/**
	 * Sets text content for the different recipients (if it varies)
	 * A content entry is required.  If you specify one entry, it will be used
	 * for all recipients.  If you specify multiple entries, make sure that
	 * you specify one per recipient, otherwise the mailer will just use the 
	 * first content entry.
	 * @param text
	 */
	public void setTextContent(String... text) {
		int i = 0;
		contents = new String[text.length];
		for (String txt : text) {
			contents[i++] = txt;
		}

	}

	public void setAuthenticator(Authenticator authenticator) {
		this.authenticator = authenticator;
	}

	/**
	 * Triggers the sending of the messages.
	 */
	public void sendMessage() {
		if (recipients == null || recipients.length == 0) {
			logger.error("No recipient(s) provided.  Message(s) will not be sent.");
			return;
		}
		if (subjects == null || subjects.length == 0) {
			logger.error("No subject(s) provided.  Message(s) will not be sent.");
			return;
		}
		if (contents == null || contents.length == 0) {
			logger.error("No contents provided.  Message(s) will not be sent.");
			return;
		}

		// Get session
		Session session = null;
		Transport transport = null;
		try {

			session = Session.getDefaultInstance(props, this.authenticator);
			if (logger.isDebugEnabled()) {
				session.setDebug(true);
			}

			
			// Send message
		 
			transport = session.getTransport();
			//transport.connect(props.getProperty("mail.smtps.host"), props.getProperty("mail.smtps.username"),
			//		props.getProperty("mail.smtps.password"));
			transport.connect();
			
			for (int i = 0; i < recipients.length; i++) {
			// Define each message
				MimeMessage message = new MimeMessage(session);
				message.setFrom();
				message.addRecipient(Message.RecipientType.TO, recipients[i]);

				// One subject or many?
				if (subjects.length > 1 && subjects.length >= recipients.length) {
					message.setSubject(subjects[i]);
				} else {
					// Just use the first subject
					message.setSubject(subjects[0]);
				}

				// One body or many?
				if (contents.length > 1 && contents.length >= recipients.length) {
					message.setText(contents[i]);
				} else {
					// Just use the first content
					message.setText(contents[0]);
				}

				
				InternetAddress[] replyTo = { new InternetAddress("noreply@domain.com") };
				message.setReplyTo(replyTo);

				// Finally, send the message.
				transport.sendMessage(message, message.getAllRecipients());
			}
			transport.close();

		} catch (AddressException e) {
			logger.error("Address error: " + e.getMessage(), e);
		} catch (AuthenticationFailedException e) {
			logger.error("Authorization error: " + e.getMessage(), e);
		} catch (MessagingException e) {
			logger.error("General mail messaging error: " + e.getMessage(), e);
		}
	}

	/**
	 * Command-line invocation of the mail manager
	 * @param args
	 * <ul>
	 * 	<li>-username smtp username</li>
	 *  <li>-password smtp password</li>
	 *  <li>-host smtp host</li>
	 *  <li>-protocol (optional. default=smtps) smtp protocol</li>
	 *  <li>-from from address</li>
	 *  <li>-authRequired (optional. default=true) require authentication.</li>
	 *  <li>-recipient comma-separated email addresses</li>
	 *  <li>-subject subject line (enclosed in quotes)</li>
	 *  <li>-content email content (enclosed in quotes)</li>
	 * </ul>
	 */
	public static final void main(String[] args) {
		try {
			String username=null;
			String password=null;
			String protocol="smtps";
			String host=null;
			String from="noreply@domain.com";
			Boolean auth = true;
			String recipient = null;
			String subject=null;
			String content=null;
			for(String arg: args){
				if(arg.startsWith("-username")){
					username = arg.substring(arg.indexOf('=')+1);
				} else if(arg.startsWith("-password")){
					password = arg.substring(arg.indexOf('=')+1);
				} else if(arg.startsWith("-protocol")){
					protocol = arg.substring(arg.indexOf('=')+1);
				} else if(arg.startsWith("-host")){
					host = arg.substring(arg.indexOf('=')+1);
				} else if(arg.startsWith("-from")){
					from = arg.substring(arg.indexOf('=')+1);
				} else if(arg.startsWith("-authRequired")){
					auth = Boolean.parseBoolean(  arg.substring(arg.indexOf('=')+1) );
				} else if(arg.startsWith("-recipient")){
					recipient = arg.substring(arg.indexOf('=')+1);
				} else if(arg.startsWith("-subject")){
					subject = arg.substring(arg.indexOf('=')+1);
				} else if(arg.startsWith("-content")){
					content = arg.substring(arg.indexOf('=')+1);
				} 
			}
			
			MailManager mailer = new MailManager();
			Properties props = new Properties();
			props.put("mail.transport.protocol", protocol);
			props.put("mail.smtps.from", from);
			props.put("mail.smtps.host", host);//smtp.gmail.com
			props.put("mail.smtps.auth", auth);
			mailer.setProperties(props);

			if(auth){
				Authenticator authenticator = new Authenticator(username,password);
				mailer.setAuthenticator(authenticator);
			}
			
			mailer.setRecipients(recipient);
			mailer.setSubject(subject);
			mailer.setTextContent(content);

			mailer.sendMessage();
		} catch (AddressException e) {
			logger.error(e.getMessage(), e);
		}
	}

	
	/**
	 * Calls {@link #sendMessage()}
	 */
	@Override
	public void run() {
		sendMessage();
	}
	
	
	private static class Authenticator extends javax.mail.Authenticator {
		private PasswordAuthentication authentication;

		public Authenticator(String u, String p) {
			String username = u;
			String password = p;
			authentication = new PasswordAuthentication(username, password);
		}

		protected PasswordAuthentication getPasswordAuthentication() {
			return authentication;
		}
	}
}
