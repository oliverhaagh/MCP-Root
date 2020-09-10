/*
 * Copyright 2020 Maritime Connectivity Platform Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.maritimeconnectivity.rootcalist.controllers;

import lombok.extern.slf4j.Slf4j;
import net.maritimeconnectivity.rootcalist.model.RootCA;
import net.maritimeconnectivity.rootcalist.services.RootCAService;
import org.bouncycastle.cert.CertException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509ContentVerifierProviderBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.operator.OperatorCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.StringReader;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/")
public class RootCAController {

    private RootCAService rootCAService;

    @Autowired
    public void setRootCAService(RootCAService rootCAService) {
        this.rootCAService = rootCAService;
    }

    @RequestMapping(
            value = "/roots",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<RootCA>> getRootCAs(@RequestParam(required = false, name = "attestorId") List<Long> attestorIds) {
        if (attestorIds != null) {
            return new ResponseEntity<>(this.rootCAService.listByAttestors(attestorIds), HttpStatus.OK);
        }
        return new ResponseEntity<>(this.rootCAService.listAll(), HttpStatus.OK);
    }

    @RequestMapping(
            value = "/root/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<RootCA> getRootCA(@PathVariable Long id) {
        RootCA rootCA = this.rootCAService.getById(id);
        if (rootCA != null) {
            return new ResponseEntity<>(rootCA, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @RequestMapping(
            value = "/root",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<RootCA> createRootCA(@RequestBody RootCA rootCA) {
        PEMParser pemParser = new PEMParser(new StringReader(rootCA.getCertificate()));
        try {
            X509CertificateHolder certificateHolder = (X509CertificateHolder) pemParser.readObject();
            if (certificateHolder.isValidOn(new Date()) && isSelfSigned(certificateHolder)) {
                RootCA newRootCA = this.rootCAService.save(rootCA);
                return new ResponseEntity<>(newRootCA, HttpStatus.OK);
            }
        } catch (IOException e) {
            log.error("New root CA certificate could not be read");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    private boolean isSelfSigned(X509CertificateHolder certificateHolder) {
        if (certificateHolder.getSubject().equals(certificateHolder.getIssuer())) {
            JcaX509ContentVerifierProviderBuilder contentVerifierProviderBuilder = new JcaX509ContentVerifierProviderBuilder();
            contentVerifierProviderBuilder.setProvider(new BouncyCastleProvider());
            try {
                return certificateHolder.isSignatureValid(contentVerifierProviderBuilder.build(certificateHolder));
            } catch (CertException | OperatorCreationException e) {
                return false;
            }
        }
        return false;
    }
}
