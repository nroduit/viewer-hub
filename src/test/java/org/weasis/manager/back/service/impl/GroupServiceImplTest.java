package org.weasis.manager.back.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.weasis.manager.back.entity.GroupEntity;
import org.weasis.manager.back.entity.GroupEntityPK;
import org.weasis.manager.back.entity.TargetEntity;
import org.weasis.manager.back.enums.TargetType;
import org.weasis.manager.back.repository.GroupRepository;
import org.weasis.manager.back.repository.GroupRepositoryTest;
import org.weasis.manager.back.service.GroupService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class GroupServiceImplTest {

	private final GroupRepository groupRepositoryMock = Mockito.mock(GroupRepository.class);

	private GroupService groupService;

	@BeforeEach
	public void setUp() {
		// GroupEntity User
		GroupEntity groupUser = new GroupEntity();
		GroupEntityPK groupEntityPK = new GroupEntityPK();
		groupEntityPK.setGroupId(1L);
		groupEntityPK.setMemberId(2L);
		groupUser.setGroupEntityPK(groupEntityPK);

		// Group association
		GroupEntity groupAssociation = new GroupEntity();
		GroupEntityPK groupAssociationPK = new GroupEntityPK();
		groupAssociationPK.setGroupId(88L);
		groupAssociationPK.setMemberId(99L);
		groupAssociation.setGroupEntityPK(groupAssociationPK);

		// GroupRepository behaviour
		Mockito.when(this.groupRepositoryMock.findByGroupEntityPKMemberId(Mockito.anyLong()))
			.thenReturn(Collections.singletonList(groupUser));
		Mockito.when(this.groupRepositoryMock.findByGroupEntityPKGroupId(Mockito.anyLong()))
			.thenReturn(Collections.singletonList(groupUser));
		Mockito.when(this.groupRepositoryMock.saveAll(Mockito.any()))
			.thenReturn(Collections.singletonList(groupAssociation));
		Mockito.doNothing().when(this.groupRepositoryMock).deleteAll(Mockito.anyList());

		// Build the mocked target service
		this.groupService = new GroupServiceImpl(this.groupRepositoryMock);
	}

	/**
	 * Test to retrieve all groups corresponding to the target in parameter
	 */
	@Test
	void retrieveGroupsByMemberTest() {
		// TargetEntity User
		TargetEntity targetUser = new TargetEntity();
		targetUser.setName("TargetUser");
		targetUser.setType(TargetType.USER);
		targetUser.setId(2L);

		// Call service
		List<GroupEntity> groups = this.groupService.retrieveGroupsByMember(targetUser);

		// Test results
		assertEquals(1, groups.size());
		Mockito.verify(this.groupRepositoryMock, Mockito.times(1)).findByGroupEntityPKMemberId(Mockito.any());
		assertEquals(Long.valueOf(1), groups.get(0).getGroupEntityPK().getGroupId());
		assertEquals(Long.valueOf(2), groups.get(0).getGroupEntityPK().getMemberId());
	}

	/**
	 * Test to retrieve all groups corresponding to the group in parameter
	 */
	@Test
	void retrieveGroupsByGroupTest() {
		// TargetEntity UserGroup
		TargetEntity targetUserGroup = new TargetEntity();
		targetUserGroup.setName("TargetUserGroup");
		targetUserGroup.setType(TargetType.USER_GROUP);
		targetUserGroup.setId(2L);

		// Call service
		List<GroupEntity> groups = this.groupService.retrieveGroupsByGroup(targetUserGroup);

		// Test results
		assertEquals(1, groups.size());
		Mockito.verify(this.groupRepositoryMock, Mockito.times(1)).findByGroupEntityPKGroupId(Mockito.any());
		assertEquals(Long.valueOf(1), groups.get(0).getGroupEntityPK().getGroupId());
		assertEquals(Long.valueOf(2), groups.get(0).getGroupEntityPK().getMemberId());
	}

	/**
	 * Test to create group association
	 */
	@Test
	void createGroupAssociationTest() {

		TargetEntity group = GroupRepositoryTest.buildTarget(true, 88L, "Target Group", TargetType.HOST_GROUP);
		List<TargetEntity> targets = new ArrayList<>();
		TargetEntity target = GroupRepositoryTest.buildTarget(true, 99L, "Target Host", TargetType.HOST);
		targets.add(target);

		// Call service
		List<GroupEntity> groupAssociation = this.groupService.createGroupAssociation(group, targets);

		// Test results
		Mockito.verify(this.groupRepositoryMock, Mockito.times(1)).saveAll(Mockito.any());
		assertEquals(Long.valueOf(88), groupAssociation.get(0).getGroupEntityPK().getGroupId());
		assertEquals(Long.valueOf(99), groupAssociation.get(0).getGroupEntityPK().getMemberId());
	}

	/**
	 * Test delete group association
	 */
	@Test
	void deleteGroupAssociationTest() {
		// Init data
		TargetEntity targetEntity = GroupRepositoryTest.buildTarget(true, 1L, "Target Host", TargetType.HOST);

		// Call service
		this.groupService.deleteGroupAssociation(targetEntity);

		// Test results
		Mockito.verify(this.groupRepositoryMock, Mockito.times(1)).deleteAll(Mockito.anyList());
		Mockito.verify(this.groupRepositoryMock, Mockito.times(1)).findByGroupEntityPKGroupId(Mockito.anyLong());
	}

	/**
	 * Test delete member association
	 */
	@Test
	void deleteMemberAssociationTest() {
		// Init data
		TargetEntity targetEntity = GroupRepositoryTest.buildTarget(true, 1L, "Target Host", TargetType.HOST);

		// Call service
		this.groupService.deleteMemberAssociation(targetEntity);

		// Test results
		Mockito.verify(this.groupRepositoryMock, Mockito.times(1)).deleteAll(Mockito.anyList());
		Mockito.verify(this.groupRepositoryMock, Mockito.times(1)).findByGroupEntityPKMemberId(Mockito.anyLong());
	}

	/**
	 * Test delete members
	 */
	@Test
	void deleteMembersTest() {
		// Init data
		TargetEntity target = GroupRepositoryTest.buildTarget(true, 1L, "Target Host", TargetType.HOST);
		TargetEntity group = GroupRepositoryTest.buildTarget(true, 1L, "Target Host Group", TargetType.HOST_GROUP);

		// Call service
		this.groupService.deleteMembers(group, Collections.singletonList(target));

		// Test results
		Mockito.verify(this.groupRepositoryMock, Mockito.times(1)).deleteAll(Mockito.anyList());
	}

	/**
	 * Test delete groups
	 */
	@Test
	void deleteGroupsTest() {
		// Init data
		TargetEntity target = GroupRepositoryTest.buildTarget(true, 1L, "Target Host", TargetType.HOST);
		TargetEntity group = GroupRepositoryTest.buildTarget(true, 1L, "Target Host Group", TargetType.HOST_GROUP);

		// Call service
		this.groupService.deleteGroups(target, Collections.singletonList(group));

		// Test results
		Mockito.verify(this.groupRepositoryMock, Mockito.times(1)).deleteAll(Mockito.anyList());
	}

}
