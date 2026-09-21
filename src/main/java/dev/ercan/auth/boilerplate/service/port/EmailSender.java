package dev.ercan.auth.boilerplate.service.port;

import dev.ercan.auth.boilerplate.model.pojo.OtpDetail;

public interface EmailSender {

  void sendOtp(OtpDetail otpDetail);

}
