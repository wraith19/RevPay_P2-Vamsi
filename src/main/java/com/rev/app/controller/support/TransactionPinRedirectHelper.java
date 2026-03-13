package com.rev.app.controller.support;

import com.rev.app.entity.User;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Component
public class TransactionPinRedirectHelper {

    public String ensurePinConfigured(User user, RedirectAttributes redirectAttributes, String returnTo) {
        if (user.getTransactionPin() == null || user.getTransactionPin().isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Set your transaction PIN before making transactions.");
            return "redirect:/profile/set-pin?returnTo=" + returnTo;
        }
        return null;
    }
}
