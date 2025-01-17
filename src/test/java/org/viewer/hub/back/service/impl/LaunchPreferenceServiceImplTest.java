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

package org.viewer.hub.back.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.viewer.hub.back.controller.exception.ConstraintException;
import org.viewer.hub.back.controller.exception.ParameterException;
import org.viewer.hub.back.entity.GroupEntity;
import org.viewer.hub.back.entity.GroupEntityPK;
import org.viewer.hub.back.entity.LaunchConfigEntity;
import org.viewer.hub.back.entity.LaunchEntity;
import org.viewer.hub.back.entity.LaunchEntityPK;
import org.viewer.hub.back.entity.LaunchPreferredEntity;
import org.viewer.hub.back.entity.PackageVersionEntity;
import org.viewer.hub.back.entity.TargetEntity;
import org.viewer.hub.back.enums.PreferredType;
import org.viewer.hub.back.enums.TargetType;
import org.viewer.hub.back.repository.GroupRepositoryTest;
import org.viewer.hub.back.repository.LaunchConfigRepository;
import org.viewer.hub.back.repository.LaunchPreferredRepository;
import org.viewer.hub.back.repository.LaunchRepository;
import org.viewer.hub.back.repository.LaunchRepositoryTest;
import org.viewer.hub.back.repository.TargetRepository;
import org.viewer.hub.back.service.GroupService;
import org.viewer.hub.back.service.LaunchPreferenceService;
import org.viewer.hub.back.service.OverrideConfigService;
import org.viewer.hub.back.service.PackageService;
import org.viewer.hub.back.util.PackageUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LaunchPreferenceServiceImplTest {

	// Init mocks
	private final LaunchRepository launchRepositoryMock = Mockito.mock(LaunchRepository.class);

	private final LaunchConfigRepository launchConfigRepositoryMock = Mockito.mock(LaunchConfigRepository.class);

	private final LaunchPreferredRepository launchPreferedRepositoryMock = Mockito
		.mock(LaunchPreferredRepository.class);

	private final TargetRepository targetRepositoryMock = Mockito.mock(TargetRepository.class);

	private final GroupService groupServiceMock = Mockito.mock(GroupService.class);

	private final PackageService packageServiceMock = Mockito.mock(PackageService.class);

	private final OverrideConfigService overrideConfigServiceMock = Mockito.mock(OverrideConfigService.class);

	private LaunchPreferenceService launchPreferenceService;

	@BeforeEach
	public void setUp() {
		// Build entity for mocks
		// LaunchConfigEntity
		LaunchConfigEntity launchConfigEntity = new LaunchConfigEntity();
		launchConfigEntity.setId(1L);
		launchConfigEntity.setName("LaunchConfigName");
		// TargetEntity
		TargetEntity targetEntity = new TargetEntity();
		targetEntity.setName("TargetName");
		targetEntity.setType(TargetType.USER);
		targetEntity.setId(1L);
		// LaunchPreferedEntity
		LaunchPreferredEntity launchPreferedEntity = new LaunchPreferredEntity();
		launchPreferedEntity.setId(1L);
		launchPreferedEntity.setName("launch");
		launchPreferedEntity.setType(PreferredType.EXT_CFG.getCode());
		// GroupEntity
		GroupEntity groupEntity = new GroupEntity();
		GroupEntityPK groupEntityPK = new GroupEntityPK();
		groupEntityPK.setGroupId(1L);
		groupEntityPK.setMemberId(1L);
		groupEntity.setGroupEntityPK(groupEntityPK);

		// Define the behaviour of the mocks

		// TargetRepository
		when(this.targetRepositoryMock.findByNameIgnoreCase(anyString())).thenReturn(targetEntity);
		when(this.targetRepositoryMock.findByNameIgnoreCaseAndType(anyString(), any(TargetType.class)))
			.thenReturn(targetEntity);
		when(this.targetRepositoryMock.existsByNameIgnoreCase(anyString())).thenReturn(false);
		when(this.targetRepositoryMock.save(any(TargetEntity.class))).thenReturn(targetEntity);
		when(this.targetRepositoryMock.findById(Mockito.anyLong())).thenReturn(Optional.of(targetEntity));

		// LaunchConfigRepository
		when(this.launchConfigRepositoryMock.findByName(anyString())).thenReturn(launchConfigEntity);
		when(this.launchConfigRepositoryMock.findAll()).thenReturn(Collections.singletonList(launchConfigEntity));
		when(this.launchConfigRepositoryMock.findById(Mockito.anyLong())).thenReturn(Optional.of(launchConfigEntity));
		when(this.launchConfigRepositoryMock.saveAll(Mockito.anyCollection()))
			.thenReturn(Collections.singletonList(launchConfigEntity));

		// LaunchPreferedRepository
		when(this.launchPreferedRepositoryMock.findByName(anyString())).thenReturn(launchPreferedEntity);
		when(this.launchPreferedRepositoryMock.findAll()).thenReturn(Collections.singletonList(launchPreferedEntity));
		when(this.launchPreferedRepositoryMock.findById(Mockito.anyLong()))
			.thenReturn(Optional.of(launchPreferedEntity));
		when(this.launchPreferedRepositoryMock.findByType(anyString()))
			.thenReturn(Collections.singletonList(launchPreferedEntity));

		// Group Service
		when(this.groupServiceMock.retrieveGroupsByMember(any(TargetEntity.class)))
			.thenReturn(Collections.singletonList(groupEntity));

		// LaunchRepository
		LaunchEntity launchEntity = new LaunchEntity();
		launchEntity.setSelection("weasis");
		LaunchEntityPK launchEntityPK = new LaunchEntityPK();
		launchEntityPK.setLaunchConfigId(1L);
		launchEntityPK.setLaunchPreferredId(1L);
		launchEntityPK.setTargetId(1L);
		launchEntity.setLaunchEntityPK(launchEntityPK);
		Optional<LaunchEntity> oLaunchEntity = Optional.of(launchEntity);
		when(this.launchRepositoryMock.findOne(any(Specification.class))).thenReturn(oLaunchEntity);
		when(this.launchRepositoryMock.save(any())).thenReturn(null);
		when(this.launchRepositoryMock.findAll(any(Specification.class)))
			.thenReturn(new ArrayList(List.of(launchEntity)));
		Mockito.doNothing().when(this.launchRepositoryMock).deleteAll(any());

		// Build the mocked launch preference service
		this.launchPreferenceService = new LaunchPreferenceServiceImpl(this.launchRepositoryMock,
				this.launchConfigRepositoryMock, this.launchPreferedRepositoryMock, this.targetRepositoryMock,
				this.groupServiceMock, this.packageServiceMock, this.overrideConfigServiceMock);
	}

	/**
	 * Should retrieve the launch value requested Initial: call the mocked service to
	 * retrieve the data
	 * <p>
	 * Expected: retrieve the mocked value "Value"
	 */
	@Test
	void shouldRetrieveLaunchValueRequested() {
		// Call service
		String valueToTest = this.launchPreferenceService.retrieveLaunchValue("TargetName", "ConfigName",
				"PreferedName", false);

		// Test result
		assertEquals("weasis", valueToTest);
	}

	/**
	 * Test method retrieveLaunchesByHostUserConfigOrderByTargets
	 * <p>
	 * Expected: - Retrieve the launch entity mocked
	 */
	@Test
	void shouldRetrieveLaunchesByHostUserConfigOrderByTargets() {
		// Call service
		List<LaunchEntity> launchEntities = this.launchPreferenceService
			.retrieveLaunchesByHostUserConfigPreferedTypeOrderByTargets("pc-001", "user", "default", null);

		// Test results
		assertEquals(1, launchEntities.size());
		assertEquals(Long.valueOf(1), launchEntities.get(0).getLaunchEntityPK().getLaunchPreferredId());
		assertEquals(Long.valueOf(1), launchEntities.get(0).getLaunchEntityPK().getLaunchConfigId());
		assertEquals(Long.valueOf(1), launchEntities.get(0).getLaunchEntityPK().getTargetId());
		assertEquals("LaunchConfigName", launchEntities.get(0).getAssociatedConfig().getName());
		assertEquals("launch", launchEntities.get(0).getAssociatedPreferred().getName());
		assertEquals("TARGETNAME", launchEntities.get(0).getAssociatedTarget().getName());
	}

	/**
	 * Test method buildLaunchConfiguration
	 * <p>
	 * Expected: retrieve mocked values - key: PreferedType.EXT_CFG - value: weasis
	 */

	@Test
	void shouldBuildLaunchConfiguration() {
		// Init data
		MultiValueMap<String, String> launchPropertiesMap = new LinkedMultiValueMap<>();
		launchPropertiesMap.add(PreferredType.QUALIFIER.getCode(), "-MGR");
		PackageVersionEntity packageVersionEntity = new PackageVersionEntity();
		packageVersionEntity.setVersionNumber("4.0.3");
		packageVersionEntity.setId(1L);
		packageVersionEntity.setQualifier("-MGR");
		packageVersionEntity.setDescription("description");
		LaunchConfigEntity launchConfigEntity = new LaunchConfigEntity();
		launchConfigEntity.setName("default");
		launchConfigEntity.setId(1L);

		// Mock
		when(this.packageServiceMock.retrieveAvailablePackageVersionToUse(anyString(), anyString()))
			.thenReturn(packageVersionEntity);
		when(this.launchConfigRepositoryMock.findByNameIgnoreCase(anyString())).thenReturn(launchConfigEntity);

		// Call service
		MultiValueMap<String, String> map = this.launchPreferenceService.buildLaunchConfiguration(launchPropertiesMap,
				"user", "host", "config", "4.0.3"); // "Weasis/4.0.3 (Windows 10)");

		// Test results
		assertEquals(10, map.size());
		assertEquals("weasis", map.get(PreferredType.EXT_CFG.getCode()).get(0));
	}

	@Test
	void when_buildLaunchConfiguration_with_launchConfigNull_should_useDefaultLaunchConfigAndDefaultTarget() {
		// Init data
		MultiValueMap<String, String> launchPropertiesMap = new LinkedMultiValueMap<>();
		launchPropertiesMap.add(PreferredType.QUALIFIER.getCode(), "-MGR");
		PackageVersionEntity packageVersionEntity = new PackageVersionEntity();
		packageVersionEntity.setVersionNumber("4.0.3");
		packageVersionEntity.setId(1L);
		packageVersionEntity.setQualifier("-MGR");
		packageVersionEntity.setDescription("description");

		LaunchConfigEntity launchConfigEntity = new LaunchConfigEntity();
		launchConfigEntity.setName("config");
		launchConfigEntity.setId(2L);

		LaunchConfigEntity defaultLaunchConfigEntity = new LaunchConfigEntity();
		defaultLaunchConfigEntity.setName("default");
		defaultLaunchConfigEntity.setId(1L);

		TargetEntity defaultTargetEntity = new TargetEntity();
		defaultTargetEntity.setId(1L);
		defaultTargetEntity.setType(TargetType.USER_GROUP);
		defaultTargetEntity.setName("default");

		// Mock
		when(this.packageServiceMock.retrieveAvailablePackageVersionToUse(anyString(), anyString()))
			.thenReturn(packageVersionEntity);
		when(this.launchConfigRepositoryMock.findByNameIgnoreCase(eq("default"))).thenReturn(defaultLaunchConfigEntity);
		when(this.launchConfigRepositoryMock.findByNameIgnoreCase(eq("config"))).thenReturn(launchConfigEntity);
		when(this.targetRepositoryMock.findByNameIgnoreCase(eq("default"))).thenReturn(defaultTargetEntity);

		// Call service
		MultiValueMap<String, String> map = this.launchPreferenceService.buildLaunchConfiguration(launchPropertiesMap,
				"user", "host", null, "4.0.3"); // "Weasis/4.0.3 (Windows 10)");

		// Test results
		assertEquals("4.0.3-MGR", map.get(PackageUtil.FREEMARKER_PACKAGE_VERSION).get(0));
		assertEquals("1", map.get(PackageUtil.FREEMARKER_PROPERTIES_PACKAGE_VERSION_ID).get(0));
		assertEquals("1", map.get(PackageUtil.FREEMARKER_PROPERTIES_LAUNCH_CONFIG_ID).get(0));
		assertEquals("1", map.get(PackageUtil.FREEMARKER_PROPERTIES_GROUP_ID).get(0));
	}

	@Test
	void when_buildLaunchConfiguration_with_launchConfigNotNullGroupsEmpty_should_useLaunchConfigIdDefaultTarget() {
		// Init data
		MultiValueMap<String, String> launchPropertiesMap = new LinkedMultiValueMap<>();
		launchPropertiesMap.add(PreferredType.QUALIFIER.getCode(), "-MGR");
		PackageVersionEntity packageVersionEntity = new PackageVersionEntity();
		packageVersionEntity.setVersionNumber("4.0.3");
		packageVersionEntity.setId(1L);
		packageVersionEntity.setQualifier("-MGR");
		packageVersionEntity.setDescription("description");

		LaunchConfigEntity launchConfigEntity = new LaunchConfigEntity();
		launchConfigEntity.setName("config");
		launchConfigEntity.setId(2L);

		LaunchConfigEntity defaultLaunchConfigEntity = new LaunchConfigEntity();
		defaultLaunchConfigEntity.setName("default");
		defaultLaunchConfigEntity.setId(1L);

		TargetEntity defaultTargetEntity = new TargetEntity();
		defaultTargetEntity.setId(1L);
		defaultTargetEntity.setType(TargetType.USER_GROUP);
		defaultTargetEntity.setName("default");

		// Mock
		when(this.packageServiceMock.retrieveAvailablePackageVersionToUse(anyString(), anyString()))
			.thenReturn(packageVersionEntity);
		when(this.launchConfigRepositoryMock.findByNameIgnoreCase(eq("default"))).thenReturn(defaultLaunchConfigEntity);
		when(this.launchConfigRepositoryMock.findByNameIgnoreCase(eq("config"))).thenReturn(launchConfigEntity);
		when(this.targetRepositoryMock.findByNameIgnoreCase(eq("default"))).thenReturn(defaultTargetEntity);

		// Call service
		MultiValueMap<String, String> map = this.launchPreferenceService.buildLaunchConfiguration(launchPropertiesMap,
				"user", "host", "config", "4.0.3"); // "Weasis/4.0.3 (Windows 10)");

		// Test results
		assertEquals("4.0.3-MGR", map.get(PackageUtil.FREEMARKER_PACKAGE_VERSION).get(0));
		assertEquals("1", map.get(PackageUtil.FREEMARKER_PROPERTIES_PACKAGE_VERSION_ID).get(0));
		assertEquals("2", map.get(PackageUtil.FREEMARKER_PROPERTIES_LAUNCH_CONFIG_ID).get(0));
		assertEquals("1", map.get(PackageUtil.FREEMARKER_PROPERTIES_GROUP_ID).get(0));
	}

	@Test
	void when_buildLaunchConfiguration_with_launchConfigNonNullGroupsNonEmpty_should_useLaunchConfigIdTargetId() {
		// Init data
		MultiValueMap<String, String> launchPropertiesMap = new LinkedMultiValueMap<>();
		launchPropertiesMap.add(PreferredType.QUALIFIER.getCode(), "-MGR");
		PackageVersionEntity packageVersionEntity = new PackageVersionEntity();
		packageVersionEntity.setVersionNumber("4.0.3");
		packageVersionEntity.setId(1L);
		packageVersionEntity.setQualifier("-MGR");
		packageVersionEntity.setDescription("description");

		LaunchConfigEntity launchConfigEntity = new LaunchConfigEntity();
		launchConfigEntity.setName("config");
		launchConfigEntity.setId(2L);

		LaunchConfigEntity defaultLaunchConfigEntity = new LaunchConfigEntity();
		defaultLaunchConfigEntity.setName("default");
		defaultLaunchConfigEntity.setId(1L);

		TargetEntity defaultTargetEntity = new TargetEntity();
		defaultTargetEntity.setId(1L);
		defaultTargetEntity.setType(TargetType.USER_GROUP);
		defaultTargetEntity.setName("default");

		TargetEntity targetEntity = new TargetEntity();
		targetEntity.setId(2L);
		targetEntity.setName("Target");
		targetEntity.setType(TargetType.USER_GROUP);

		// Mock
		when(this.packageServiceMock.retrieveAvailablePackageVersionToUse(anyString(), anyString()))
			.thenReturn(packageVersionEntity);
		when(this.launchConfigRepositoryMock.findByNameIgnoreCase(eq("default"))).thenReturn(defaultLaunchConfigEntity);
		when(this.launchConfigRepositoryMock.findByNameIgnoreCase(eq("config"))).thenReturn(launchConfigEntity);
		when(this.targetRepositoryMock.findByNameIgnoreCase(eq("default"))).thenReturn(defaultTargetEntity);
		when(this.targetRepositoryMock.findByNameIgnoreCaseAndType(eq("user"), eq(TargetType.USER)))
			.thenReturn(targetEntity);
		when(this.overrideConfigServiceMock.existOverrideConfigWithVersionConfigTarget(any(), any(), any()))
			.thenReturn(true);

		// Call service
		MultiValueMap<String, String> map = this.launchPreferenceService.buildLaunchConfiguration(launchPropertiesMap,
				"user", "host", "config", "4.0.3"); // "Weasis/4.0.3 (Windows 10)");

		// Test results
		assertEquals("4.0.3-MGR", map.get(PackageUtil.FREEMARKER_PACKAGE_VERSION).get(0));
		assertEquals("1", map.get(PackageUtil.FREEMARKER_PROPERTIES_PACKAGE_VERSION_ID).get(0));
		assertEquals("2", map.get(PackageUtil.FREEMARKER_PROPERTIES_LAUNCH_CONFIG_ID).get(0));
		assertEquals("2", map.get(PackageUtil.FREEMARKER_PROPERTIES_GROUP_ID).get(0));
	}

	/**
	 * Test method distinctByKey
	 * <p>
	 * Initial: List<LaunchEntity>: - LaunchEntity -> Prefered Name=xxx Value=1 -
	 * LaunchEntity -> Prefered Name=XXX Value=2 - LaunchEntity -> Prefered Name=yyy
	 * Value=3
	 * <p>
	 * Expected: List<LaunchEntity>: - LaunchEntity -> Prefered Name=xxx Value=1 -
	 * LaunchEntity -> Prefered Name=yyy Value=3
	 */
	@Test
	void shouldDistinctByKey() {
		// Init data
		List<LaunchEntity> launches = new LinkedList<>();

		// Create Launches
		LaunchEntity launchUser = LaunchRepositoryTest.buildLaunchEntity(1L, "TargetName", TargetType.USER, 1L,
				"LaunchConfigName", 1L, "xxx", PreferredType.EXT_CFG, "1");
		LaunchEntity launchHost = LaunchRepositoryTest.buildLaunchEntity(1L, "TargetName", TargetType.HOST, 1L,
				"LaunchConfigName", 1L, "XXX", PreferredType.EXT_CFG, "2");
		LaunchEntity launchHostGroup = LaunchRepositoryTest.buildLaunchEntity(1L, "TargetName", TargetType.HOST_GROUP,
				1L, "LaunchConfigName", 1L, "yyy", PreferredType.EXT_CFG, "3");

		// Add in the list
		launches.add(launchUser);
		launches.add(launchHost);
		launches.add(launchHostGroup);

		// Test initial
		assertEquals(3, launches.size());
		assertEquals("xxx", launches.get(0).getAssociatedPreferred().getName());
		assertEquals("XXX", launches.get(1).getAssociatedPreferred().getName());
		assertEquals("yyy", launches.get(2).getAssociatedPreferred().getName());

		// Call filter
		List<LaunchEntity> toTest = launches.stream()
			.filter(this.launchPreferenceService.distinctByKey(l -> l.getAssociatedPreferred().getName().toLowerCase()))
			.collect(Collectors.toList());

		// Test filter
		assertEquals(2, toTest.size());
		assertEquals("xxx", toTest.get(0).getAssociatedPreferred().getName());
		assertEquals("yyy", toTest.get(1).getAssociatedPreferred().getName());
	}

	/**
	 * Test method freeMarkerModelMapping
	 * <p>
	 * Initial: LaunchPropertiesMap: Key: Property Values {aaa+bbb}, {ccc ddd} Key:
	 * Argument Values {eee+fff}, {ggg hhh} Key: other Values {iii}, {jjj}
	 * <p>
	 * <p>
	 * Expected: Attributes have been added Key: Property Values {aaa bbb}, {ccc ddd} Key:
	 * Argument Values {eee fff}, {ggg hhh} Key: other Values {iii}
	 */
	@Test
	void shouldAddValuesInFreemarkerModel() {
		// Init data
		Model model = new ExtendedModelMap();
		MultiValueMap<String, String> launchPropertiesMap = new LinkedMultiValueMap<>();
		launchPropertiesMap.add(PreferredType.PROPERTY.getCode(), "aaa+bbb");
		launchPropertiesMap.add(PreferredType.PROPERTY.getCode(), "ccc ddd");
		launchPropertiesMap.add(PreferredType.ARGUMENT.getCode(), "eee+fff");
		launchPropertiesMap.add(PreferredType.ARGUMENT.getCode(), "ggg hhh");
		launchPropertiesMap.add(PreferredType.EXT_CFG.getCode(), "iii");
		launchPropertiesMap.add(PreferredType.EXT_CFG.getCode(), "jjj");

		// Call service
		this.launchPreferenceService.freeMarkerModelMapping(model, launchPropertiesMap);

		// Test model
		assertTrue(model.containsAttribute(PreferredType.PROPERTY.getCode()));
		assertTrue(model.containsAttribute(PreferredType.ARGUMENT.getCode()));
		assertTrue(model.containsAttribute(PreferredType.EXT_CFG.getCode()));
		assertEquals("aaa bbb",
				((Object[]) Objects.requireNonNull(model.getAttribute(PreferredType.PROPERTY.getCode())))[0]);
		assertEquals("ccc ddd",
				((Object[]) Objects.requireNonNull(model.getAttribute(PreferredType.PROPERTY.getCode())))[1]);
		assertEquals("eee fff",
				((Object[]) Objects.requireNonNull(model.getAttribute(PreferredType.ARGUMENT.getCode())))[0]);
		assertEquals("ggg hhh",
				((Object[]) Objects.requireNonNull(model.getAttribute(PreferredType.ARGUMENT.getCode())))[1]);
		assertEquals("iii", model.getAttribute(PreferredType.EXT_CFG.getCode()));
	}

	/**
	 * Test the method checkOnEmptyValues
	 * <p>
	 * Check if values found in DB should be kept in the freemarker mapping: Used to avoid
	 * for example: value= "http://example.org/weasis-" instead of value=
	 * "http://example.org/weasis"
	 * <p>
	 * Test: LaunchEntity Property Empty Values Expected: should return true Test:
	 * LaunchEntity Property No Empty Values Expected: should return true Test:
	 * LaunchEntity Other Empty Values Expected: should return false Test: LaunchEntity
	 * Other No Empty Values Expected: should return true
	 */
	@Test
	void checkOnEmptyValuesTest() {
		// Test: LaunchEntity Property Empty Values Expected: should return true
		LaunchEntity launchEntity = LaunchRepositoryTest.buildLaunchEntity(1L, "TargetName", TargetType.USER, 1L,
				"LaunchConfigName", 1L, "ext-config", PreferredType.PROPERTY, " ");
		assertTrue(this.launchPreferenceService.checkOnEmptyValues(launchEntity));

		// Test: LaunchEntity Property No Empty Values Expected: should return true
		launchEntity.setSelection("Test");
		assertTrue(this.launchPreferenceService.checkOnEmptyValues(launchEntity));

		// Test: LaunchEntity Other Empty Values Expected: should return false
		launchEntity.setSelection(" ");
		launchEntity.getAssociatedPreferred().setType(PreferredType.EXT_CFG.getCode());
		assertFalse(this.launchPreferenceService.checkOnEmptyValues(launchEntity));

		// Test: LaunchEntity Other No Empty Values Expected: should return true
		launchEntity.setSelection("Test");
		assertTrue(this.launchPreferenceService.checkOnEmptyValues(launchEntity));
	}

	/**
	 * Test method createLaunchConfigs
	 * <p>
	 * Expected: saveAll on repository has been called
	 */
	@Test
	void createLaunchConfigsTest() {

		// Init data
		LaunchConfigEntity launchConfigEntity = new LaunchConfigEntity();
		launchConfigEntity.setId(1L);
		launchConfigEntity.setName("LaunchConfigName");

		// Call service
		this.launchPreferenceService.createLaunchConfigs(Collections.singletonList(launchConfigEntity));

		// Test results
		Mockito.verify(this.launchConfigRepositoryMock, Mockito.times(1)).saveAll(any());
	}

	/**
	 * Test method createLaunchPrefered
	 * <p>
	 * Expected: saveAll on repository has been called
	 */
	@Test
	void createLaunchPreferedTest() {
		// Init data
		LaunchPreferredEntity launchPreferedEntity = new LaunchPreferredEntity();
		launchPreferedEntity.setId(1L);
		launchPreferedEntity.setName("LaunchConfigName");
		launchPreferedEntity.setType("Type");

		// Call service
		this.launchPreferenceService.createLaunchPrefered(Collections.singletonList(launchPreferedEntity));

		// Test results
		Mockito.verify(this.launchPreferedRepositoryMock, Mockito.times(1)).saveAll(any());
	}

	/**
	 * Test method checkParametersCreateLaunches
	 * <p>
	 * - Case there is an error in the request body or empty lists Expected: Bad request +
	 * ErrorMessage in the body with correct value
	 */
	@Test
	void checkParametersCreateLaunchesCaseWrongParametersTest() {
		// Init data
		List<LaunchEntity> launches = new ArrayList<>();

		// Call service and test results
		assertThrows(ParameterException.class,
				() -> this.launchPreferenceService.checkParametersLaunches(launches, "created"));
	}

	/**
	 * Test method checkParametersCreateLaunches
	 * <p>
	 * - Case config name does not exist in database Expected: Bad request + ErrorMessage
	 * in the body with correct value
	 */
	@Test
	void checkParametersCreateLaunchesCaseConfigNameDoesNotExistTest() {
		// Init data
		List<LaunchEntity> launches = new ArrayList<>();
		LaunchEntity launchUser = LaunchRepositoryTest.buildLaunchEntity(1L, "TargetName", TargetType.USER, 1L,
				"LaunchConfigName", 1L, "ext-config", PreferredType.EXT_CFG, "alternatecdb");
		launches.add(launchUser);

		// Mock repository
		when(this.launchConfigRepositoryMock.existsByName(anyString())).thenReturn(false);

		// Call service and test results
		assertThrows(ParameterException.class,
				() -> this.launchPreferenceService.checkParametersLaunches(launches, "created"));
	}

	/**
	 * Test method checkParametersCreateLaunches
	 * <p>
	 * - Case prefered name does not exist in database Expected: Parameter exception
	 * thrown
	 */
	@Test
	void checkParametersCreateLaunchesCasePreferedNameDoesNotExistTest() {
		// Init data
		List<LaunchEntity> launches = new ArrayList<>();
		LaunchEntity launchUser = LaunchRepositoryTest.buildLaunchEntity(1L, "TargetName", TargetType.USER, 1L,
				"LaunchConfigName", 1L, "ext-config", PreferredType.EXT_CFG, "alternatecdb");
		launches.add(launchUser);

		// Mock repository
		when(this.launchConfigRepositoryMock.existsByName(anyString())).thenReturn(true);
		when(this.launchPreferedRepositoryMock.existsByName(anyString())).thenReturn(false);

		// Call service and test results
		assertThrows(ParameterException.class,
				() -> this.launchPreferenceService.checkParametersLaunches(launches, "created"));
	}

	/**
	 * Test method checkParametersCreateLaunches
	 * <p>
	 * - Case target name does not exist in database Expected: Parameter exception
	 */
	@Test
	void checkParametersCreateLaunchesCaseTargetNameDoesNotExistTest() {
		// Init data
		List<LaunchEntity> launches = new ArrayList<>();
		LaunchEntity launchUser = LaunchRepositoryTest.buildLaunchEntity(1L, "TargetName", TargetType.USER, 1L,
				"LaunchConfigName", 1L, "ext-config", PreferredType.EXT_CFG, "alternatecdb");
		launches.add(launchUser);

		// Mock repository
		when(this.launchConfigRepositoryMock.existsByName(anyString())).thenReturn(true);
		when(this.launchPreferedRepositoryMock.existsByName(anyString())).thenReturn(true);
		when(this.targetRepositoryMock.existsByNameIgnoreCase(anyString())).thenReturn(false);

		// Call service and test results
		assertThrows(ParameterException.class,
				() -> this.launchPreferenceService.checkParametersLaunches(launches, "created"));
	}

	/**
	 * Test method checkParametersCreateLaunches
	 * <p>
	 * - Case no errors Expected: no exception thrown
	 */
	@Test
	void checkParametersCreateLaunchesCaseNoErrorTest() {
		// Init data
		List<LaunchEntity> launches = new ArrayList<>();
		LaunchEntity launchUser = LaunchRepositoryTest.buildLaunchEntity(1L, "TargetName", TargetType.USER, 1L,
				"LaunchConfigName", 1L, "ext-config", PreferredType.EXT_CFG, "alternatecdb");
		launches.add(launchUser);

		// Mock repository
		when(this.launchConfigRepositoryMock.existsByName(anyString())).thenReturn(true);
		when(this.launchPreferedRepositoryMock.existsByName(anyString())).thenReturn(true);
		when(this.targetRepositoryMock.existsByNameIgnoreCase(anyString())).thenReturn(true);

		// Call service and test results
		assertDoesNotThrow(() -> this.launchPreferenceService.checkParametersLaunches(launches, "created"));
	}

	/**
	 * Test method createLaunches
	 * <p>
	 * Expected: - saveAll has been called - entities are returned with correct values
	 */
	@Test
	void createLaunchesTest() {

		// Mock repositories
		LaunchEntity launch = LaunchRepositoryTest.buildLaunchEntity(1L, "TargetName", TargetType.USER, 1L,
				"LaunchConfigName", 1L, "LaunchPreferedName", PreferredType.EXT_CFG, "launchValue");
		LaunchPreferredEntity launchPreferedEntity = launch.getAssociatedPreferred();
		LaunchConfigEntity launchConfigEntity = launch.getAssociatedConfig();
		TargetEntity targetEntity = launch.getAssociatedTarget();
		when(this.launchPreferedRepositoryMock.findByNameIn(Mockito.anyList()))
			.thenReturn(Collections.singletonList(launchPreferedEntity));
		when(this.launchConfigRepositoryMock.findByNameIn(Mockito.anyList()))
			.thenReturn(Collections.singletonList(launchConfigEntity));
		when(this.targetRepositoryMock.findByNameIn(Mockito.anyList()))
			.thenReturn(Collections.singletonList(targetEntity));
		when(this.launchRepositoryMock.saveAll(any())).thenReturn(new ArrayList(List.of(launch)));

		// Call service
		List<LaunchEntity> launchesToTest = new ArrayList<>();
		LaunchEntity launchEntityToTest = new LaunchEntity();
		launchesToTest.add(launchEntityToTest);
		launchEntityToTest.setSelection("launchValue");
		LaunchPreferredEntity launchPreferedEntityToTest = new LaunchPreferredEntity();
		launchPreferedEntityToTest.setName("LaunchPreferedName");
		LaunchConfigEntity launchConfigEntityToTest = new LaunchConfigEntity();
		launchConfigEntityToTest.setName("LaunchConfigName");
		TargetEntity targetEntityToTest = new TargetEntity();
		targetEntityToTest.setName("TargetName");
		launchEntityToTest.setAssociatedConfig(launchConfigEntityToTest);
		launchEntityToTest.setAssociatedPreferred(launchPreferedEntityToTest);
		launchEntityToTest.setAssociatedTarget(targetEntityToTest);

		List<LaunchEntity> launches = this.launchPreferenceService.createLaunches(launchesToTest);

		// Test result
		Mockito.verify(this.launchRepositoryMock, Mockito.times(1)).saveAll(any());
		assertEquals(1, launches.size());
		assertEquals(Long.valueOf(1), launches.get(0).getLaunchEntityPK().getLaunchConfigId());
		assertEquals(Long.valueOf(1), launches.get(0).getLaunchEntityPK().getLaunchPreferredId());
		assertEquals(Long.valueOf(1), launches.get(0).getLaunchEntityPK().getTargetId());
		assertEquals("LaunchPreferedName", launches.get(0).getAssociatedPreferred().getName());
		assertEquals("LaunchConfigName", launches.get(0).getAssociatedConfig().getName());
		assertEquals("TARGETNAME", launches.get(0).getAssociatedTarget().getName());
		assertEquals("launchValue", launches.get(0).getSelection());
	}

	/**
	 * Test method checkParametersDeleteLaunches: Case no error
	 * <p>
	 * Expected: no exception thrown
	 */
	@Test
	void checkParametersDeleteLaunchesCaseNoErrorTest() {

		// Init data
		List<LaunchEntity> launches = new ArrayList<>();
		LaunchEntity launchUser = LaunchRepositoryTest.buildLaunchEntity(1L, "TargetName", TargetType.USER, 1L,
				"LaunchConfigName", 1L, "PreferedName", PreferredType.EXT_CFG, "LaunchValue");
		launches.add(launchUser);

		// Mock repository
		when(this.launchConfigRepositoryMock.existsByName(anyString())).thenReturn(true);
		when(this.launchPreferedRepositoryMock.existsByName(anyString())).thenReturn(true);
		when(this.targetRepositoryMock.existsByNameIgnoreCase(anyString())).thenReturn(true);
		when(this.launchRepositoryMock.existsById(any())).thenReturn(true);

		// Call service and test results
		assertDoesNotThrow(() -> this.launchPreferenceService.checkParametersDeleteLaunches(launches));
	}

	/**
	 * Test method checkParametersDeleteLaunches: Case some launch doesn't exist
	 * <p>
	 * Expected: ParameterException
	 */
	@Test
	void checkParametersDeleteLaunchesCaseSomeLaunchesDoesNotExistTest() {
		// Init data
		List<LaunchEntity> launches = new ArrayList<>();
		LaunchEntity launchUser = LaunchRepositoryTest.buildLaunchEntity(1L, "TargetName", TargetType.USER, 1L,
				"LaunchConfigName", 1L, "PreferedName", PreferredType.EXT_CFG, "LaunchValue");
		launches.add(launchUser);

		// Mock repository
		when(this.launchConfigRepositoryMock.existsByName(anyString())).thenReturn(true);
		when(this.launchPreferedRepositoryMock.existsByName(anyString())).thenReturn(true);
		when(this.targetRepositoryMock.existsByNameIgnoreCase(anyString())).thenReturn(true);
		when(this.launchRepositoryMock.existsById(any())).thenReturn(false);

		// Call service and test results
		assertThrows(ParameterException.class,
				() -> this.launchPreferenceService.checkParametersDeleteLaunches(launches));
	}

	/**
	 * Test method deleteLaunches
	 * <p>
	 * Expected: deleteAll has been called
	 */
	@Test
	void deleteLaunchesTest() {

		// Init data
		List<LaunchEntity> launches = new ArrayList<>();
		LaunchEntity launchUser = LaunchRepositoryTest.buildLaunchEntity(1L, "TargetName", TargetType.USER, 1L,
				"LaunchConfigName", 1L, "PreferedName", PreferredType.EXT_CFG, "LaunchValue");
		launches.add(launchUser);

		// call service
		this.launchPreferenceService.deleteLaunches(launches);

		// Test result
		Mockito.verify(this.launchRepositoryMock, Mockito.times(1)).deleteAll(any());
	}

	/**
	 * Test method hasLaunchWithTargetName:Case launch exist
	 * <p>
	 * Expected: return true
	 */
	@Test
	void hasLaunchWithTargetNameCaseLaunchFoundTest() {

		// Mock data
		when(this.launchRepositoryMock.existsByLaunchEntityPKTargetId(Mockito.anyLong())).thenReturn(true);

		// call service and test result
		assertTrue(this.launchPreferenceService.hasLaunchWithTargetName("targetName"));
	}

	/**
	 * Test method hasLaunchWithTargetName:Case launch does not exist
	 * <p>
	 * Expected: return false
	 */
	@Test
	void hasLaunchWithTargetNameCaseLaunchNotFoundTest() {

		// Mock data
		when(this.launchRepositoryMock.existsByLaunchEntityPKTargetId(Mockito.anyLong())).thenReturn(false);

		// call service and test result
		assertFalse(this.launchPreferenceService.hasLaunchWithTargetName("targetName"));
	}

	/**
	 * Test method checkParameterDeleteLaunchConfig: case wrong parameters
	 * <p>
	 * Expected: - Parameter exception
	 */
	@Test
	void checkParameterDeleteLaunchConfigCaseWrongParametersTest() {
		// Call service and test results
		assertThrows(ParameterException.class, () -> this.launchPreferenceService.checkParameterDeleteLaunchConfig(""));
	}

	/**
	 * Test method checkParameterDeleteLaunchConfig: case launch config not existing
	 * <p>
	 * Expected: - Parameter exception
	 */
	@Test
	void checkParameterDeleteLaunchConfigCaseLaunchConfigNotExistingTest() {
		// Mock data
		when(this.launchConfigRepositoryMock.existsByName(anyString())).thenReturn(false);

		// Call service and test results
		assertThrows(ParameterException.class,
				() -> this.launchPreferenceService.checkParameterDeleteLaunchConfig("launchConfigName"));
	}

	/**
	 * Test method checkParameterDeleteLaunchConfig: case a launch is associated to a
	 * launch config
	 * <p>
	 * Expected: - Constraint Exception
	 */
	@Test
	void checkParameterDeleteLaunchConfigCaseLaunchAssociatedTest() {
		// Mock data
		when(this.launchConfigRepositoryMock.existsByName(anyString())).thenReturn(true);
		when(this.launchRepositoryMock.existsByLaunchEntityPKLaunchConfigId(Mockito.anyLong())).thenReturn(true);

		// Call service and test results
		assertThrows(ConstraintException.class,
				() -> this.launchPreferenceService.checkParameterDeleteLaunchConfig("launchConfigName"));
	}

	/**
	 * Test method checkParameterDeleteLaunchConfig: case no error
	 * <p>
	 * Expected: - no exception thrown
	 */
	@Test
	void checkParameterDeleteLaunchConfigCaseNoErrorTest() {
		// Mock data
		when(this.launchConfigRepositoryMock.existsByName(anyString())).thenReturn(true);
		when(this.launchRepositoryMock.existsByLaunchEntityPKLaunchConfigId(Mockito.anyLong())).thenReturn(false);

		// Call service and test results
		assertDoesNotThrow(() -> this.launchPreferenceService.checkParameterDeleteLaunchConfig("launchConfigName"));
	}

	/**
	 * Test method deleteLaunchConfig
	 * <p>
	 * Expected: Delete has been called
	 */
	@Test
	void deleteLaunchConfigTest() {

		// call service
		this.launchPreferenceService.deleteLaunchConfig("launchConfigName");

		// Test result
		Mockito.verify(this.launchConfigRepositoryMock, Mockito.times(1)).delete(any());
	}

	/**
	 * Test method checkParameterDeleteLaunchPrefered: case wrong parameters
	 * <p>
	 * Expected: ParameterException thrown
	 */
	@Test
	void checkParameterDeleteLaunchPreferedCaseWrongParametersTest() {
		// Call service and test results
		assertThrows(ParameterException.class,
				() -> this.launchPreferenceService.checkParameterDeleteLaunchPrefered(""));
	}

	/**
	 * Test method checkParameterDeleteLaunchPrefered: case launch prefered not existing
	 * <p>
	 * Expected: ParameterException thrown
	 */
	@Test
	void checkParameterDeleteLaunchPreferedCaseLaunchPreferedNotExistingTest() {

		// Mock data
		when(this.launchPreferedRepositoryMock.existsByName(anyString())).thenReturn(false);

		// Call service and test results
		assertThrows(ParameterException.class,
				() -> this.launchPreferenceService.checkParameterDeleteLaunchPrefered("launchPrefered"));
	}

	/**
	 * Test method checkParameterDeleteLaunchPrefered: case a launch is associated to a
	 * launch config
	 * <p>
	 * Expected: -Constraint exception
	 */
	@Test
	void checkParameterDeleteLaunchPreferedCaseLaunchAssociatedTest() {
		// Mock data
		when(this.launchPreferedRepositoryMock.existsByName(anyString())).thenReturn(true);
		when(this.launchRepositoryMock.existsByLaunchEntityPKLaunchPreferredId(Mockito.anyLong())).thenReturn(true);

		// Call service and test results
		assertThrows(ConstraintException.class,
				() -> this.launchPreferenceService.checkParameterDeleteLaunchPrefered("launchPreferedName"));
	}

	/**
	 * Test method checkParameterDeleteLaunchPrefered: case no error
	 * <p>
	 * Expected: - no exception
	 */
	@Test
	void checkParameterDeleteLaunchPreferedCaseNoErrorTest() {
		// Mock data
		when(this.launchPreferedRepositoryMock.existsByName(anyString())).thenReturn(true);
		when(this.launchRepositoryMock.existsByLaunchEntityPKLaunchPreferredId(Mockito.anyLong())).thenReturn(false);

		// Call service and test results
		assertDoesNotThrow(() -> this.launchPreferenceService.checkParameterDeleteLaunchPrefered("launchPreferedName"));
	}

	/**
	 * Test method deleteLaunchPrefered
	 * <p>
	 * Expected: Delete has been called
	 */
	@Test
	void deleteLaunchPreferedTest() {

		// call service
		this.launchPreferenceService.deleteLaunchPrefered("launchConfigName");

		// Test result
		Mockito.verify(this.launchPreferedRepositoryMock, Mockito.times(1)).delete(any());
	}

	/**
	 * Test method retrieveGroupLaunches. Case config is null
	 * <p>
	 * Expected: - retrieve the mocked launch entities - config is null: findAll is called
	 */
	@Test
	void retrieveGroupLaunchesCaseConfigNullTest() {
		// Init data
		// Host group
		TargetEntity group = GroupRepositoryTest.buildTarget(true, 1L, "groupName", TargetType.HOST_GROUP);

		// call service
		List<LaunchEntity> launchEntities = this.launchPreferenceService.retrieveGroupLaunches(group, null);

		// Test result
		assertNotNull(launchEntities);
		assertEquals(1, launchEntities.size());
		Mockito.verify(this.launchConfigRepositoryMock, Mockito.times(1)).findAll();
		Mockito.verify(this.launchPreferedRepositoryMock, Mockito.times(1)).findAll();
		Mockito.verify(this.launchRepositoryMock, Mockito.times(1)).findAll(any(Specification.class));
	}

	/**
	 * Test method retrieveGroupLaunches. Case config is not null
	 * <p>
	 * Expected: - retrieve the mocked launch entities - config is not null: findByName is
	 * called
	 */
	@Test
	void retrieveGroupLaunchesCaseConfigNotNullTest() {
		// Init data
		// Host group
		TargetEntity group = GroupRepositoryTest.buildTarget(true, 1L, "groupName", TargetType.HOST_GROUP);

		// call service
		List<LaunchEntity> launchEntities = this.launchPreferenceService.retrieveGroupLaunches(group,
				"launchConfigName");

		// Test result
		assertNotNull(launchEntities);
		assertEquals(1, launchEntities.size());
		Mockito.verify(this.launchConfigRepositoryMock, Mockito.times(1)).findByName(anyString());
		Mockito.verify(this.launchPreferedRepositoryMock, Mockito.times(1)).findAll();
		Mockito.verify(this.launchRepositoryMock, Mockito.times(1)).findAll(any(Specification.class));
	}

	/**
	 * Test method retrieveLaunchPrefered. Case prefered type is null
	 * <p>
	 * Expected: - retrieve the mocked launch prefered entities - prefered type is null:
	 * findAll is called
	 */
	@Test
	void retrieveLaunchPreferedCasePreferedTypeNullTest() {
		// call service
		List<LaunchPreferredEntity> launchPreferedEntities = this.launchPreferenceService.retrieveLaunchPrefered(null);

		// Test result
		assertNotNull(launchPreferedEntities);
		assertEquals(1, launchPreferedEntities.size());
		Mockito.verify(this.launchPreferedRepositoryMock, Mockito.times(1)).findAll();
	}

	/**
	 * Test method retrieveLaunchPrefered. Case prefered type is not null
	 * <p>
	 * Expected: - retrieve the mocked launch prefered entities - prefered type is not
	 * null: findByType is called
	 */
	@Test
	void retrieveLaunchPreferedCasePreferedTypeNotNullTest() {
		// call service
		List<LaunchPreferredEntity> launchPreferedEntities = this.launchPreferenceService
			.retrieveLaunchPrefered("preferedType");

		// Test result
		assertNotNull(launchPreferedEntities);
		assertEquals(1, launchPreferedEntities.size());
		Mockito.verify(this.launchPreferedRepositoryMock, Mockito.times(1)).findByType(anyString());
	}

	/**
	 * Test method existLaunchPreferedPreferedType. Case prefered type not existing
	 * <p>
	 * Expected: - return false
	 */
	@Test
	void existLaunchPreferedPreferedTypeCaseNotExistingTest() {
		// Mock
		when(this.launchPreferedRepositoryMock.existsByType(anyString())).thenReturn(false);

		// call service
		boolean exist = this.launchPreferenceService.existLaunchPreferedPreferedType("preferedType");

		// Test result
		assertFalse(exist);
		Mockito.verify(this.launchPreferedRepositoryMock, Mockito.times(1)).existsByType(anyString());
	}

	/**
	 * Test method existLaunchPreferedPreferedType. Case prefered type is existing
	 * <p>
	 * Expected: - return true
	 */
	@Test
	void existLaunchPreferedPreferedTypeCaseIsExistingTest() {
		// Mock
		when(this.launchPreferedRepositoryMock.existsByType(anyString())).thenReturn(true);

		// call service
		boolean exist = this.launchPreferenceService.existLaunchPreferedPreferedType("preferedType");

		// Test result
		assertTrue(exist);
		Mockito.verify(this.launchPreferedRepositoryMock, Mockito.times(1)).existsByType(anyString());
	}

	/**
	 * Test method retrieveLaunchConfig.
	 * <p>
	 * Expected: - findAll of launchConfigRepository has been called
	 */
	@Test
	void retrieveLaunchConfigTest() {
		// call service
		List<LaunchConfigEntity> launchConfigEntities = this.launchPreferenceService.retrieveLaunchConfig();

		// Test result
		assertNotNull(launchConfigEntities);
		assertEquals(1, launchConfigEntities.size());
		Mockito.verify(this.launchConfigRepositoryMock, Mockito.times(1)).findAll();
	}

	/**
	 * Test method retrieveLaunchesById.
	 * <p>
	 * Expected: - findByLaunchEntityPKTargetId of launchRepository has been called
	 */
	@Test
	void retrieveLaunchesByIdTest() {

		TargetEntity targetEntity = new TargetEntity();
		targetEntity.setId(1L);

		// call service
		this.launchPreferenceService.retrieveLaunchesById(targetEntity);

		// Test result
		Mockito.verify(this.launchRepositoryMock, Mockito.times(1)).findByLaunchEntityPKTargetId(Mockito.anyLong());
	}

	/**
	 * Test method retrieveLaunchConfigsById.
	 * <p>
	 * Expected: - findAllById of launchConfigRepository has been called
	 */
	@Test
	void retrieveLaunchConfigsByIdTest() {
		// call service
		this.launchPreferenceService.retrieveLaunchConfigsById(Collections.singletonList(1L));

		// Test result
		Mockito.verify(this.launchConfigRepositoryMock, Mockito.times(1)).findAllById(Mockito.anyCollection());
	}

	/**
	 * Test method retrieveLaunchPreferedById.
	 * <p>
	 * Expected: - findAllById of launchPreferedRepository has been called
	 */
	@Test
	void retrieveLaunchPreferedByIdTest() {
		// call service
		this.launchPreferenceService.retrieveLaunchPreferedById(Collections.singletonList(1L));

		// Test result
		Mockito.verify(this.launchPreferedRepositoryMock, Mockito.times(1)).findAllById(Mockito.anyCollection());
	}

}
