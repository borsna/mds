// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package org.datacite.mds.web.ui.controller;

import java.io.UnsupportedEncodingException;
import java.lang.Integer;
import java.lang.Long;
import java.lang.String;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.datacite.mds.domain.Prefix;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

privileged aspect PrefixController_Roo_Controller {
    
    @RequestMapping(method = RequestMethod.POST)
    public String PrefixController.create(@Valid Prefix prefix, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            uiModel.addAttribute("prefix", prefix);
            return "prefixes/create";
        }
        uiModel.asMap().clear();
        prefix.persist();
        return "redirect:/prefixes/" + encodeUrlPathSegment(prefix.getId().toString(), httpServletRequest);
    }
    
    @RequestMapping(params = "form", method = RequestMethod.GET)
    public String PrefixController.createForm(Model uiModel) {
        uiModel.addAttribute("prefix", new Prefix());
        return "prefixes/create";
    }
    
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String PrefixController.show(@PathVariable("id") Long id, Model uiModel) {
        uiModel.addAttribute("prefix", Prefix.findPrefix(id));
        uiModel.addAttribute("itemId", id);
        return "prefixes/show";
    }
    
    @RequestMapping(method = RequestMethod.GET)
    public String PrefixController.list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            uiModel.addAttribute("prefixes", Prefix.findPrefixEntries(page == null ? 0 : (page.intValue() - 1) * sizeNo, sizeNo));
            float nrOfPages = (float) Prefix.countPrefixes() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("prefixes", Prefix.findAllPrefixes());
        }
        return "prefixes/list";
    }
    
    @RequestMapping(method = RequestMethod.PUT)
    public String PrefixController.update(@Valid Prefix prefix, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            uiModel.addAttribute("prefix", prefix);
            return "prefixes/update";
        }
        uiModel.asMap().clear();
        prefix.merge();
        return "redirect:/prefixes/" + encodeUrlPathSegment(prefix.getId().toString(), httpServletRequest);
    }
    
    @RequestMapping(value = "/{id}", params = "form", method = RequestMethod.GET)
    public String PrefixController.updateForm(@PathVariable("id") Long id, Model uiModel) {
        uiModel.addAttribute("prefix", Prefix.findPrefix(id));
        return "prefixes/update";
    }
    
    String PrefixController.encodeUrlPathSegment(String pathSegment, HttpServletRequest httpServletRequest) {
        String enc = httpServletRequest.getCharacterEncoding();
        if (enc == null) {
            enc = WebUtils.DEFAULT_CHARACTER_ENCODING;
        }
        try {
            pathSegment = UriUtils.encodePathSegment(pathSegment, enc);
        }
        catch (UnsupportedEncodingException uee) {}
        return pathSegment;
    }
    
}
