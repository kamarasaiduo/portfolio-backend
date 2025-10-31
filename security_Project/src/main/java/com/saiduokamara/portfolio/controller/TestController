@RestController
public class TestController {

    @Autowired
    private JavaMailSender mailSender;

    @GetMapping("/test-email")
    public String testEmail() {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo("kamarasaidu558@gmail.com");
            message.setSubject("qkjdagnldktnlccu");
            message.setText("This is a test email from your application.");
            message.setFrom("Saidu's Portfolio");

            mailSender.send(message);
            return "Email sent successfully!";
        } catch (Exception e) {
            return "Email failed: " + e.getMessage();
        }
    }
}