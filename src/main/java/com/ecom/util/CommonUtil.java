package com.ecom.util;

import com.ecom.model.ProductOrder;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;

@Component
public class CommonUtil {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${aws.s3.bucket.category}")
    private String categoryBucket;

    @Value("${aws.s3.bucket.product}")
    private String productBucket;

    @Value("${aws.s3.bucket.profile}")
    private String profileBucket;

    public Boolean sendMail(String url, String recipientEmail) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        helper.setFrom("saurav.kumar3279@gmail.com", "Ecommerce App");
        helper.setTo(recipientEmail);
        String content = "<p>Hello, </p>" + "<p>You have requested to reset your reset your password.</p>"
                + "<p>Click the link below to change your password: </p>" + "<p><a href=\"" + url
                + "\">Change my password</a></p>";
        helper.setSubject("Password Reset");
        helper.setText(content, true);
        mailSender.send(message);
        return true;
    }

    public static String generateUrl(HttpServletRequest request) {
        String siteUrl = request.getRequestURL().toString();
        return siteUrl.replace(request.getServletPath(), "");
    }

    public void sendMailForProductOrder(ProductOrder order, String status) throws MessagingException, UnsupportedEncodingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        helper.setFrom("saurav.kumar3279@gmail.com", "Ecommerce App");
        helper.setTo(order.getOrderAddress().getEmail());

        String content = "<p>Hi [[name]], your order status is <b>[[orderStatus]]</b></p>"
                + "<p><b>Product Details -</b></p>"
                + "<p>Name: [[productName]]</p>"
                + "<p>Category: [[category]]</p>"
                + "<p>Quantity: " + order.getQuantity() + "</p>"
                + "<p>Price: [[price]]</p>"
                + "<p>Payment Type: [[paymentType]]</p>";
        content = content.replace("[[name]]", order.getOrderAddress().getFirstName());
        content = content.replace("[[orderStatus]]", status);
        content = content.replace("[[productName]]", order.getProduct().getTitle());
        content = content.replace("[[category]]", order.getProduct().getCategory());
        content = content.replace("[[price]]", order.getProduct().getDiscountPrice().toString());
        content = content.replace("[[paymentType]]", order.getPaymentType());

        helper.setSubject("Product Order Status");
        helper.setText(content, true);
        mailSender.send(message);
    }

    public String getImageUrl(MultipartFile file, Integer bucketType) {
        String bucketName = null;
        if(bucketType == 1)
            bucketName = categoryBucket;
        else if(bucketType == 2)
            bucketName = productBucket;
        else if(bucketType == 3)
            bucketName = profileBucket;
        String imageName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
        return "https://" + bucketName + ".s3.ap-southeast-2.amazonaws.com/" + imageName;
    }
}
