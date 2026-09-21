package dev.ercan.auth.boilerplate.provider.email;

import dev.ercan.auth.boilerplate.model.pojo.OtpDetail;
import dev.ercan.auth.boilerplate.service.port.EmailSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "email.sender-type", havingValue = "console")
public class ConsoleMailSender implements EmailSender {

  @Override
  public void sendOtp(OtpDetail otpDetail) {
    log.info("[CONSOLE_MAIL_SENDER] [MAIL_TO={}] [OTP={}]", otpDetail.dataToVerify(), otpDetail.value());
  }

}
