package com.ipeirotis.util;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class MailUtil {
	private static final Logger logger = Logger.getLogger(MailUtil.class.getName());
	
	public static void send(String htmlBody, String subject, 
			String fromAddress, String fromText, List<String> recepients, String replyTo) {
		try {
			Properties props = new Properties();
			Session session = Session.getDefaultInstance(props, null);

			Multipart mp = new MimeMultipart();
			MimeBodyPart htmlPart = new MimeBodyPart();
			htmlPart.setContent(htmlBody, "text/html");
			htmlPart.setHeader("Content-type", "text/html; charset=UTF-8");
			mp.addBodyPart(htmlPart);

			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(fromAddress, fromText));
			for(String recepient : recepients){
				msg.addRecipient(Message.RecipientType.TO, new InternetAddress(
						recepient, recepient));
			}

			if(replyTo != null){
				msg.setReplyTo(new Address[]{new InternetAddress(replyTo)});
			}
			msg.setSubject(subject);
			msg.setContent(mp);
			Transport.send(msg);
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	public static void send(String htmlBody, String subject, 
			String fromAddress, String fromText, String recepient) {
		send(htmlBody, subject, fromAddress, fromText, Arrays.asList(recepient), null);
	}

	public static void send(String htmlBody, String subject, 
			String fromAddress, String fromText, String recepient, String replyTo) {
		send(htmlBody, subject, fromAddress, fromText, Arrays.asList(recepient), replyTo);
	}

}
