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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import net.maritimeconnectivity.rootcalist.exception.BasicRestException;
import net.maritimeconnectivity.rootcalist.model.RevocationRequest;
import net.maritimeconnectivity.rootcalist.model.database.Attestation;
import net.maritimeconnectivity.rootcalist.model.database.Attestor;
import net.maritimeconnectivity.rootcalist.model.database.Revocation;
import net.maritimeconnectivity.rootcalist.model.database.RootCA;
import net.maritimeconnectivity.rootcalist.services.AttestationService;
import net.maritimeconnectivity.rootcalist.services.AttestorService;
import net.maritimeconnectivity.rootcalist.services.RevocationService;
import net.maritimeconnectivity.rootcalist.services.RootCAService;
import net.maritimeconnectivity.rootcalist.utils.CryptoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
public class RevocationController {

    private RevocationService revocationService;
    private AttestationService attestationService;
    private RootCAService rootCAService;
    private AttestorService attestorService;

    @Autowired
    public void setRevocationService(RevocationService revocationService) {
        this.revocationService = revocationService;
    }

    @Autowired
    public void setAttestationService(AttestationService attestationService) {
        this.attestationService = attestationService;
    }

    @Autowired
    public void setRootCAService(RootCAService rootCAService) {
        this.rootCAService = rootCAService;
    }

    @Autowired
    public void setAttestorService(AttestorService attestorService) {
        this.attestorService = attestorService;
    }

    @GetMapping(
            value = "/revocations",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            description = "Gets the list of all revocations."
    )
    public ResponseEntity<List<Revocation>> getRevocations() {
        List<Revocation> revocations = this.revocationService.listAll();
        return new ResponseEntity<>(revocations, HttpStatus.OK);
    }

    @GetMapping(
            value = "/revocation/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            description = "Gets a specific revocation based on the given ID."
    )
    public ResponseEntity<Revocation> getRevocation(@PathVariable @Parameter(description = "The ID of the revocation") Long id) {
        Revocation revocation = this.revocationService.getById(id);
        if (revocation == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(revocation, HttpStatus.OK);
    }

    @PostMapping(
            value = "/revocation",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            description = "Creates a new revocation of a previous attestation."
    )
    public ResponseEntity<Revocation> createRevocation(HttpServletRequest request, @RequestBody RevocationRequest input) throws BasicRestException {
        if (input.getAttestorId() != null && input.getRootCAid() != null && input.getAttestationId() != null) {
            Attestor attestor = this.attestorService.getById(input.getAttestorId());
            Attestation attestation = this.attestationService.getById(input.getAttestationId());
            RootCA rootCA = this.rootCAService.getById(input.getRootCAid());
            if (attestation != null && attestation.getRootCA().equals(rootCA) && attestation.getAttestor().equals(attestor)) {
                try {
                    if (CryptoUtil.isSignatureValid(input.getSignature(), input.getAlgorithmIdentifier(), attestor, attestation.getSignature())) {
                        Revocation temp = new Revocation(input);
                        temp.setAttestation(attestation);
                        temp.setAttestor(attestor);
                        temp.setRootCA(rootCA);
                        Revocation newRevocation = this.revocationService.save(temp);
                        return new ResponseEntity<>(newRevocation, HttpStatus.OK);
                    }
                } catch (IOException | SignatureException | InvalidKeyException | CertificateException | NoSuchAlgorithmException | NoSuchProviderException e) {
                    log.error("Signature could not be verified", e);
                    throw new BasicRestException(HttpStatus.BAD_REQUEST, "The signature of the revocation could not be verified", request.getServletPath());
                } catch (DataIntegrityViolationException e) {
                    log.error("New revocation could not be persisted because it already exists", e);
                    throw new BasicRestException(HttpStatus.BAD_REQUEST, "A similar revocation already exists", request.getServletPath());
                }
            }
        }
        throw new BasicRestException(HttpStatus.BAD_REQUEST, "The request did not contain all required attributes", request.getServletPath());
    }
}
