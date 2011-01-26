package org.datacite.mds.web.api.controller;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.datacite.mds.domain.Datacentre;
import org.datacite.mds.domain.Dataset;
import org.datacite.mds.domain.Metadata;
import org.datacite.mds.service.DoiService;
import org.datacite.mds.service.HandleException;
import org.datacite.mds.service.SecurityException;
import org.datacite.mds.util.SecurityUtils;
import org.datacite.mds.validation.ValidationException;
import org.datacite.mds.validation.ValidationHelper;
import org.datacite.mds.web.api.ApiController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/*")
@Controller
public class MetadataApiController implements ApiController {

    private static Logger log4j = Logger.getLogger(MetadataApiController.class);
    
    @Autowired
    DoiService doiService;

    @Autowired
    ValidationHelper validationHelper;

    @RequestMapping(value = "metadata", method = RequestMethod.GET, headers = { "Accept=application/xml" })
    public ResponseEntity<? extends Object> get(@RequestParam String doi) {
        HttpHeaders headers = new HttpHeaders();

        Datacentre datacentre;
        try {
            datacentre = SecurityUtils.getCurrentDatacentreWithException();
        } catch (Exception e) {
            return new ResponseEntity<String>(e.getMessage(), headers, HttpStatus.FORBIDDEN);
        } 
        
        Dataset dataset = Dataset.findDatasetByDoi(doi);
        if (dataset == null)
            return new ResponseEntity<String>("DOI doesn't exist", headers, HttpStatus.NOT_FOUND);

        if (!datacentre.getSymbol().equals(dataset.getDatacentre().getSymbol()))
            return new ResponseEntity<String>("cannot retrieve metadata which belongs to another party", headers, HttpStatus.FORBIDDEN);

        if (!dataset.getIsActive())
            return new ResponseEntity<String>("dataset inactive", headers, HttpStatus.GONE);

        Metadata metadata = Metadata.findLatestMetadatasByDataset(dataset);
        if (metadata == null)
            return new ResponseEntity<String>("no metadata for the DOI", headers, HttpStatus.NOT_FOUND);

        headers.setContentType(MediaType.APPLICATION_XML);
        return new ResponseEntity<Object>(metadata.getXml(), headers, HttpStatus.OK);
    }
    
//    @RequestMapping(value = "metadata", method = { RequestMethod.PUT }, headers = { "Content-Type=application/xml;charset=UTF-8" })
    @RequestMapping(value = "metadata", method = { RequestMethod.PUT, RequestMethod.POST }, headers = { "Content-Type=application/xml;charset=UTF-8" })
        public ResponseEntity<String> createOrUpdate(@RequestBody String body, 
                                             @RequestParam String doi,
                                             @RequestParam(required = false) String url, 
                                             @RequestParam(required = false) Boolean testMode,
                                             HttpServletRequest httpRequest) throws ValidationException {

        String method = httpRequest.getMethod();
        String logPrefix = "*****" + method + " metadata: ";
        if (testMode == null)
            testMode = false;
        HttpHeaders headers = new HttpHeaders();

        log4j.debug(logPrefix + doi + ", url: " + url + " \ntestMode = " + testMode);

        Metadata metadata = new Metadata();
        try {
            metadata.setXml(body.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            return new ResponseEntity<String>(e.getMessage(), headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
   
        metadata.setDataset(new Dataset());
        String violationMessages = validationHelper.getViolationMessages(metadata);
        if (violationMessages != null) {
            return new ResponseEntity<String>(violationMessages, headers, HttpStatus.FORBIDDEN);
        }
        
        Dataset dataset;

        try {
            if (method.equals("POST")) {
                dataset = doiService.create(doi, url, testMode);
            } else { // PUT
                dataset = doiService.update(doi, url, testMode);
            }
        } catch (SecurityException e) {
            return new ResponseEntity<String>(e.getMessage(), headers, HttpStatus.FORBIDDEN);
        } catch (RuntimeException e) {
            return new ResponseEntity<String>(e.getMessage(), headers, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (HandleException e) {
            return new ResponseEntity<String>(e.getMessage(), headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        log4j.debug(logPrefix + "dataset id = " + dataset.getId());
        metadata.setDataset(dataset);
        if (!testMode) {
            log4j.debug(logPrefix + "persisting XML");
            metadata.persist();
        }

        return new ResponseEntity<String>("OK", headers, HttpStatus.CREATED);
    }


    @RequestMapping(value = "metadata", method = RequestMethod.DELETE)
    public ResponseEntity<String> delete(@RequestParam String doi,
            @RequestParam(required = false) Boolean testMode) {
        log4j.debug("*****DELETE metadata: " + doi + " \ntestMode = " + testMode);

        if (testMode == null)
            testMode = false;

        HttpHeaders headers = new HttpHeaders();
        Datacentre datacentre;
        Dataset dataset;

        try {
            datacentre = SecurityUtils.getCurrentDatacentreWithException();
        } catch (Exception e) {
            return new ResponseEntity<String>(e.getMessage(), headers, HttpStatus.FORBIDDEN);
        } 

        dataset = Dataset.findDatasetByDoi(doi);
        if (dataset == null)
            return new ResponseEntity<String>("DOI doesn't exist", headers, HttpStatus.NOT_FOUND);

        if (!datacentre.getSymbol().equals(dataset.getDatacentre().getSymbol()))
            return new ResponseEntity<String>("cannot delete DOI which belongs to another party", headers, HttpStatus.FORBIDDEN);
        
        if (!testMode) {
            if (dataset.getIsActive() == null)
                dataset.setIsActive(false);
            else
                dataset.setIsActive(!dataset.getIsActive());
            dataset.merge();
        }

        return new ResponseEntity<String>("OK", headers, HttpStatus.OK);
    }
}
