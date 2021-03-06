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

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

@Getter
@Setter
@MappedSuperclass
public abstract class SignatureModel extends TimestampModel {

    @ApiModelProperty(
            value = "HEX encoded signature signed with the private key of the attestor",
            required = true
    )
    @Column(name = "signature", nullable = false)
    protected String signature;

    @ApiModelProperty(
            value = "The identifier of the algorithm that was used to generate the signature",
            required = true
    )
    @Column(name = "algorithm", nullable = false)
    protected String algorithmIdentifier;

    @ManyToOne
    @JoinColumn(name = "id_root_ca")
    protected RootCA rootCA;

    @ManyToOne
    @JoinColumn(name = "id_attestor")
    protected Attestor attestor;
}
