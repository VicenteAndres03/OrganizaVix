import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailService {

    private static final String FROM = "vixdeev@gmail.com";
    private static final String PASSWORD = "ueha enzd pvvb jaji";

    public static boolean enviarCodigoRecuperacion(String destinatario, String codigo) {
        // Forzar carga de jakarta.activation antes de que javax.mail lo necesite
        try {
            Class.forName("com.sun.activation.registries.MimeTypeFile");
        } catch (ClassNotFoundException ignored) {
            // Si no encuentra esa clase interna, intenta la pública
            try {
                Class.forName("jakarta.activation.DataHandler");
            } catch (ClassNotFoundException e2) {
                System.err.println("jakarta.activation no está en el classpath: " + e2.getMessage());
                return false;
            }
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM, PASSWORD);
            }
        });

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(FROM));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            msg.setSubject("Recuperación de contraseña - OrganizaVix");

            // Usar MimeMultipart para evitar problemas con DataHandler
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(
                    "Hola,\n\n" +
                            "Tu código de recuperación es:\n\n" +
                            "  " + codigo + "\n\n" +
                            "Este código expira en 15 minutos.\n\n" +
                            "Si no solicitaste esto, ignora este mensaje.\n\n" +
                            "— OrganizaVix");

            MimeMultipart multipart = new MimeMultipart();
            multipart.addBodyPart(textPart);
            msg.setContent(multipart);

            Transport.send(msg);
            return true;
        } catch (MessagingException e) {
            System.err.println("Error al enviar email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}