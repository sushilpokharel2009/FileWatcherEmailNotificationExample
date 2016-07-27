package filewatcheremailnotificationexample;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Email {
	
	static Properties props = new Properties();
	
	static{
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.socketFactory.port", "587");
		props.put("mail.smtp.socketFactory.class",
				"javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "587");
	}
	
	public static void send(String fileChangeInfo) {
		
		
		Session session = Session.getDefaultInstance(props,
				new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication("some_account@gmail.com","password");
			}
		});

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress("some_account@gmail.com"));
			message.setRecipients(Message.RecipientType.CC,
					InternetAddress.parse(ContactList.getList()));
			message.setSubject("Intranet information");
			message.setText("Dear Sir/Madam," +
					"\n\n this an automatically generated e-mail to inform You about the following:" + 
					"\n\n " + fileChangeInfo + 
					"\n\n Regards");

			Transport.send(message);

			System.out.println("Done");

		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}
}
