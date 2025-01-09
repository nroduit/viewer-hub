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

package org.weasis.manager.back.util;

public class SpringDocUtil {

	/**
	 * Group
	 */

	// Description
	public static final String descriptionCheckParametersAssociation = "Checks done:\n"
			+ "- names of the targets have been well deserialized\n"
			+ "- group exists in database and has the right type\n"
			+ "- targets exist in database and have the right type";

	public static final String descriptionRetrieveHostsFromHostGroup = "Retrieve members of the host group in parameter. "
			+ "Checks done:\n " + "- group name not empty\n" + "- group name has been found and has the right type";

	public static final String descriptionRetrieveUsersFromUserGroup = "Retrieve members of the user group in parameter. "
			+ "Checks done:\n " + "- group name not empty\n" + "- group name has been found and has the right type";

	// Examples
	public static final String exObjValReqBodyUsersToUserGroup = "[\n" + "    { \n" + "        \"name\":\"user\" \n"
			+ "    }, \n" + "    { \n" + "        \"name\":\"nirt\"\n" + "    }, \n" + "    { \n"
			+ "        \"name\":\"btja\"\n" + "    }, \n" + "    { \n" + "        \"name\":\"jlrz\"\n" + "    }\n"
			+ "]";

	public static final String exObjValRespAssociateUsersToUserGroup = "[\n" + "    {\n"
			+ "        \"association\": {\n" + "            \"groupId\": 777,\n" + "            \"memberId\": 136\n"
			+ "        }\n" + "    },\n" + "    {\n" + "        \"association\": {\n"
			+ "            \"groupId\": 777,\n" + "            \"memberId\": 119\n" + "        }\n" + "    },\n"
			+ "    {\n" + "        \"association\": {\n" + "            \"groupId\": 777,\n"
			+ "            \"memberId\": 161\n" + "        }\n" + "    },\n" + "    {\n"
			+ "        \"association\": {\n" + "            \"groupId\": 777,\n" + "            \"memberId\": 137\n"
			+ "        }\n" + "    }\n" + "]";

	public static final String exObjValReqBodyHostsToHostGroup = "[\n" + "    { \n" + "        \"name\":\"pc-001\" \n"
			+ "    }, \n" + "    { \n" + "        \"name\":\"pc-001\"\n" + "    }\n" + "]";

	public static final String exObjValRespAssociateHostsToHostGroup = "[\n" + "    {\n"
			+ "        \"association\": {\n" + "            \"groupId\": 999,\n" + "            \"memberId\": 20\n"
			+ "        }\n" + "    },\n" + "    {\n" + "        \"association\": {\n"
			+ "            \"groupId\": 999,\n" + "            \"memberId\": 19\n" + "        }\n" + "    }\n" + "]";

	public static final String exObjValRespTargetsUserGroup = "[\n" + "    {\n" + "        \"id\": 236,\n"
			+ "        \"name\": \"target USERGROUP\",\n" + "        \"type\": \"USERGROUP\"\n" + "    },\n" + "    {\n"
			+ "        \"id\": 237,\n" + "        \"name\": \"target USERGROUP 2\",\n"
			+ "        \"type\": \"USERGROUP\"\n" + "    }\n" + "]";

	public static final String exObjValRespTargetsHostGroup = "[\n" + "    {\n" + "        \"id\": 236,\n"
			+ "        \"name\": \"target HOSTGROUP\",\n" + "        \"type\": \"HOSTGROUP\"\n" + "    },\n" + "    {\n"
			+ "        \"id\": 237,\n" + "        \"name\": \"target HOSTGROUP 2\",\n"
			+ "        \"type\": \"HOSTGROUP\"\n" + "    }\n" + "]";

	/**
	 * Target
	 */

	// Description
	public static final String descriptionCreateTarget = "Create targets: take the json body of the request which corresponds to the list of target entities."
			+ "Checks done:\n"
			+ "- name and type of the target have been well deserialized, target's type should have following codes: HOST, HOSTGROUP, USER, USERGROUP\n"
			+ "- name of the target should be unique, if the name already exists in database a message error will be shown";

	public static final String descriptionDeleteTarget = "Delete selected target: delete group associations + delete target.\n"
			+ "Checks done:\n" + "- check target name has been filled\n" + "- check target exist\n"
			+ "- check target not associated to a launch entity\n";

	// Examples
	public static final String exObjValReqBodyTargets = "[\n" + "    { \n" + "        \"name\":\"target HOST\", \n"
			+ "        \"type\":\"HOST\" \n" + "    }, \n" + "    { \n" + "        \"name\":\"target USER\", \n"
			+ "        \"type\":\"USER\" \n" + "    },\n" + "    { \n" + "        \"name\":\"target USERGROUP\", \n"
			+ "        \"type\":\"USERGROUP\" \n" + "    },\n" + "    { \n"
			+ "        \"name\":\"target HOSTGROUP\", \n" + "        \"type\":\"HOSTGROUP\" \n" + "    }\n" + "]";

	public static final String exObjValRespTargets = "[\n" + "    {\n" + "        \"id\": 234,\n"
			+ "        \"name\": \"target HOST\",\n" + "        \"type\": \"HOST\"\n" + "    },\n" + "    {\n"
			+ "        \"id\": 235,\n" + "        \"name\": \"target USER\",\n" + "        \"type\": \"USER\"\n"
			+ "    },\n" + "    {\n" + "        \"id\": 236,\n" + "        \"name\": \"target USERGROUP\",\n"
			+ "        \"type\": \"USERGROUP\"\n" + "    },\n" + "    {\n" + "        \"id\": 237,\n"
			+ "        \"name\": \"target HOSTGROUP\",\n" + "        \"type\": \"HOSTGROUP\"\n" + "    }\n" + "]";

	public static final String exObjValRespTargetsUsers = "[\n" + "    {\n" + "        \"id\": 234,\n"
			+ "        \"name\": \"target USER 1\",\n" + "        \"type\": \"USER\"\n" + "    },\n" + "    {\n"
			+ "        \"id\": 235,\n" + "        \"name\": \"target USER 2\",\n" + "        \"type\": \"USER\"\n"
			+ "    }" + "]";

	public static final String exObjValRespTargetsHosts = "[\n" + "    {\n" + "        \"id\": 234,\n"
			+ "        \"name\": \"target HOST 1\",\n" + "        \"type\": \"HOST\"\n" + "    },\n" + "    {\n"
			+ "        \"id\": 235,\n" + "        \"name\": \"target HOST 2\",\n" + "        \"type\": \"HOST\"\n"
			+ "    }" + "]";

	/**
	 * Launch Preference
	 */

	// Description
	public static final String descriptionLaunchesByUserHostConfigPrefered = "Retrieve the Launches by user/host/config/requested prefered: retrieve the launches for the config in parameter "
			+ "or for config default if no config requested:\n" + " - Prefered type: all => retrieve all the prefered\n"
			+ " - Prefered type: specific prefered type => retrieve the specific prefered";

	public static final String descriptionLaunchesByUserHostConfig = "Retrieve the Launches by by user/host/config: retrieve the launches for the config in parameter "
			+ "or for config default if no config requested. No filter on prefered type.\n";

	public static final String descriptionCreateLaunchConfig = "Create launch configs: take the json body of the request which corresponds to the list of launch config entities. Checks done:\n"
			+ "- Names of LaunchConfig have well been deserialized\n"
			+ "- Name of the launchConfig should be unique, if a LaunchConfig with this name already exist in database, an error will be shown";

	public static final String descriptionCreateLaunchPrefered = "Create prefered: take the json body of the request which corresponds to the list of prefered entities.Checks done:\n"
			+ "- Names of LaunchPrefered have well been deserialized\n"
			+ "- Name of the launchPrefered should be unique, if a LaunchPrefered with this name already exist in database, an error will be shown";

	public static final String descriptionCreateLaunch = "Create launches: take the json body of the request which corresponds to the list of launch entities:\n"
			+ "- Check LaunchConfig/LaunchPrefered/Target have correctly been deserialized,\n"
			+ "- LaunchConfig/LaunchPrefered/Target should already exists in order to create the launch,\n"
			+ "  otherwise an error message will appear to warn the user to create the corresponding entity\n"
			+ "- Value can have value \" \" but not \"\" : database constraint not null on value column";

	public static final String descriptionDeleteLaunchConfig = "Delete a launch config:\n - check name not empty \n - check selected launch config exist \n - check there is no launch associated to this launch config";

	public static final String descriptionDeleteLaunchPrefered = "Delete a launch prefered:\n - check name not empty \n - check selected launch prefered exist \n - check there is no launch associated to this launch prefered";

	public static final String descriptionDeleteLaunch = "Delete launches: take the json body of the request which corresponds to the list of launch entities. Check that Launches in BodyRequest have well been deserialized and exist.";

	public static final String descriptionLaunchesForAGroup = "Retrieve launches for a group. If param config is filled, apply a filter on the Launches returned depending on the Launch Config name."
			+ "Checks done: \n" + "- group name has been filled\n"
			+ "- group exists and has a type 'group' (HOSTGROUP or USERGROUP)";

	// Examples
	public static final String exObjValRespGetLaunchesByRequestedPrefered = "<?xml version='1.0' encoding='UTF-8'?>\n"
			+ "<Launches>\n" + "    <Launch>\n" + "        <Config>\n" + "            <Id>1</Id>\n"
			+ "            <Name>default</Name>\n" + "        </Config>\n" + "        <Prefered>\n"
			+ "            <Id>49</Id>\n" + "            <Name>ext-config</Name>\n"
			+ "            <Type>ext-cfg</Type>\n" + "        </Prefered>\n" + "        <Target>\n"
			+ "            <Id>20</Id>\n" + "            <Name>pc-001</Name>\n" + "            <Type>HOST</Type>\n"
			+ "        </Target>\n" + "        <Value>alternatecdb</Value>\n" + "    </Launch>\n" + "    <Launch>\n"
			+ "        <Config>\n" + "            <Id>1</Id>\n" + "            <Name>default</Name>\n"
			+ "        </Config>\n" + "        <Prefered>\n" + "            <Id>76</Id>\n"
			+ "            <Name>property 2</Name>\n" + "            <Type>pro</Type>\n" + "        </Prefered>\n"
			+ "        <Target>\n" + "            <Id>20</Id>\n" + "            <Name>pc-001</Name>\n"
			+ "            <Type>HOST</Type>\n" + "        </Target>\n"
			+ "        <Value>weasis.export.dicom+false</Value>\n" + "    </Launch>\n" + "</Launches>";

	public static final String exObjValReqBodyCreateLaunchConfig = "[\n" + "    {\n"
			+ "        \"name\":\"name config 1\"\n" + "    },\n" + "    {\n" + "        \"name\":\"name config 2\"\n"
			+ "    }\n" + "]";

	public static final String exObjValRespLaunchConfig = "[\n" + "    {\n" + "        \"id\": 63,\n"
			+ "        \"name\": \"name config 1\"\n" + "    },\n" + "    {\n" + "        \"id\": 64,\n"
			+ "        \"name\": \"name config 2\"\n" + "    }\n" + "]";

	public static final String exObjValReqBodyCreateLaunchPrefered = "[\n" + "    {\n"
			+ "        \"name\":\"name prefered 1\",\n" + "        \"type\":\"ext-cfg\"\n" + "    },\n" + "    {\n"
			+ "        \"name\":\"name prefered 2\",\n" + "        \"type\":\"pro\"\n" + "    }\n" + "]";

	public static final String exObjValRespLaunchPrefered = "[\n" + "    {\n" + "        \"id\": 90,\n"
			+ "        \"name\": \"name prefered 1\",\n" + "        \"type\": \"ext-cfg\"\n" + "    },\n" + "    {\n"
			+ "        \"id\": 91,\n" + "        \"name\": \"name prefered 2\",\n" + "        \"type\": \"pro\"\n"
			+ "    }\n" + "]";

	public static final String exObjValRespLaunchPreferedSpecific = "[\n" + "    {\n" + "        \"id\": 91,\n"
			+ "        \"name\": \"name prefered 2\",\n" + "        \"type\": \"pro\"\n" + "    }\n" + "]";

	public static final String exObjValReqBodyCreateLaunch = "[\n" + "    { \n" + "        \"config\": {\n"
			+ "            \"name\":\"name config 1\"\n" + "        },\n" + "        \"prefered\": {\n"
			+ "            \"name\":\"name prefered 1\"\n" + "        },\n" + "        \"target\": {\n"
			+ "            \"name\":\"name target 1\"\n" + "        },\n" + "        \"value\":\"value launch 1\" \n"
			+ "    },\n" + "    { \n" + "        \"config\": {\n" + "            \"name\":\"name config 2\"\n"
			+ "        },\n" + "        \"prefered\": {\n" + "            \"name\":\"name prefered 2\"\n"
			+ "        },\n" + "        \"target\": {\n" + "            \"name\":\"name target 2\"\n" + "        },\n"
			+ "        \"value\":\" \" \n" + "    }\n" + "]";

	public static final String exObjValRespCreateLaunch = "[\n" + "    {\n" + "        \"config\": {\n"
			+ "            \"id\": 63,\n" + "            \"name\": \"name config 1\"\n" + "        },\n"
			+ "        \"prefered\": {\n" + "            \"id\": 90,\n" + "            \"name\": \"name prefered 1\",\n"
			+ "            \"type\": \"ext-cfg\"\n" + "        },\n" + "        \"target\": {\n"
			+ "            \"id\": 264,\n" + "            \"name\": \"name target 1\",\n"
			+ "            \"type\": \"USER\"\n" + "        },\n" + "        \"value\": \"value launch 1\"\n"
			+ "    },\n" + "    {\n" + "        \"config\": {\n" + "            \"id\": 64,\n"
			+ "            \"name\": \"name config 2\"\n" + "        },\n" + "        \"prefered\": {\n"
			+ "            \"id\": 91,\n" + "            \"name\": \"name prefered 2\",\n"
			+ "            \"type\": \"pro\"\n" + "        },\n" + "        \"target\": {\n"
			+ "            \"id\": 263,\n" + "            \"name\": \"name target 2\",\n"
			+ "            \"type\": \"HOST\"\n" + "        },\n" + "        \"value\": \" \"\n" + "    }\n" + "]";

	public static final String exObjValReqBodyDeleteLaunch = "[\n" + "   {\n" + "       \"config\": {\n"
			+ "           \"name\":\"name config 1\"\n" + "       },\n" + "       \"prefered\": {\n"
			+ "           \"name\":\"name prefered 1\"\n" + "       },\n" + "       \"target\": {\n"
			+ "           \"name\":\"name target 1\"\n" + "       }\n" + "   },\n" + "   {\n" + "       \"config\": {\n"
			+ "           \"name\":\"name config 2\"\n" + "       },\n" + "       \"prefered\": {\n"
			+ "           \"name\":\"name prefered 2\"\n" + "       },\n" + "       \"target\": {\n"
			+ "           \"name\":\"name target 2\"\n" + "       }\n" + "   }\n" + "]";

	public static final String exObjValRespGetGroupLaunches = "[\n" + "    {\n" + "        \"config\": {\n"
			+ "            \"id\": 93,\n" + "            \"name\": \"name config 1\"\n" + "        },\n"
			+ "        \"prefered\": {\n" + "            \"id\": 116,\n"
			+ "            \"name\": \"name prefered 1\",\n" + "            \"type\": \"ext-cfg\"\n" + "        },\n"
			+ "        \"target\": {\n" + "            \"id\": 310,\n" + "            \"name\": \"hostgroup test\",\n"
			+ "            \"type\": \"HOSTGROUP\"\n" + "        },\n" + "        \"value\": \"value launch 1\"\n"
			+ "    },\n" + "    {\n" + "        \"config\": {\n" + "            \"id\": 94,\n"
			+ "            \"name\": \"name config 2\"\n" + "        },\n" + "        \"prefered\": {\n"
			+ "            \"id\": 117,\n" + "            \"name\": \"name prefered 2\",\n"
			+ "            \"type\": \"pro\"\n" + "        },\n" + "        \"target\": {\n"
			+ "            \"id\": 310,\n" + "            \"name\": \"hostgroup test\",\n"
			+ "            \"type\": \"HOSTGROUP\"\n" + "        },\n" + "        \"value\": \"value launch 2\"\n"
			+ "    }\n" + "]";

}
