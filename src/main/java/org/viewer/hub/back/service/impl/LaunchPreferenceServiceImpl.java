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

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.viewer.hub.back.controller.exception.ConstraintException;
import org.viewer.hub.back.controller.exception.ParameterException;
import org.viewer.hub.back.controller.exception.TechnicalException;
import org.viewer.hub.back.controller.exception.WeasisException;
import org.viewer.hub.back.entity.GroupEntity;
import org.viewer.hub.back.entity.LaunchConfigEntity;
import org.viewer.hub.back.entity.LaunchEntity;
import org.viewer.hub.back.entity.LaunchEntityPK;
import org.viewer.hub.back.entity.LaunchPreferredEntity;
import org.viewer.hub.back.entity.PackageVersionEntity;
import org.viewer.hub.back.entity.TargetEntity;
import org.viewer.hub.back.entity.comparator.LaunchByTargetOrderComparator;
import org.viewer.hub.back.entity.comparator.TargetOrderComparator;
import org.viewer.hub.back.enums.LaunchConfigType;
import org.viewer.hub.back.enums.PreferredType;
import org.viewer.hub.back.enums.TargetType;
import org.viewer.hub.back.enums.WeasisLevelMessageType;
import org.viewer.hub.back.model.weasis.WeasisMessage;
import org.viewer.hub.back.repository.LaunchConfigRepository;
import org.viewer.hub.back.repository.LaunchPreferredRepository;
import org.viewer.hub.back.repository.LaunchRepository;
import org.viewer.hub.back.repository.TargetRepository;
import org.viewer.hub.back.repository.specification.LaunchByTargetConfigPreferredSpecification;
import org.viewer.hub.back.service.GroupService;
import org.viewer.hub.back.service.LaunchPreferenceService;
import org.viewer.hub.back.service.OverrideConfigService;
import org.viewer.hub.back.service.PackageService;
import org.viewer.hub.back.util.JacksonUtil;
import org.viewer.hub.back.util.MultiValueMapUtil;
import org.viewer.hub.back.util.PackageUtil;
import org.viewer.hub.back.util.XmlUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * CRUD (Create, Read, Update and Delete) methods for Weasis Launch Preferences
 */
@Service
@Transactional
@Slf4j
public class LaunchPreferenceServiceImpl implements LaunchPreferenceService {

	private static final long serialVersionUID = 6169958674513752164L;

	// Repositories
	private final LaunchRepository launchRepository;

	private final LaunchConfigRepository launchConfigRepository;

	private final LaunchPreferredRepository launchPreferredRepository;

	private final TargetRepository targetRepository;

	private final OverrideConfigService overrideConfigService;

	// Services
	private final GroupService groupService;

	private final PackageService packageService;

	/**
	 * Autowired constructor with parameters
	 * @param launchRepository Launch Repository
	 * @param launchConfigRepository LaunchConfig Repository
	 * @param launchPreferredRepository LaunchPreferred Repository
	 * @param targetRepository Target Repository
	 */
	@Autowired
	public LaunchPreferenceServiceImpl(final LaunchRepository launchRepository,
			final LaunchConfigRepository launchConfigRepository,
			final LaunchPreferredRepository launchPreferredRepository, final TargetRepository targetRepository,
			final GroupService groupService, final PackageService packageService,
			final OverrideConfigService overrideConfigService) {
		this.launchRepository = launchRepository;
		this.launchConfigRepository = launchConfigRepository;
		this.launchPreferredRepository = launchPreferredRepository;
		this.targetRepository = targetRepository;
		this.groupService = groupService;
		this.packageService = packageService;
		this.overrideConfigService = overrideConfigService;
	}

	@Override
	public String retrieveLaunchValue(String targetName, String configName, String preferedName, boolean prettyPrint) {
		LOG.debug("retrieveLaunchValue");

		// Retrieve Launch Entity By Target/Config/Prefered names
		LaunchEntity launch = this.retrieveLaunchByTargetConfigPreferedNames(targetName, configName, preferedName);

		// Get the launch value
		String launchValue = launch != null ? launch.getSelection() : null;

		if (launchValue != null && prettyPrint) {
			launchValue = XmlUtil.prettyPrint(launchValue);
		}

		return launchValue;
	}

	@Override
	public MultiValueMap<String, String> buildLaunchConfiguration(MultiValueMap<String, String> launchPropertiesMap,
			String user, String host, String config, String version) {

		// Retrieve Launches in DB and sort them by order of targets
		List<LaunchEntity> launchesSorted = this.retrieveLaunchesByHostUserConfigPreferedTypeOrderByTargets(host, user,
				config, null);

		// Filter to have only one unique prefered name
		List<LaunchEntity> launchesSortedAndFiltered =
				// filter to have only one unique prefered name
				launchesSorted.stream()
					.filter(this.distinctByKey(l -> l.getAssociatedPreferred().getName().toLowerCase()))
					.toList();

		// Mapping in initial launch property map
		MultiValueMap<String, String> launchProperties = this.mapInInitialLaunchPropertiesMap(launchPropertiesMap,
				launchesSortedAndFiltered);

		// Determine weasis package version to use
		PackageVersionEntity packageVersionToUse = this.determineWeasisPackageVersion(version, launchProperties);

		// Determine the url which will be used to retrieve the configuration properties
		// for the package version/ launch config/group of the user/host requested
		this.determineConfigurationPropertiesUrl(user, host, config, launchProperties, packageVersionToUse);

		return launchProperties;
	}

	/**
	 * Determine the url which will be used to retrieve the configuration properties for
	 * the package version/ launch config/group of the user/host requested
	 * @param user User
	 * @param host Host
	 * @param config Config
	 * @param launchProperties Launch properties
	 * @param packageVersionToUse Package version
	 */
	private void determineConfigurationPropertiesUrl(String user, String host, String config,
			MultiValueMap<String, String> launchProperties, PackageVersionEntity packageVersionToUse) {
		// Get the config corresponding to the name requested
		LaunchConfigEntity launchConfigEntity = this.launchConfigRepository.findByNameIgnoreCase(config);

		// Retrieve the groups of the host/user and sort them by priority
		List<TargetEntity> targetsToLookFor = new ArrayList<>(this.retrieveTargetsToLookFor(host, user)
			.stream()
			.filter(g -> Objects.equals(g.getType(), TargetType.USER_GROUP)
					|| Objects.equals(g.getType(), TargetType.HOST_GROUP))
			.toList());
		targetsToLookFor.sort(new TargetOrderComparator());

		// Set in the variables of freemarker
		this.fillFreeMarkerMapForLaunchPackageGroupToUse(packageVersionToUse, launchConfigEntity, targetsToLookFor,
				launchProperties);
	}

	/**
	 * Fill in the MultiValueMap the package version, group and launch config id in order
	 * to use them in the freemarker template
	 * @param packageVersion Package version to evaluate
	 * @param launchConfig Launch config to evaluate
	 * @param groups Groups of the user/host
	 */
	public void fillFreeMarkerMapForLaunchPackageGroupToUse(PackageVersionEntity packageVersion,
			LaunchConfigEntity launchConfig, List<TargetEntity> groups,
			MultiValueMap<String, String> launchProperties) {
		// Package version never null otherwise should have thrown an exception before:
		// set directly the package version
		launchProperties.add(PackageUtil.FREEMARKER_PROPERTIES_PACKAGE_VERSION_ID, packageVersion.getId().toString());
		launchProperties.add(PackageUtil.PROPERTIES_PACKAGE_VERSION_NAME,
				packageVersion.getVersionNumber() + packageVersion.getQualifier());

		// I18n
		launchProperties.add(PackageUtil.FREEMARKER_PROPERTIES_I18N_VERSION, packageVersion.getI18nVersion());

		if (launchConfig == null) {
			this.fillFreeMarkerPropertiesDefaultLaunchConfig(launchProperties);
			this.fillFreeMarkerPropertiesDefaultTarget(launchProperties);
		}
		else if (groups.isEmpty()) {
			launchProperties.add(PackageUtil.FREEMARKER_PROPERTIES_LAUNCH_CONFIG_ID, launchConfig.getId().toString());
			launchProperties.add(PackageUtil.PROPERTIES_LAUNCH_CONFIG_NAME, launchConfig.getName());
			this.fillFreeMarkerPropertiesDefaultTarget(launchProperties);
		}
		else {
			TargetEntity groupToUse = groups.stream()
				.filter(group -> this.overrideConfigService.existOverrideConfigWithVersionConfigTarget(packageVersion,
						launchConfig, group))
				.findFirst()
				.orElse(this.retrieveDefaultTarget());
			launchProperties.add(PackageUtil.FREEMARKER_PROPERTIES_LAUNCH_CONFIG_ID, launchConfig.getId().toString());
			launchProperties.add(PackageUtil.PROPERTIES_LAUNCH_CONFIG_NAME, launchConfig.getName());
			launchProperties.add(PackageUtil.FREEMARKER_PROPERTIES_GROUP_ID, groupToUse.getId().toString());
			launchProperties.add(PackageUtil.PROPERTIES_GROUP_NAME, groupToUse.getName());
		}
	}

	private void fillFreeMarkerPropertiesDefaultLaunchConfig(MultiValueMap<String, String> launchProperties) {
		LaunchConfigEntity defaultLaunchConfigEntity = this.launchConfigRepository
			.findByNameIgnoreCase(LaunchConfigType.DEFAULT.getCode());
		if (defaultLaunchConfigEntity != null) {
			launchProperties.add(PackageUtil.FREEMARKER_PROPERTIES_LAUNCH_CONFIG_ID,
					defaultLaunchConfigEntity.getId().toString());
			launchProperties.add(PackageUtil.PROPERTIES_LAUNCH_CONFIG_NAME, defaultLaunchConfigEntity.getName());
		}
		else {
			throw new TechnicalException("Default launch config not configured in database");
		}
	}

	private void fillFreeMarkerPropertiesDefaultTarget(MultiValueMap<String, String> launchProperties) {
		launchProperties.add(PackageUtil.FREEMARKER_PROPERTIES_GROUP_ID,
				this.retrieveDefaultTarget().getId().toString());
		launchProperties.add(PackageUtil.PROPERTIES_GROUP_NAME, this.retrieveDefaultTarget().getName());
	}

	private TargetEntity retrieveDefaultTarget() {
		TargetEntity defaultTargetEntity = this.targetRepository.findByNameIgnoreCase(TargetType.DEFAULT.getCode());
		if (defaultTargetEntity == null) {
			throw new TechnicalException("Default target not configured in database");
		}
		return defaultTargetEntity;
	}

	@Override
	public boolean checkOnEmptyValues(LaunchEntity launchEntity) {
		return Objects.equals(launchEntity.getAssociatedPreferred().getType(), PreferredType.PROPERTY.getCode())
				|| Objects.equals(launchEntity.getAssociatedPreferred().getType(), PreferredType.ARGUMENT.getCode())
				|| !(launchEntity.getSelection().trim().isEmpty());
	}

	@Override
	public List<LaunchEntity> retrieveLaunchesByHostUserConfigPreferedTypeOrderByTargets(String host, String user,
			String configName, String preferedType) {
		// Retrieve targets to look for: current target + groups of the target
		List<TargetEntity> targetsToLookFor = this.retrieveTargetsToLookFor(host, user);

		// Get the config corresponding to the name requested
		LaunchConfigEntity launchConfigEntity = this.launchConfigRepository.findByName(configName);

		// Get the prefered corresponding to the type requested: null = all, preferedType
		// requested otherwise
		List<LaunchPreferredEntity> launchPreferedEntities = preferedType != null
				? this.launchPreferredRepository.findByType(preferedType) : this.launchPreferredRepository.findAll();

		// Create the specification to query the launch table
		Specification<LaunchEntity> launchSpecification = new LaunchByTargetConfigPreferredSpecification(
				targetsToLookFor, Collections.singletonList(launchConfigEntity), launchPreferedEntities);

		// Apply the specification to retrieve results
		List<LaunchEntity> launches = this.launchRepository.findAll(Specification.where(launchSpecification));

		// Fill the launches' associated entities
		this.fillAssociatedEntitiesLaunches(targetsToLookFor, launchPreferedEntities,
				Collections.singletonList(launchConfigEntity), launches);

		// Sort launches by targets' order in order to retrieve the highest priority group
		launches.sort(new LaunchByTargetOrderComparator());

		// Return the launches found
		return launches;
	}

	@NotNull
	private List<TargetEntity> retrieveTargetsToLookFor(String host, String user) {
		List<TargetEntity> targetsToLookFor = new ArrayList<>();

		// Find targets corresponding to the groups of the host
		if (host != null && !host.isEmpty()) {
			this.fillTargetsToLookFor(host, TargetType.HOST, targetsToLookFor);
		}
		// Find targets corresponding to the groups of the user
		if (user != null && !user.isEmpty()) {
			this.fillTargetsToLookFor(user, TargetType.USER, targetsToLookFor);
		}
		return targetsToLookFor;
	}

	@Override
	public void fillAssociatedEntitiesLaunches(@Valid List<TargetEntity> targets,
			@Valid List<LaunchPreferredEntity> launchPrefered, @Valid List<LaunchConfigEntity> launchConfigs,
			List<LaunchEntity> launches) {

		Predicate<LaunchEntity> fillAssociatedEntitiesNotValid = launch -> {

			// Targets
			Optional<TargetEntity> target = targets.stream()
				.filter(Objects::nonNull)
				.filter(targetEntity -> Objects.equals(launch.getLaunchEntityPK().getTargetId(), targetEntity.getId()))
				.findAny();
			if (!target.isPresent()) {
				return true;
			}
			launch.setAssociatedTarget(target.get());

			// Prefered
			Optional<LaunchPreferredEntity> prefered = launchPrefered.stream()
				.filter(Objects::nonNull)
				.filter(preferedEntity -> Objects.equals(launch.getLaunchEntityPK().getLaunchPreferredId(),
						preferedEntity.getId()))
				.findAny();
			if (!prefered.isPresent()) {
				return true;
			}
			launch.setAssociatedPreferred(prefered.get());

			// Config
			Optional<LaunchConfigEntity> config = launchConfigs.stream()
				.filter(Objects::nonNull)
				.filter(launchConfigEntity -> Objects.equals(launch.getLaunchEntityPK().getLaunchConfigId(),
						launchConfigEntity.getId()))
				.findAny();
			if (!config.isPresent()) {
				return true;
			}
			launch.setAssociatedConfig(config.get());

			return false;
		};

		launches.removeIf(fillAssociatedEntitiesNotValid);
	}

	/**
	 * Determine version to use
	 * @param version Weasis version
	 * @param launchPropertiesMap Launch Properties associated to the user/host
	 */
	private PackageVersionEntity determineWeasisPackageVersion(String version,
			MultiValueMap<String, String> launchPropertiesMap) {

		PackageVersionEntity packageVersionEntityToReturn;

		// Retrieve qualifier if defined in launch configuration
		String qualifier = launchPropertiesMap.get(PreferredType.QUALIFIER.getCode()) != null
				? launchPropertiesMap.get(PreferredType.QUALIFIER.getCode()).stream().findFirst().orElse(null) : null;

		// Retrieve the package version
		packageVersionEntityToReturn = this.packageService.retrieveAvailablePackageVersionToUse(version, qualifier);

		// Throw an exception if packageVersion not found
		if (packageVersionEntityToReturn == null) {
			throw new WeasisException(JacksonUtil.serializeIntoJson(new WeasisMessage("Package version",
					"Version not installed on server", WeasisLevelMessageType.WARN)));
		}

		// Set in the variables of freemarker
		launchPropertiesMap
			.add(PackageUtil.FREEMARKER_PACKAGE_VERSION, packageVersionEntityToReturn.getQualifier() == null
					? packageVersionEntityToReturn.getVersionNumber()
					: packageVersionEntityToReturn.getVersionNumber() + packageVersionEntityToReturn.getQualifier());

		return packageVersionEntityToReturn;
	}

	/**
	 * Map in the initial launch properties map, the values sorted, filtered from DB
	 * @param launchPropertiesMap Initial Launch Properties Map
	 * @param launchesSortedAndFiltered Values from DB filtered and sorted
	 * @return The launchPropertiesMap with combined data
	 */
	private MultiValueMap<String, String> mapInInitialLaunchPropertiesMap(
			MultiValueMap<String, String> launchPropertiesMap, List<LaunchEntity> launchesSortedAndFiltered) {

		if (!launchesSortedAndFiltered.isEmpty()) {
			launchesSortedAndFiltered.stream()
				// filter no empty values for launch whose prefered type are not
				// property nor argument:
				// Used to avoid for example: value=
				// "http://example.org/weasis-"
				// instead of value= "http://example.org/weasis"
				.filter(this::checkOnEmptyValues)
				.forEach(launchEntity -> {
					if (Objects.equals(launchEntity.getAssociatedPreferred().getType(),
							PreferredType.PROPERTY.getCode())
							|| Objects.equals(launchEntity.getAssociatedPreferred().getType(),
									PreferredType.ARGUMENT.getCode())) {
						launchPropertiesMap.add(launchEntity.getAssociatedPreferred().getType(),
								launchEntity.getSelection());
					}
					else {
						MultiValueMapUtil.multiValueMapAddFirst(launchPropertiesMap,
								launchEntity.getAssociatedPreferred().getType(), launchEntity.getSelection());
					}
				});
		}
		return launchPropertiesMap;
	}

	/**
	 * Fill the ids for launches from entities
	 * @param targets Targets to compare
	 * @param launchPrefered Launch Prefered to compare
	 * @param launchConfigs Launch Configs to compare
	 * @param launches Launches to fill
	 */
	private void fillLaunchIdsFromAssociatedNames(List<TargetEntity> targets,
			List<LaunchPreferredEntity> launchPrefered, List<LaunchConfigEntity> launchConfigs,
			List<LaunchEntity> launches) {

		// Create LaunchEntityPK
		launches.forEach(l -> l.setLaunchEntityPK(new LaunchEntityPK()));

		// Targets
		launches.forEach(l -> targets.stream()
			.filter(targetEntity -> Objects.equals(l.getAssociatedTarget().getName(), targetEntity.getName()))
			.forEach(t -> l.getLaunchEntityPK().setTargetId(t.getId())));

		// Prefered
		launches.forEach(l -> launchPrefered.stream()
			.filter(preferedEntity -> Objects.equals(l.getAssociatedPreferred().getName(), preferedEntity.getName()))
			.forEach(p -> l.getLaunchEntityPK().setLaunchPreferredId(p.getId())));

		// Config
		launches.forEach(l -> launchConfigs.stream()
			.filter(launchConfigEntity -> Objects.equals(l.getAssociatedConfig().getName(),
					launchConfigEntity.getName()))
			.forEach(c -> l.getLaunchEntityPK().setLaunchConfigId(c.getId())));

	}

	/**
	 * Retrieve targets to look for (target + groups of the target) depending on the name
	 * target in parameter
	 * @param targetName Name of the target
	 * @param targetType Target Type
	 * @param targetsToLookFor List of targets to fill
	 */
	private void fillTargetsToLookFor(String targetName, TargetType targetType, List<TargetEntity> targetsToLookFor) {
		TargetEntity target = this.targetRepository.findByNameIgnoreCaseAndType(targetName, targetType);
		// If target exist in database
		if (target != null) {
			// Add the target to look for
			targetsToLookFor.add(target);

			List<GroupEntity> groups = this.groupService.retrieveGroupsByMember(target);
			if (!groups.isEmpty()) {
				List<TargetEntity> groupsTargets = groups.stream()
					.map(g -> this.targetRepository.findById(g.getGroupEntityPK().getGroupId()))
					.filter(Optional::isPresent)
					.map(Optional::get)
					.toList();
				// Add the groups corresponding to the target
				targetsToLookFor.addAll(groupsTargets);
			}
		}
	}

	@Override
	public <T> Predicate<T> distinctByKey(Function<? super T, ?> key) {
		Set<Object> seen = ConcurrentHashMap.newKeySet();
		return t -> seen.add(key.apply(t));
	}

	@Override
	public void freeMarkerModelMapping(Model model, MultiValueMap<String, String> launchPropertiesMap) {
		// Map the key/values to the freemarker model
		launchPropertiesMap.forEach((key, values) -> {
			if (Objects.equals(PreferredType.PROPERTY.getCode(), key)
					|| Objects.equals(PreferredType.ARGUMENT.getCode(), key)) {
				// replace property or argument with + by space in order for the
				// freemarker to separate name/value
				// weasis.export.dicom+false => ex: <property name="weasis.export.dicom"
				// value="false"/>
				values.replaceAll(v -> v.replace("+", " "));
				model.addAttribute(key, values.toArray());
			}
			else {
				model.addAttribute(key, values.get(0));
			}
		});
	}

	@Override
	public List<LaunchConfigEntity> createLaunchConfigs(@Valid List<LaunchConfigEntity> configs) {
		LOG.debug("createLaunchConfigs");

		// Save the launch config provided
		return this.launchConfigRepository.saveAll(configs);
	}

	@Override
	public List<LaunchPreferredEntity> createLaunchPrefered(@Valid List<LaunchPreferredEntity> launchPrefered) {
		LOG.debug("createLaunchPrefered");
		// Save the launch prefered provided
		return this.launchPreferredRepository.saveAll(launchPrefered);
	}

	@Override
	public void checkParametersLaunches(List<LaunchEntity> launches, String message) {
		if (launches.isEmpty() || launches.stream()
			.anyMatch(l -> l.getAssociatedConfig() == null || l.getAssociatedConfig().getName() == null
					|| l.getAssociatedPreferred() == null || l.getAssociatedPreferred().getName() == null
					|| l.getAssociatedTarget() == null || l.getAssociatedTarget().getName() == null)) {
			// case there is an error in the request body or empty lists
			throw new ParameterException("Launches not %s: wrong parameters".formatted(message));
		}
		else if (launches.stream()
			.anyMatch(l -> !this.launchConfigRepository.existsByName(l.getAssociatedConfig().getName()))) {
			// case config name does not exist in database
			throw new ParameterException("Launches not %s: one of the config does not exist".formatted(message));
		}
		else if (launches.stream()
			.anyMatch(l -> !this.launchPreferredRepository.existsByName(l.getAssociatedPreferred().getName()))) {
			// case prefered name does not exist in database
			throw new ParameterException("Launches not %s: one of the prefered does not exist".formatted(message));
		}
		else if (launches.stream()
			.anyMatch(l -> !this.targetRepository.existsByNameIgnoreCase(l.getAssociatedTarget().getName()))) {
			// case target name does not exist in database
			throw new ParameterException("Launches not %s: one of the target does not exist".formatted(message));
		}
	}

	@Override
	public List<LaunchEntity> createLaunches(List<LaunchEntity> launches) {
		LOG.debug("LaunchPreferenceService -> createLaunches");

		// Retrieve the entities config/prefered/target
		List<LaunchConfigEntity> launchConfigEntities = this.launchConfigRepository
			.findByNameIn(launches.stream().map(l -> l.getAssociatedConfig().getName()).collect(Collectors.toList()));
		List<LaunchPreferredEntity> launchPreferedEntities = this.launchPreferredRepository.findByNameIn(
				launches.stream().map(l -> l.getAssociatedPreferred().getName()).collect(Collectors.toList()));
		List<TargetEntity> targetEntities = this.targetRepository
			.findByNameIn(launches.stream().map(l -> l.getAssociatedTarget().getName()).collect(Collectors.toList()));

		// Fill the primary key ids
		this.fillLaunchIdsFromAssociatedNames(targetEntities, launchPreferedEntities, launchConfigEntities, launches);

		// Save entities in database
		List<LaunchEntity> launchEntitiesCreated = this.launchRepository.saveAll(launches);

		// Fill the associated entities
		this.fillAssociatedEntitiesLaunches(targetEntities, launchPreferedEntities, launchConfigEntities,
				launchEntitiesCreated);

		return launchEntitiesCreated;
	}

	@Override
	public void checkParametersDeleteLaunches(List<LaunchEntity> launches) {
		// Check no issues in inputs
		this.checkParametersLaunches(launches, "deleted");

		// Retrieve the entities config/prefered/target
		List<LaunchConfigEntity> launchConfigEntities = this.launchConfigRepository
			.findByNameIn(launches.stream().map(l -> l.getAssociatedConfig().getName()).collect(Collectors.toList()));
		List<LaunchPreferredEntity> launchPreferedEntities = this.launchPreferredRepository.findByNameIn(
				launches.stream().map(l -> l.getAssociatedPreferred().getName()).collect(Collectors.toList()));
		List<TargetEntity> targetEntities = this.targetRepository
			.findByNameIn(launches.stream().map(l -> l.getAssociatedTarget().getName()).collect(Collectors.toList()));
		// Fill the primary key ids
		this.fillLaunchIdsFromAssociatedNames(targetEntities, launchPreferedEntities, launchConfigEntities, launches);
		// Check that all launches exist
		if (!launches.stream().allMatch(l -> this.launchRepository.existsById(l.getLaunchEntityPK()))) {
			// Case one of the launch does not exist
			throw new ParameterException("Launches not deleted: one of the launch does not exist");
		}
	}

	@Override
	public String deleteLaunches(List<LaunchEntity> launches) {
		LOG.debug("deleteLaunches");

		// Delete entities in database
		this.launchRepository.deleteAll(launches);

		return "deleted";
	}

	@Override
	public boolean hasLaunchWithTargetName(String targetName) {
		boolean hasLaunchWithTargetName = false;

		// retrieve the target
		TargetEntity targetFound = this.targetRepository.findByNameIgnoreCase(targetName);

		// Check if target is associated to a launch
		if (targetFound != null) {
			hasLaunchWithTargetName = this.launchRepository.existsByLaunchEntityPKTargetId(targetFound.getId());
		}

		return hasLaunchWithTargetName;
	}

	@Override
	public void checkParameterDeleteLaunchConfig(String launchConfigName) {
		if (launchConfigName == null || launchConfigName.trim().isEmpty()) {
			// case there is an error in the name of the launch config: empty or not
			// filled
			throw new ParameterException("Delete not done: wrong parameters");
		}
		else if (!this.launchConfigRepository.existsByName(launchConfigName)) {
			// case launch config not existing
			throw new ParameterException("Delete not done: launch config not existing");
		}
		// Check launch config not associated to a launch entity
		else {
			// retrieve the launch config
			LaunchConfigEntity launchConfigFound = this.launchConfigRepository.findByName(launchConfigName);

			// Check if launch config is associated to a launch
			if (this.launchRepository.existsByLaunchEntityPKLaunchConfigId(launchConfigFound.getId())) {
				// case there is a launch associated to the launch config:
				throw new ConstraintException(
						"Delete not done: a launch is associated to the launch config. Please remove launch before deleting launch config.");
			}
		}
	}

	@Override
	public void deleteLaunchConfig(String launchConfigName) {
		LOG.debug("deleteLaunchConfig");

		// Retrieve the launch config
		LaunchConfigEntity launchConfig = this.launchConfigRepository.findByName(launchConfigName);

		// Delete launch config
		this.launchConfigRepository.delete(launchConfig);
	}

	@Override
	public void checkParameterDeleteLaunchPrefered(String launchPreferedName) {
		if (launchPreferedName == null || launchPreferedName.trim().isEmpty()) {
			// case there is an error in the name of the launch prefered: empty or not
			// filled
			throw new ParameterException("Delete not done: wrong parameters");
		}
		else if (!this.launchPreferredRepository.existsByName(launchPreferedName)) {
			// case launch prefered not existing
			throw new ParameterException("Delete not done: launch prefered not existing");
		}
		// Check launch prefered not associated to a launch entity
		else {
			// retrieve the launch prefered
			LaunchPreferredEntity launchPreferedFound = this.launchPreferredRepository.findByName(launchPreferedName);

			// Check if launch prefered is associated to a launch
			if (this.launchRepository.existsByLaunchEntityPKLaunchPreferredId(launchPreferedFound.getId())) {
				// case there is a launch associated to the launch prefered:
				throw new ConstraintException(
						"Delete not done: a launch is associated to the launch prefered. Please remove launch before deleting launch prefered.");
			}
		}
	}

	@Override
	public void deleteLaunchPrefered(String launchPreferedName) {
		LOG.debug("deleteLaunchPrefered");

		// Retrieve the launch prefered
		LaunchPreferredEntity launchPrefered = this.launchPreferredRepository.findByName(launchPreferedName);

		// Delete launch Prefered
		this.launchPreferredRepository.delete(launchPrefered);
	}

	@Override
	public List<LaunchEntity> retrieveGroupLaunches(@Valid TargetEntity group, String launchConfigName) {

		// Get the config corresponding to the name requested or if launch config name is
		// null retrieve all the launch configs
		List<LaunchConfigEntity> launchConfigEntities = launchConfigName == null ? this.launchConfigRepository.findAll()
				: Collections.singletonList(this.launchConfigRepository.findByName(launchConfigName));

		// Get all the Launch Prefered
		List<LaunchPreferredEntity> launchPreferedEntities = this.launchPreferredRepository.findAll();

		// Create the specification to query the launch table
		Specification<LaunchEntity> launchSpecification = new LaunchByTargetConfigPreferredSpecification(
				Collections.singletonList(group), launchConfigEntities, launchPreferedEntities);

		// Apply the specification to retrieve results
		List<LaunchEntity> launches = this.launchRepository.findAll(Specification.where(launchSpecification));

		// Fill the launches' associated entities
		this.fillAssociatedEntitiesLaunches(Collections.singletonList(group), launchPreferedEntities,
				launchConfigEntities, launches);

		// Sort launches by targets' order
		launches.sort(new LaunchByTargetOrderComparator());

		// Return the launches found
		return launches;
	}

	@Override
	public List<LaunchPreferredEntity> retrieveLaunchPrefered(String preferedType) {
		LOG.debug("retrieveLaunchPrefered");
		// Retrieve all the launch prefered
		return preferedType == null ? this.launchPreferredRepository.findAll()
				: this.launchPreferredRepository.findByType(preferedType);
	}

	@Override
	public boolean existLaunchPreferedPreferedType(String preferedType) {
		LOG.debug("existLaunchPreferedPreferedType");
		return this.launchPreferredRepository.existsByType(preferedType);
	}

	@Override
	public List<LaunchConfigEntity> retrieveLaunchConfig() {
		LOG.debug("retrieveLaunchConfig");
		// Retrieve all the launch config
		return this.launchConfigRepository.findAll();
	}

	@Override
	public List<LaunchEntity> retrieveLaunchesById(@Valid TargetEntity targetEntity) {
		return this.launchRepository.findByLaunchEntityPKTargetId(targetEntity.getId());
	}

	@Override
	public List<LaunchConfigEntity> retrieveLaunchConfigsById(List<Long> idsLaunchConfig) {
		return this.launchConfigRepository.findAllById(idsLaunchConfig);
	}

	@Override
	public List<LaunchPreferredEntity> retrieveLaunchPreferedById(List<Long> idsLaunchPrefered) {
		return this.launchPreferredRepository.findAllById(idsLaunchPrefered);
	}

	/**
	 * Retrieve Launch By Target/Config/Prefered names
	 * @param targetName Target Name
	 * @param configName Config Name
	 * @param preferedName Prefered Name
	 * @return LaunchEntity found
	 */
	private LaunchEntity retrieveLaunchByTargetConfigPreferedNames(String targetName, String configName,
			String preferedName) {
		// Get the target corresponding to the name requested
		TargetEntity targetEntity = this.targetRepository.findByNameIgnoreCase(targetName);
		// Get the config corresponding to the name requested
		LaunchConfigEntity launchConfigEntity = this.launchConfigRepository.findByName(configName);
		// Get the prefered corresponding to the name requested
		LaunchPreferredEntity launchPreferedEntity = this.launchPreferredRepository.findByName(preferedName);

		// Create the specification to query the launch table
		Specification<LaunchEntity> launchSpecification = new LaunchByTargetConfigPreferredSpecification(
				Collections.singletonList(targetEntity), Collections.singletonList(launchConfigEntity),
				Collections.singletonList(launchPreferedEntity));

		// Apply the specification to retrieve results
		Optional<LaunchEntity> oLaunch = this.launchRepository.findOne(Specification.where(launchSpecification));

		// Return the entity found
		return oLaunch.orElse(null);
	}

}
