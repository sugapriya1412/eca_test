package org.vtop.CourseRegistration.Common.service;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;

import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;

@Service
public class MailUtility
{
	public static String triggerMail(String subject, String body, String attachementFilePath, String to)
	{
		String FromEmailID = "XXXX";
		String FromEmailPass = "XXXX";			
 
		Properties props = new Properties();		 
		//props.put("mail.smtp.host", "10.10.4.55");
		props.put("mail.smtp.host", "XXXX");
		props.put("mail.smtp.port", "XXXX"); 		  
		props.put("mail.smtp.auth", "XXXX");		 
		props.put("mail.smtp.ssl.trust", "XXXX");
		
		
		Session session = Session.getInstance(props,
		  new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(FromEmailID, FromEmailPass);
			}
		});
		 
		 
		try
		{		
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(FromEmailID));
			message.setRecipients(Message.RecipientType.TO,InternetAddress.parse(to));
			message.setSubject(subject);
			message.setContent(body ,"text/html");		
 
			Transport.send(message);
			//System.out.println("<<<<<< Email Sent Successfully >>>>>>>");
			
			return "SUCCESS";
		 }
		 catch(Exception e)
		 {
			 //e.printStackTrace();
			 //System.out.println("<<<<<<<<< Error Sending Email >>>>>>>>>>");
			 return e.toString();
		 }
	}
}
