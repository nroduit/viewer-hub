/*
 *  Copyright (c) 2022-2025 Weasis Team and other contributors.
 *
 *  This program and the accompanying materials are made available under the terms of the Eclipse
 *  Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 *  License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *  SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package org.weasis.manager.back.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.CascadeType;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entity for the table override_config.
 */
@Entity
@Table(name = "override_config")
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class OverrideConfigEntity extends AuditEntity implements Serializable {

	@Serial
	private static final long serialVersionUID = 5489704615651135697L;

	@EmbeddedId
	@JsonIgnore
	private OverrideConfigEntityPK overrideConfigEntityPK = new OverrideConfigEntityPK();

	@OneToOne
	@MapsId("packageVersionId")
	@JsonIgnore
	private PackageVersionEntity packageVersion;

	@OneToOne
	@MapsId("launchConfigId")
	@JsonIgnore
	private LaunchConfigEntity launchConfig;

	@ManyToOne
	@MapsId("targetId")
	@JsonIgnore
	private TargetEntity target;

	// =====================================
	// ========== Properties ===============
	// =====================================
	@Builder.Default
	@JsonProperty("weasisPreferences")
	@JsonManagedReference
	@OneToMany(mappedBy = "overrideConfigEntity", cascade = CascadeType.ALL, orphanRemoval = true,
			fetch = FetchType.EAGER)
	private List<WeasisPropertyEntity> weasisPropertyEntities = new ArrayList<>();

	/**
	 * Copy default properties
	 * @param defaultOverrideConfig default OverrideConfigEntity
	 */
	public void replaceNullOrNotExistingPropertiesByDefault(OverrideConfigEntity defaultOverrideConfig) {
		if (defaultOverrideConfig != null) {
			// Handle existing property with value null
			this.weasisPropertyEntities.forEach(p -> {
				WeasisPropertyEntity defaultWeasisPropertyFound = defaultOverrideConfig.getWeasisPropertyEntities()
					.stream()
					.filter((dp) -> Objects.equals(p.getCode(), dp.getCode()))
					.findFirst()
					.orElse(null);
				p.replaceNullByDefault(defaultWeasisPropertyFound);
			});

			// Handle missing property whereas it is existing in default
			List<String> codesOfPropertiesToEvaluate = this.weasisPropertyEntities.stream()
				.map(WeasisPropertyEntity::getCode)
				.toList();

			List<String> missingCodes = defaultOverrideConfig.getWeasisPropertyEntities()
				.stream()
				.map(WeasisPropertyEntity::getCode)
				.filter((element) -> !codesOfPropertiesToEvaluate.contains(element))
				.toList();

			List<WeasisPropertyEntity> propertiesToAdd = defaultOverrideConfig.getWeasisPropertyEntities()
				.stream()
				.filter((p) -> missingCodes.contains(p.getCode()))
				.toList();

			ArrayList<WeasisPropertyEntity> modifiableList = new ArrayList<>(this.weasisPropertyEntities);
			modifiableList.addAll(propertiesToAdd.stream().map(WeasisPropertyEntity::copy).toList());
			this.setWeasisPropertyEntities(modifiableList);
		}
	}

	@Override
	public String toString() {
		return "OverrideConfigEntity{" + "packageVersion=" + this.packageVersion + ", launchConfig=" + this.launchConfig
				+ ", target=" + this.target + '}';
	}

}
