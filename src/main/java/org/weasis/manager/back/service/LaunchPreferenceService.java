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

package org.weasis.manager.back.service;

import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.weasis.manager.back.entity.LaunchConfigEntity;
import org.weasis.manager.back.entity.LaunchEntity;
import org.weasis.manager.back.entity.LaunchPreferredEntity;
import org.weasis.manager.back.entity.TargetEntity;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public interface LaunchPreferenceService {

	/**
	 * Retrieve the launch value depending on target name, config name, prefered name
	 * @param targetName Target Name
	 * @param configName Config Name
	 * @param preferedName Prefered Name
	 * @param prettyPrint Pretty Print flag
	 * @return the launch value depending on target name, config name, prefered name
	 */
	String retrieveLaunchValue(String targetName, String configName, String preferedName, boolean prettyPrint);

	/**
	 * Build Launch Configuration
	 * @param launchPropertiesMap Initial launch map to fill
	 * @param user User
	 * @param host Host
	 * @param config Configuration selected
	 * @param version Weasis version
	 * @return MultiValueMap<String, String> corresponding to the map filled with DB
	 * values(sorted and filtered)
	 */
	MultiValueMap<String, String> buildLaunchConfiguration(MultiValueMap<String, String> launchPropertiesMap,
			String user, String host, String config, String version);

	/**
	 * Filter empty values for Prefered not argument nor property
	 * @param launchEntity Launch Entity to evaluate
	 * @return false if empty value for launch not argument nor property, true otherwise
	 */
	boolean checkOnEmptyValues(LaunchEntity launchEntity);

	/**
	 * Retrieve launches by host/user/config/preferedType ordered by Targets
	 * @param host Host
	 * @param user User
	 * @param configName Config name
	 * @param preferedType Prefered type: if null => all prefered type, specific prefered
	 * type otherwise
	 * @return List of launch entities ordered (in order to retrieve the highest priority
	 * group)
	 */
	List<LaunchEntity> retrieveLaunchesByHostUserConfigPreferedTypeOrderByTargets(String host, String user,
			String configName, String preferedType);

	/**
	 * Fill the launches' associated entities
	 * @param targets Targets to fill
	 * @param launchPrefered Launch Prefered to fill
	 * @param launchConfigs Launch Configs to fill
	 * @param launches Launches filled
	 */
	void fillAssociatedEntitiesLaunches(List<TargetEntity> targets, List<LaunchPreferredEntity> launchPrefered,
			List<LaunchConfigEntity> launchConfigs, List<LaunchEntity> launches);

	/**
	 * Used to keep only one Object depending on the first distinct key in parameter of a
	 * List of objects
	 * @param key Key
	 * @return Predicate which allow to filter the list
	 */
	<T> Predicate<T> distinctByKey(Function<? super T, ?> key);

	/**
	 * Map the key/values to the freemarker model
	 * @param model Model to map
	 * @param launchPropertiesMap Key/Values to add to the model
	 */
	void freeMarkerModelMapping(Model model, MultiValueMap<String, String> launchPropertiesMap);

	/**
	 * Save the launch config provided and return the created launch configs
	 * @param configs Launch Config to create
	 * @return Created launch config
	 */
	List<LaunchConfigEntity> createLaunchConfigs(List<LaunchConfigEntity> configs);

	/**
	 * Save the launch prefered provided and return the created launch prefered
	 * @param launchPrefered Launch Prefered to create
	 * @return Created launch prefered
	 */
	List<LaunchPreferredEntity> createLaunchPrefered(List<LaunchPreferredEntity> launchPrefered);

	/**
	 * Check potential errors in the input of the request for launches - Check right
	 * deserialization - Check all names filled - Check config exists - Check prefered
	 * exists - Check target exists
	 * @param launches Launches to check
	 * @return if errors throw Parameter exception
	 */
	void checkParametersLaunches(List<LaunchEntity> launches, String message);

	/**
	 * Create launches in database from Jackson deserialization inputs
	 * @param launches Deserialized jackson input
	 * @return Create launches
	 */
	List<LaunchEntity> createLaunches(List<LaunchEntity> launches);

	/**
	 * Check potential errors in the input of the request for delete launches
	 * @param launches Launches to check
	 * @return if errors throw Parameter exception
	 */
	void checkParametersDeleteLaunches(List<LaunchEntity> launches);

	/**
	 * Delete launches
	 * @param launches Launches to delete
	 * @return deleted if ok
	 */
	String deleteLaunches(List<LaunchEntity> launches);

	/**
	 * Check if a launch is associated to the target in parameter
	 * @param targetName Target Name
	 * @return true if a launch is associated to the target
	 */
	boolean hasLaunchWithTargetName(String targetName);

	/**
	 * Check parameter for delete launch config - check name not empty - check selected
	 * launch config exist - check there is no launch associated to this launch config
	 * @param launchConfigName Launch Config name to check
	 * @return if error exception is thrown
	 */
	void checkParameterDeleteLaunchConfig(String launchConfigName);

	/**
	 * Delete launch config for the name in parameter
	 * @param launchConfigName Launch Config name
	 */
	void deleteLaunchConfig(String launchConfigName);

	/**
	 * Check parameter for delete launch prefered - check name not empty - check selected
	 * launch prefered exist - check there is no launch associated to this launch prefered
	 * @param launchPreferedName Launch Prefered name to check
	 * @return if errors throw exception
	 */
	void checkParameterDeleteLaunchPrefered(String launchPreferedName);

	/**
	 * Delete launch prefered for the name in parameter
	 * @param launchPreferedName Launch Prefered name
	 */
	void deleteLaunchPrefered(String launchPreferedName);

	/**
	 * Retrieve launches for the group in parameter
	 * @param group Group
	 * @param launchConfigName Launch Config Name
	 * @return Launches found
	 */
	List<LaunchEntity> retrieveGroupLaunches(TargetEntity group, String launchConfigName);

	/**
	 * Retrieve launch prefered depending on prefered type (if null retrieve all the
	 * launch prefered)
	 * @param preferedType Prefered type
	 * @return launch prefered found
	 */
	List<LaunchPreferredEntity> retrieveLaunchPrefered(String preferedType);

	/**
	 * Check if the prefered type exists in the launch prefered table
	 * @param preferedType Prefered type to look for
	 * @return true if the prefered type exists in the launch prefered table
	 */
	boolean existLaunchPreferedPreferedType(String preferedType);

	/**
	 * Retrieve all launch config
	 * @return launch config found
	 */
	List<LaunchConfigEntity> retrieveLaunchConfig();

	/**
	 * Retrieve launches by target id
	 * @param targetEntity Target to look for
	 * @return Launches found
	 */
	List<LaunchEntity> retrieveLaunchesById(TargetEntity targetEntity);

	/**
	 * Retrieve LaunchConfigs By Id
	 * @param idsLaunchConfig Ids to look for
	 * @return entities found
	 */
	List<LaunchConfigEntity> retrieveLaunchConfigsById(List<Long> idsLaunchConfig);

	/**
	 * Retrieve LaunchPrefered By Id
	 * @param idsLaunchPrefered Ids to look for
	 * @return entities found
	 */
	List<LaunchPreferredEntity> retrieveLaunchPreferedById(List<Long> idsLaunchPrefered);

}
