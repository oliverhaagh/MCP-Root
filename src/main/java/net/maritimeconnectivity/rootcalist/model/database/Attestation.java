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

package net.maritimeconnectivity.rootcalist.model.database;

import lombok.Getter;
import lombok.Setter;
import net.maritimeconnectivity.rootcalist.model.AttestationRequest;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "attestation")
@Getter
@Setter
public class Attestation extends SignatureModel {

    @OneToOne(mappedBy = "attestation")
    private Revocation revocation;

    public Attestation() {
        // empty constructor
    }

    public Attestation(AttestationRequest attestationRequest) {
        this.signature = attestationRequest.getSignature();
        this.algorithmIdentifier = attestationRequest.getAlgorithmIdentifier();
    }
}
