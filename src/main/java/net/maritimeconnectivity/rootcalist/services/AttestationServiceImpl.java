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

package net.maritimeconnectivity.rootcalist.services;

import net.maritimeconnectivity.rootcalist.model.Attestation;
import net.maritimeconnectivity.rootcalist.repositories.AttestationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AttestationServiceImpl extends BaseServiceImpl<Attestation> implements AttestationService {

    private AttestationRepository attestationRepository;

    @Autowired
    public void setAttestationRepository(AttestationRepository attestationRepository) {
        this.attestationRepository = attestationRepository;
    }

    @Override
    public AttestationRepository getRepository() {
        return this.attestationRepository;
    }
}