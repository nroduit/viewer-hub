package org.weasis.manager.back.repository;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.weasis.manager.back.entity.GroupEntity;
import org.weasis.manager.back.entity.GroupEntityPK;
import org.weasis.manager.back.entity.TargetEntity;
import org.weasis.manager.back.enums.TargetType;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Slf4j
public class GroupRepositoryTest {

	@Autowired
	private GroupRepository groupRepository;

	@Autowired
	private TargetRepository targetRepository;

	/**
	 * Build a target
	 * @param name Name of the target
	 * @param targetType Type of the target
	 * @param setId If the method will set id in parameter
	 * @param id Id to set
	 */
	public static TargetEntity buildTarget(boolean setId, Long id, String name, TargetType targetType) {
		TargetEntity target = new TargetEntity();
		if (setId) {
			target.setId(id);
		}
		target.setName(name);
		target.setType(targetType);
		return target;
	}

	/**
	 * Build a group entity
	 * @param targetGroup Group Target
	 * @param targetMember Member Target
	 * @return Group built
	 */
	public static GroupEntity buildGroup(TargetEntity targetGroup, TargetEntity targetMember) {
		GroupEntity entity = new GroupEntity();
		GroupEntityPK groupEntityPK = new GroupEntityPK();
		groupEntityPK.setGroupId(targetGroup.getId());
		groupEntityPK.setMemberId(targetMember.getId());
		entity.setGroupEntityPK(groupEntityPK);
		return entity;
	}

	/**
	 * Test save and find by id.
	 */
	@Test
	void shouldSaveAndFindARecord() {
		// Create an entity to save
		TargetEntity targetGroup = buildTarget(false, null, "group", TargetType.USER_GROUP);
		TargetEntity targetMember = buildTarget(false, null, "member", TargetType.USER);
		targetGroup = this.targetRepository.saveAndFlush(targetGroup);
		targetMember = this.targetRepository.saveAndFlush(targetMember);
		GroupEntity entity = buildGroup(targetGroup, targetMember);

		// Save the entity
		LOG.info("Saving entity Group with Group ID [{}] and Member ID [{}]", entity.getGroupEntityPK().getGroupId(),
				entity.getGroupEntityPK().getMemberId());
		entity = this.groupRepository.saveAndFlush(entity);

		// Test Save
		assertEquals("GROUP", this.targetRepository.findById(entity.getGroupEntityPK().getGroupId()).get().getName());
		assertEquals("MEMBER", this.targetRepository.findById(entity.getGroupEntityPK().getMemberId()).get().getName());
		assertEquals(targetGroup.getId(), entity.getGroupEntityPK().getGroupId());
		assertEquals(targetMember.getId(), entity.getGroupEntityPK().getMemberId());
		LOG.info("Entity Target with  with Group ID [{}] and Member ID [{}] saved",
				entity.getGroupEntityPK().getGroupId(), entity.getGroupEntityPK().getMemberId());

		// Find By Id
		GroupEntityPK groupEntityPK = new GroupEntityPK();
		groupEntityPK.setGroupId(entity.getGroupEntityPK().getGroupId());
		groupEntityPK.setMemberId(entity.getGroupEntityPK().getMemberId());
		Optional<GroupEntity> foundByIdOpt = this.groupRepository.findById(groupEntityPK);

		// Test Find by Id
		assertTrue(foundByIdOpt.isPresent());
		LOG.info("Entity Target found with name [{}] and id [{}]", foundByIdOpt.get().getGroupEntityPK().getGroupId(),
				foundByIdOpt.get().getGroupEntityPK().getMemberId());
		assertEquals(entity.getGroupEntityPK().getGroupId(), foundByIdOpt.get().getGroupEntityPK().getGroupId());
		assertEquals(entity.getGroupEntityPK().getMemberId(), foundByIdOpt.get().getGroupEntityPK().getMemberId());
	}

	/**
	 * Test find all.
	 */
	@Test
	void shouldFindAllRecords() {
		// Create an entity to save
		TargetEntity targetGroup = buildTarget(false, null, "group", TargetType.USER_GROUP);
		TargetEntity targetMember = buildTarget(false, null, "member", TargetType.USER);
		targetGroup = this.targetRepository.saveAndFlush(targetGroup);
		targetMember = this.targetRepository.saveAndFlush(targetMember);
		GroupEntity entity = buildGroup(targetGroup, targetMember);

		// Save the entity
		entity = this.groupRepository.saveAndFlush(entity);
		LOG.info("Saving entity Group with Group ID [{}] and Member ID [{}]", entity.getGroupEntityPK().getGroupId(),
				entity.getGroupEntityPK().getMemberId());

		// Find all
		List<GroupEntity> all = this.groupRepository.findAll();

		// Test find all
		assertNotNull(all);
		assertTrue(all.size() > 0);
		assertEquals(1, all.size());
		LOG.info("Number of entities found [{}]", all.size());
	}

	/**
	 * Test delete record.
	 */
	@Test
	void shouldDeleteRecord() {
		// Create an entity to save
		TargetEntity targetGroup = buildTarget(false, null, "group", TargetType.USER_GROUP);
		TargetEntity targetMember = buildTarget(false, null, "member", TargetType.USER);
		targetGroup = this.targetRepository.saveAndFlush(targetGroup);
		targetMember = this.targetRepository.saveAndFlush(targetMember);
		GroupEntity entity = buildGroup(targetGroup, targetMember);

		// Save the entity
		LOG.info("Saving entity Group with Group ID [{}] and Member ID [{}]", entity.getGroupEntityPK().getGroupId(),
				entity.getGroupEntityPK().getMemberId());
		entity = this.groupRepository.saveAndFlush(entity);

		// Retrieve the entity
		GroupEntityPK groupEntityPK = new GroupEntityPK();
		groupEntityPK.setGroupId(targetGroup.getId());
		groupEntityPK.setMemberId(targetMember.getId());
		Optional<GroupEntity> foundByIdOpt = this.groupRepository.findById(groupEntityPK);

		// Test Find by Id
		assertTrue(foundByIdOpt.isPresent());

		// Delete the entity
		entity = foundByIdOpt.get();
		GroupEntityPK id = entity.getGroupEntityPK();
		LOG.info("Deleting entity with id [{}]", id);
		this.groupRepository.delete(entity);

		// Test Delete
		foundByIdOpt = this.groupRepository.findById(id);
		LOG.info("Is deleted entity with id [{}] present: [{}]", id, foundByIdOpt.isPresent());
		assertFalse(foundByIdOpt.isPresent());
	}

	/**
	 * Should call method findByGroupEntityPKMemberId and retrieve entity saved in
	 * database
	 */
	@Test
	void shouldFindByGroupEntityPKMemberId() {
		// Create an entity to save
		TargetEntity targetGroup = buildTarget(false, null, "group", TargetType.USER_GROUP);
		TargetEntity targetMember = buildTarget(false, null, "member", TargetType.USER);
		targetGroup = this.targetRepository.saveAndFlush(targetGroup);
		targetMember = this.targetRepository.saveAndFlush(targetMember);
		GroupEntity entity = buildGroup(targetGroup, targetMember);

		// Save the entity
		LOG.info("Saving entity Group with Group ID [{}] and Member ID [{}]", entity.getGroupEntityPK().getGroupId(),
				entity.getGroupEntityPK().getMemberId());
		entity = this.groupRepository.saveAndFlush(entity);

		// Retrieve the entity
		List<GroupEntity> groupEntities = this.groupRepository.findByGroupEntityPKMemberId(targetMember.getId());

		// Test result
		assertEquals(1, groupEntities.size());
		assertEquals(targetGroup.getId(), groupEntities.get(0).getGroupEntityPK().getGroupId());
		assertEquals(targetMember.getId(), groupEntities.get(0).getGroupEntityPK().getMemberId());
	}

	/**
	 * Should call method findByGroupEntityPKGroupId and retrieve entity saved in database
	 */
	@Test
	void shouldFindByGroupEntityPKGroupId() {
		// Create an entity to save
		TargetEntity targetGroup = buildTarget(false, null, "group", TargetType.USER_GROUP);
		TargetEntity targetMember = buildTarget(false, null, "member", TargetType.USER);
		targetGroup = this.targetRepository.saveAndFlush(targetGroup);
		targetMember = this.targetRepository.saveAndFlush(targetMember);
		GroupEntity entity = buildGroup(targetGroup, targetMember);

		// Save the entity
		LOG.info("Saving entity Group with Group ID [{}] and Member ID [{}]", entity.getGroupEntityPK().getGroupId(),
				entity.getGroupEntityPK().getMemberId());
		entity = this.groupRepository.saveAndFlush(entity);

		// Retrieve the entity
		List<GroupEntity> groupEntities = this.groupRepository.findByGroupEntityPKGroupId(targetGroup.getId());

		// Test result
		assertEquals(1, groupEntities.size());
		assertEquals(targetGroup.getId(), groupEntities.get(0).getGroupEntityPK().getGroupId());
		assertEquals(targetMember.getId(), groupEntities.get(0).getGroupEntityPK().getMemberId());
	}

}