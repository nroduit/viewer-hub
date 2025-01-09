
/*
 *  Copyright (c) 2022-2024 Weasis Team and other contributors.
 *
 *  This program and the accompanying materials are made available under the terms of the Eclipse
 *  Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 *  License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *  SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package org.weasis.manager.back.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.weasis.manager.back.enums.WeasisPropertyCategory;
import org.weasis.manager.back.enums.WeasisPropertyJavaType;
import org.weasis.manager.back.enums.WeasisPropertyType;

import java.io.Serial;
import java.io.Serializable;

@Entity
@Table(name = "weasis_property")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, exclude = "overrideConfigEntity")
public class WeasisPropertyEntity extends AuditEntity implements Serializable {

	@Serial
	private static final long serialVersionUID = 1299695343489721323L;

	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	@JsonIgnore
	private Long id;

	@Column(name = "property_code", nullable = false)
	private String code;

	@Column(name = "property_value")
	private String value;

	@Column(name = "description")
	private String description;

	@Column(name = "default_property_value")
	private String defaultValue;

	@Column(name = "type")
	private WeasisPropertyType type;

	@Column(name = "category")
	private WeasisPropertyCategory category;

	@Column(name = "java_type")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private WeasisPropertyJavaType javaType;

	@JsonBackReference
	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, optional = false)
	@JoinColumns({
			@JoinColumn(name = "override_config_package_version_id", referencedColumnName = "package_version_id"),
			@JoinColumn(name = "override_config_launch_config_id", referencedColumnName = "launch_config_id"),
			@JoinColumn(name = "override_config_target_id", referencedColumnName = "target_id") })
	private OverrideConfigEntity overrideConfigEntity;

	/**
	 * Create a new entity based on the values of the entity in parameter.
	 * @param weasisPropertyEntity Entity to copy
	 * @return new entity copied
	 */
	public static WeasisPropertyEntity copy(WeasisPropertyEntity weasisPropertyEntity) {
		return WeasisPropertyEntity.builder()
			.code(weasisPropertyEntity.getCode())
			.value(weasisPropertyEntity.getValue())
			.description(weasisPropertyEntity.getDescription())
			.defaultValue(weasisPropertyEntity.getDefaultValue())
			.type(weasisPropertyEntity.getType())
			.category(weasisPropertyEntity.getCategory())
			.javaType(weasisPropertyEntity.getJavaType())
			.overrideConfigEntity(weasisPropertyEntity.getOverrideConfigEntity())
			.build();
	}

	/**
	 * Replace null values of the WeasisPropertyEntity to evaluate by the values of the
	 * default WeasisPropertyEntity
	 * @param defaultWeasisProperty Default weasis property
	 */
	public void replaceNullByDefault(WeasisPropertyEntity defaultWeasisProperty) {
		if (defaultWeasisProperty != null) {
			if (this.getValue() == null) {
				this.setValue(defaultWeasisProperty.getValue());
			}
			if (this.getDescription() == null) {
				this.setDescription(defaultWeasisProperty.getDescription());
			}
			if (this.getDefaultValue() == null) {
				this.setDefaultValue(defaultWeasisProperty.getDefaultValue());
			}
			if (this.getType() == null) {
				this.setType(defaultWeasisProperty.getType());
			}
			if (this.getCategory() == null) {
				this.setCategory(defaultWeasisProperty.getCategory());
			}
			if (this.getJavaType() == null) {
				this.setJavaType(defaultWeasisProperty.getJavaType());
			}
			if (this.getOverrideConfigEntity() == null) {
				this.setOverrideConfigEntity(defaultWeasisProperty.getOverrideConfigEntity());
			}
		}
	}

}
