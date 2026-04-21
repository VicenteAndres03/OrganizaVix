import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailService {

    private static final String FROM = "vixdeev@gmail.com";
    private static final String PASSWORD = "ueha enzd pvvb jaji";

    public static boolean enviarCodigoRecuperacion(String destinatario, String codigo) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM, PASSWORD);
            }
        });

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(FROM));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            msg.setSubject("Recuperación de contraseña");
            msg.setText(
                    "Hola,\n\n" +
                            "Tu código de recuperación es:\n\n" +
                            "  " + codigo + "\n\n" +
                            "Este código expira en 15 minutos.\n\n" +
                            "Si no solicitaste esto, ignora este mensaje.");
            Transport.send(msg);
            return true;
        } catch (MessagingException e) {
            System.err.println("Error al enviar email: " + e.getMessage());
            return false;
        }
    }
}