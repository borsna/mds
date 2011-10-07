package org.datacite.mds.web.ui.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.apache.commons.lang.BooleanUtils;
import org.datacite.mds.domain.Allocator;
import org.datacite.mds.mail.MailMessage;
import org.datacite.mds.mail.MailMessageFactory;
import org.datacite.mds.service.MagicAuthStringService;
import org.datacite.mds.service.MailService;
import org.datacite.mds.web.ui.UiController;
import org.datacite.mds.web.ui.UiUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.roo.addon.web.mvc.controller.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@RooWebScaffold(path = "allocators", formBackingObject = Allocator.class, delete = false)
@RequestMapping("/allocators")
@Controller
public class AllocatorController implements UiController {
    
    @Autowired
    private MagicAuthStringService magicAuthStringService;
    
    @Autowired
    MailService mailService;
    
    @Autowired
    MailMessageFactory mailMessageFactory;

    @RequestMapping(params = "find=BySymbolEquals", method = RequestMethod.GET)
    public String findAllocatorsBySymbolEquals(@RequestParam("symbol") String symbol, Model model) {
        Allocator allocator = Allocator.findAllocatorBySymbol(symbol);
        return (allocator == null) ? "allocators/show" : "redirect:/allocators/" + allocator.getId();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String show(@PathVariable("id") Long id, Model model) {
        Allocator allocator = Allocator.findAllocator(id);
        model.addAttribute("allocator", allocator);
        model.addAttribute("itemId", id);
        model.addAttribute("magicAuthString", magicAuthStringService.getCurrentAuthString(allocator));
        return "allocators/show";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String create(@Valid Allocator allocator, BindingResult result, @RequestParam(required=false) Boolean sendWelcomeMail, Model model, HttpSession session) {
        if (result.hasErrors()) {
            model.addAttribute("allocator", allocator);
            return "allocators/create";
        }
        allocator.persist();
        UiUtils.refreshSymbolsForSwitchUser(session);
        
        if (BooleanUtils.isTrue(sendWelcomeMail)) {
            MailMessage mail = mailMessageFactory.createWelcomeAllocatorMail(allocator);
            mailService.sendAsync(mail);
        }
        
        return "redirect:/allocators/" + allocator.getId();
    }

    @RequestMapping(value = "/{id}", params = "form", method = RequestMethod.GET)
    public String updateForm(@PathVariable("id") Long id, Model model) {
        Allocator allocator = Allocator.findAllocator(id);
        model.addAttribute("allocator", allocator);
        model.addAttribute("magicAuthString", magicAuthStringService.getCurrentAuthString(allocator));
        return "allocators/update";
    }
    
    
    @RequestMapping(method = RequestMethod.PUT)
    public String update(@Valid Allocator allocator, BindingResult result, Model model, HttpSession session) {
        if (result.hasErrors()) {
            model.addAttribute("allocator", allocator);
            return "allocators/update";
        }
        allocator.merge();
        UiUtils.refreshSymbolsForSwitchUser(session);
        return "redirect:/allocators/" + allocator.getId();
    }

}
