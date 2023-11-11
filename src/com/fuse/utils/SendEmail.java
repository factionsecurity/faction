package com.fuse.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import javax.persistence.EntityManager;

import com.fuse.dao.Assessment;
import com.fuse.dao.HibHelper;
import com.fuse.dao.PeerReview;
import com.fuse.dao.SystemSettings;
import com.fuse.dao.User;
import com.fuse.dao.Verification;

public class SendEmail {
	private SystemSettings emailSettings = null;
	private EntityManager em = null;

	public SendEmail(EntityManager em) {
		this.em = em;
		this.getDefaultSettings();
	}


	private void getDefaultSettings() {
		SystemSettings settings = (SystemSettings) em.createQuery("from SystemSettings").getResultList().stream()
				.findFirst().orElse(null);
		if (settings != null && !settings.getServer().equals("")) {
			this.emailSettings = settings;
		} else if(settings != null && settings.getServer().equals("")) {
			settings.initSMTPSettings();
			this.emailSettings = settings;
		}else {
			settings = new SystemSettings();
			settings.initSMTPSettings();
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			em.persist(settings);
			this.emailSettings = settings;
			HibHelper.getInstance().commit();
		}
	}

	public void send(Object obj, String Msg, String Subject) {
		List<String> sends = null;
		String Class = obj.getClass().toString();
		if (Class.contains("Assessment"))
			sends = createList((Assessment) obj);
		else if (Class.contains("Verification"))
			sends = createList((Verification) obj);
		else if (Class.contains("PeerReview"))
			sends = createList((PeerReview) obj);
		else if (Class.contains("List"))
			sends = (List<String>) obj;
		send(sends, Msg, Subject);
	}

	public void send(String Sender, String Msg, String Subject) {

		List<String> sends = new ArrayList<String>();
		sends.add(Sender);
		send(sends, Msg, Subject);
	}

	private List<String> createList(Assessment a) {
		List<String> emails = new ArrayList<String>();
		for (User u : a.getAssessor())
			emails.add(u.getEmail());
		emails.add(a.getRemediation().getEmail());
		String distro = a.getDistributionList();
		if (distro != null && !distro.trim().equals("")) {
			String[] dlist = distro.split(";");
			for (String d : dlist)
				emails.add(d);
		}

		return emails;

	}

	private List<String> createList(PeerReview pr) {
		List<String> emails = new ArrayList<String>();
		for (User u : pr.getAssessment().getAssessor())
			emails.add(u.getEmail());

		return emails;

	}

	private List<String> createList(Verification v) {
		List<String> emails = new ArrayList<String>();
		emails.add(v.getAssessor().getEmail());
		emails.add(v.getAssignedRemediation().getEmail());
		String distro = v.getAssessment().getDistributionList();
		if (distro != null && !distro.trim().equals("")) {
			String[] dlist = distro.split(";");
			for (String d : dlist)
				emails.add(d);
		}
		return emails;

	}

	public void send(List<String> Sender, String Msg, String Subject) {

		String prefix = "";
		String signature = "";

		if (emailSettings.getPrefix() != null)
			prefix = emailSettings.getPrefix();

		if (emailSettings.getSignature() != null)
			signature = emailSettings.getSignature();

		Subject = prefix + " " + Subject;
		Msg = Msg + "<br>" + signature.replace("\n", "");
		Properties mailServerProperties = System.getProperties();
		mailServerProperties.put("mail.smtp.port", emailSettings.getPort());
		mailServerProperties.put("mail.smtp.host", emailSettings.getServer());

		if (emailSettings.getEmailSSL()) {

			mailServerProperties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			mailServerProperties.put("mail.smtp.ssl.socketFactory.port", emailSettings.getPort());
			mailServerProperties.put("mail.smtp.socketFactory.fallback", "false");
		} else {
			mailServerProperties.remove("mail.smtp.socketFactory.class");
			mailServerProperties.remove("mail.smtp.ssl.socketFactory.port");
			mailServerProperties.remove("mail.smtp.socketFactory.fallback");
		}

		if (emailSettings.getTls()) {
			mailServerProperties.put("mail.smtp.starttls.enable", "true");
			mailServerProperties.setProperty("mail.smtp.ssl.protocols", "TLSv1.2");
		} else {
			mailServerProperties.put("mail.smtp.starttls.enable", "false");
		}

		if (emailSettings.getEmailAuth()) {
			mailServerProperties.put("mail.smtp.auth", "true");
		} else {
			mailServerProperties.put("mail.smtp.auth", "false");
		}

		Session getMailSession = Session.getDefaultInstance(mailServerProperties, null);
		MimeMessage generateMailMessage = new MimeMessage(getMailSession);
		try {
			for (String s : Sender) {
				generateMailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(s));
			}
			generateMailMessage.setSubject(Subject);

			BodyPart messageBodyPart = new MimeBodyPart();
			List<String> images = new ArrayList();
			Pattern p = Pattern.compile("<img .* src=\""); // insert your pattern here

			if (Msg.contains("<img")) {
				Matcher m = p.matcher(Msg);
				int start = 0;
				while (m.find(start)) {
					start = m.end();
					int end = Msg.indexOf("\"", start);
					String data = Msg.substring(start, end);
					images.add(data);
					start = end;
				}
			}

			String removeImages = Msg.replaceAll("<img.*/>", "");
			messageBodyPart.setContent(removeImages, "text/html");
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);
			int count = 1;
			for (String b64 : images) {

				b64 = b64.replace("data:", "");
				String type = b64.split(";")[0];
				b64 = b64.split(",")[1];
				b64 = b64.replaceAll(" ", "+");
				byte[] image = Base64.getDecoder().decode(b64);
				messageBodyPart = new MimeBodyPart();
				messageBodyPart.setFileName("Attachment" + (count++) + "." + type.split("/")[1]);
				DataSource source = new ByteArrayDataSource(image, type);
				messageBodyPart.setDataHandler(new DataHandler(source));
				multipart.addBodyPart(messageBodyPart);

			}

			generateMailMessage.setContent(multipart);

			generateMailMessage.setFrom(emailSettings.getFromAddress());

			if (emailSettings.getEmailAuth()) {
				Transport transport = getMailSession.getTransport(emailSettings.getType());
				String emailPass = FSUtils.decryptPassword(emailSettings.getPassword());
				transport.connect(emailSettings.getServer(), emailSettings.getUname(), emailPass);
				transport.sendMessage(generateMailMessage, generateMailMessage.getAllRecipients());
			} else {
				Transport.send(generateMailMessage);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
