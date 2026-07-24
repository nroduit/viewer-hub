/*
 *  Copyright (c) 2022-2026 Weasis Team and other contributors.
 *
 *  This program and the accompanying materials are made available under the terms of the Eclipse
 *  Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 *  License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *  SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package org.viewer.hub.back.repository;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.viewer.hub.back.config.properties.MicroDicomConfigurationProperties;
import org.viewer.hub.back.config.properties.SlicerConfigurationProperties;
import org.viewer.hub.back.config.properties.WeasisConfigurationProperties;
import org.viewer.hub.back.entity.ViewerSelectionEntity;
import org.viewer.hub.back.enums.ModalityType;
import org.viewer.hub.back.enums.ViewerType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Slf4j
class ViewerSelectionRepositoryTest {

	@Autowired
	private ViewerSelectionRepository repository;

	@MockitoBean
	private ClientRegistrationRepository clientRegistrationRepository;

	@MockitoBean
	private WeasisConfigurationProperties weasisConfigurationProperties;

	@MockitoBean
	private SlicerConfigurationProperties slicerConfigurationProperties;

	@MockitoBean
	private MicroDicomConfigurationProperties microDicomConfigurationProperties;

	/**
	 * Test save and find by id.
	 */
	@Test
	void shouldSaveAndFindARecord() {
		// Create an entity to save
		ViewerSelectionEntity entity = new ViewerSelectionEntity();
		entity.setPriority(1);
		entity.setArchive("dcm4chee");
		entity.setViewer(ViewerType.WEASIS);
		List<ModalityType> modalities = new ArrayList<>();
		modalities.add(ModalityType.CT);
		modalities.add(ModalityType.MR);
		entity.setModalities(modalities);

		// Save the entity
		LOG.info("Saving entity ViewerSelection with priority [{}]", entity.getPriority());
		entity = this.repository.save(entity);

		// Test Save
		assertEquals(1, entity.getPriority());
		assertEquals("dcm4chee", entity.getArchive());
		assertEquals(ViewerType.WEASIS, entity.getViewer());
		assertNotNull(entity.getId());
		assertEquals(2, entity.getModalities().size());
		LOG.info("Entity ViewerSelection with priority [{}] and id [{}] saved", entity.getPriority(), entity.getId());

		// Find By Id
		Optional<ViewerSelectionEntity> foundByIdOpt = this.repository.findById(entity.getId());

		// Test Find by Id
		assertTrue(foundByIdOpt.isPresent());
		LOG.info("Entity ViewerSelection found with priority [{}] and id [{}]", foundByIdOpt.get().getPriority(),
				foundByIdOpt.get().getId());
		assertEquals(entity.getId(), foundByIdOpt.get().getId());
		assertEquals("dcm4chee", foundByIdOpt.get().getArchive());
		assertEquals(ViewerType.WEASIS, foundByIdOpt.get().getViewer());
	}

	/**
	 * Test find all.
	 */
	@Test
	void shouldFindAllRecords() {
		// Create entities to save
		ViewerSelectionEntity entity1 = new ViewerSelectionEntity();
		entity1.setPriority(1);
		entity1.setArchive("dcm4chee");
		entity1.setViewer(ViewerType.WEASIS);

		ViewerSelectionEntity entity2 = new ViewerSelectionEntity();
		entity2.setPriority(2);
		entity2.setArchive("orthanc");
		entity2.setViewer(ViewerType.OHIF);

		// Save the entities
		LOG.info("Saving entity ViewerSelection with priority [{}]", entity1.getPriority());
		entity1 = this.repository.saveAndFlush(entity1);
		LOG.info("Saving entity ViewerSelection with priority [{}]", entity2.getPriority());
		entity2 = this.repository.saveAndFlush(entity2);

		// Find all
		List<ViewerSelectionEntity> all = this.repository.findAll();

		// Test find all
		assertNotNull(all);
		assertTrue(all.size() >= 2);
		LOG.info("Number of entities found [{}]", all.size());
	}

	/**
	 * Test modification of a record.
	 */
	@Test
	void shouldModifyRecord() {

		String initialArchive = "dcm4chee";
		String modifiedArchive = "orthanc";

		// Create an entity to save
		ViewerSelectionEntity entity = new ViewerSelectionEntity();
		entity.setPriority(1);
		entity.setArchive(initialArchive);
		entity.setViewer(ViewerType.WEASIS);

		// Save the entity
		LOG.info("Saving entity ViewerSelection with archive [{}]", entity.getArchive());
		entity = this.repository.save(entity);
		LOG.info("Id of the entity ViewerSelection with archive [{}]", entity.getId());

		// Test Save
		assertNotNull(entity);
		assertEquals(initialArchive, entity.getArchive());

		// Modify the record
		entity.setArchive(modifiedArchive);
		LOG.info("Modify entity ViewerSelection archive [{}] to [{}]", initialArchive, modifiedArchive);
		ViewerSelectionEntity entityModified = this.repository.save(entity);

		// Test Modify
		assertNotNull(entityModified);
		assertEquals(entity.getId(), entityModified.getId());
		assertEquals(modifiedArchive, entityModified.getArchive());
		LOG.info("Archive of the entity ViewerSelection with id [{}]: [{}]", entityModified.getId(),
				entityModified.getArchive());
	}

	/**
	 * Test delete record.
	 */
	@Test
	void shouldDeleteRecord() {
		// Create an entity to save
		ViewerSelectionEntity entity = new ViewerSelectionEntity();
		entity.setPriority(1);
		entity.setArchive("dcm4chee");
		entity.setViewer(ViewerType.WEASIS);

		// Save the entity
		LOG.info("Saving entity ViewerSelection with archive [{}]", entity.getArchive());
		entity = this.repository.save(entity);

		// Retrieve the entity
		Optional<ViewerSelectionEntity> foundByIdOpt = this.repository.findById(entity.getId());

		// Test Find by Id
		assertTrue(foundByIdOpt.isPresent());

		// Delete the entity
		entity = foundByIdOpt.get();
		Long id = entity.getId();
		LOG.info("Deleting entity with id [{}]", id);
		this.repository.delete(entity);

		// Test Delete
		foundByIdOpt = this.repository.findById(id);
		LOG.info("Is deleted entity with id [{}] present: [{}]", id, foundByIdOpt.isPresent());
		assertFalse(foundByIdOpt.isPresent());
	}

	/**
	 * Test the findByArchive method
	 */
	@Test
	void shouldFindByArchive() {
		// Create entities to save
		ViewerSelectionEntity entity1 = new ViewerSelectionEntity();
		entity1.setPriority(1);
		entity1.setArchive("dcm4chee");
		entity1.setViewer(ViewerType.WEASIS);

		ViewerSelectionEntity entity2 = new ViewerSelectionEntity();
		entity2.setPriority(2);
		entity2.setArchive("dcm4chee");
		entity2.setViewer(ViewerType.OHIF);

		ViewerSelectionEntity entity3 = new ViewerSelectionEntity();
		entity3.setPriority(3);
		entity3.setArchive("orthanc");
		entity3.setViewer(ViewerType.SLICER);

		// Save the entities
		LOG.info("Saving entity ViewerSelection with archive [{}]", entity1.getArchive());
		entity1 = this.repository.saveAndFlush(entity1);
		LOG.info("Saving entity ViewerSelection with archive [{}]", entity2.getArchive());
		entity2 = this.repository.saveAndFlush(entity2);
		LOG.info("Saving entity ViewerSelection with archive [{}]", entity3.getArchive());
		entity3 = this.repository.saveAndFlush(entity3);

		// Find by archive dcm4chee
		List<ViewerSelectionEntity> foundEntities = this.repository.findByArchive("dcm4chee");

		// Test find by archive
		assertNotNull(foundEntities);
		assertEquals(2, foundEntities.size());
		LOG.info("Number of entities found for archive 'dcm4chee': [{}]", foundEntities.size());
		assertTrue(foundEntities.stream().allMatch(e -> "dcm4chee".equals(e.getArchive())));

		// Find by archive orthanc
		foundEntities = this.repository.findByArchive("orthanc");
		assertEquals(1, foundEntities.size());
		assertEquals("orthanc", foundEntities.get(0).getArchive());

		// Find by archive that doesn't exist
		foundEntities = this.repository.findByArchive("nonexistent");
		assertTrue(foundEntities.isEmpty());
	}

	/**
	 * Test the findByArchiveAndViewer method
	 */
	@Test
	void shouldFindByArchiveAndViewer() {
		// Create entities to save
		ViewerSelectionEntity entity1 = new ViewerSelectionEntity();
		entity1.setPriority(1);
		entity1.setArchive("dcm4chee");
		entity1.setViewer(ViewerType.WEASIS);

		ViewerSelectionEntity entity2 = new ViewerSelectionEntity();
		entity2.setPriority(2);
		entity2.setArchive("dcm4chee");
		entity2.setViewer(ViewerType.OHIF);

		ViewerSelectionEntity entity3 = new ViewerSelectionEntity();
		entity3.setPriority(3);
		entity3.setArchive("orthanc");
		entity3.setViewer(ViewerType.WEASIS);

		// Save the entities
		LOG.info("Saving entity ViewerSelection with archive [{}] and viewer [{}]", entity1.getArchive(),
				entity1.getViewer());
		entity1 = this.repository.saveAndFlush(entity1);
		LOG.info("Saving entity ViewerSelection with archive [{}] and viewer [{}]", entity2.getArchive(),
				entity2.getViewer());
		entity2 = this.repository.saveAndFlush(entity2);
		LOG.info("Saving entity ViewerSelection with archive [{}] and viewer [{}]", entity3.getArchive(),
				entity3.getViewer());
		entity3 = this.repository.saveAndFlush(entity3);

		// Find by archive and viewer
		List<ViewerSelectionEntity> foundEntities = this.repository.findByArchiveAndViewer("dcm4chee",
				ViewerType.WEASIS);

		// Test find by archive and viewer
		assertNotNull(foundEntities);
		assertEquals(1, foundEntities.size());
		LOG.info("Number of entities found for archive 'dcm4chee' and viewer 'WEASIS': [{}]", foundEntities.size());
		assertEquals("dcm4chee", foundEntities.get(0).getArchive());
		assertEquals(ViewerType.WEASIS, foundEntities.get(0).getViewer());

		// Find by archive dcm4chee and viewer OHIF
		foundEntities = this.repository.findByArchiveAndViewer("dcm4chee", ViewerType.OHIF);
		assertEquals(1, foundEntities.size());
		assertEquals(ViewerType.OHIF, foundEntities.get(0).getViewer());

		// Find by archive orthanc and viewer WEASIS
		foundEntities = this.repository.findByArchiveAndViewer("orthanc", ViewerType.WEASIS);
		assertEquals(1, foundEntities.size());

		// Find by combination that doesn't exist
		foundEntities = this.repository.findByArchiveAndViewer("dcm4chee", ViewerType.SLICER);
		assertTrue(foundEntities.isEmpty());

		// Find by archive that doesn't exist
		foundEntities = this.repository.findByArchiveAndViewer("nonexistent", ViewerType.WEASIS);
		assertTrue(foundEntities.isEmpty());
	}

	/**
	 * Test the findFirstByViewer method
	 */
	@Test
	void shouldFindFirstByViewer() {
		// Create entities to save
		ViewerSelectionEntity entity1 = new ViewerSelectionEntity();
		entity1.setPriority(1);
		entity1.setArchive("dcm4chee");
		entity1.setViewer(ViewerType.WEASIS);

		ViewerSelectionEntity entity2 = new ViewerSelectionEntity();
		entity2.setPriority(2);
		entity2.setArchive("orthanc");
		entity2.setViewer(ViewerType.WEASIS);

		ViewerSelectionEntity entity3 = new ViewerSelectionEntity();
		entity3.setPriority(3);
		entity3.setArchive("dcm4chee");
		entity3.setViewer(ViewerType.OHIF);

		// Save the entities
		LOG.info("Saving entity ViewerSelection with viewer [{}]", entity1.getViewer());
		entity1 = this.repository.saveAndFlush(entity1);
		LOG.info("Saving entity ViewerSelection with viewer [{}]", entity2.getViewer());
		entity2 = this.repository.saveAndFlush(entity2);
		LOG.info("Saving entity ViewerSelection with viewer [{}]", entity3.getViewer());
		entity3 = this.repository.saveAndFlush(entity3);

		// Find first by viewer WEASIS
		Optional<ViewerSelectionEntity> foundEntity = this.repository.findFirstByViewer(ViewerType.WEASIS);

		// Test find first by viewer
		assertTrue(foundEntity.isPresent());
		LOG.info("First entity found for viewer 'WEASIS' with id [{}]", foundEntity.get().getId());
		assertEquals(ViewerType.WEASIS, foundEntity.get().getViewer());

		// Find first by viewer OHIF
		foundEntity = this.repository.findFirstByViewer(ViewerType.OHIF);
		assertTrue(foundEntity.isPresent());
		assertEquals(ViewerType.OHIF, foundEntity.get().getViewer());
		assertEquals("dcm4chee", foundEntity.get().getArchive());

		// Find first by viewer that doesn't exist
		foundEntity = this.repository.findFirstByViewer(ViewerType.SLICER);
		assertFalse(foundEntity.isPresent());
		LOG.info("No entity found for viewer 'SLICER'");
	}

	/**
	 * Test with null archive
	 */
	@Test
	void shouldHandleNullArchive() {
		// Create entity with null archive
		ViewerSelectionEntity entity = new ViewerSelectionEntity();
		entity.setPriority(1);
		entity.setArchive(null);
		entity.setViewer(ViewerType.WEASIS);

		// Save the entity
		LOG.info("Saving entity ViewerSelection with null archive");
		entity = this.repository.saveAndFlush(entity);

		// Test Save
		assertNotNull(entity.getId());
		assertNull(entity.getArchive());
		assertEquals(ViewerType.WEASIS, entity.getViewer());

		// Find by null archive (should work)
		List<ViewerSelectionEntity> foundEntities = this.repository.findByArchive(null);
		assertNotNull(foundEntities);
		assertTrue(foundEntities.isEmpty() || foundEntities.stream().allMatch(e -> e.getArchive() == null));
	}

	/**
	 * Test with modalities
	 */
	@Test
	void shouldHandleModalitiesCorrectly() {
		// Create entity with modalities
		ViewerSelectionEntity entity = new ViewerSelectionEntity();
		entity.setPriority(1);
		entity.setArchive("dcm4chee");
		entity.setViewer(ViewerType.WEASIS);
		List<ModalityType> modalities = new ArrayList<>();
		modalities.add(ModalityType.CT);
		modalities.add(ModalityType.MR);
		modalities.add(ModalityType.US);
		entity.setModalities(modalities);

		// Save the entity
		LOG.info("Saving entity ViewerSelection with [{}] modalities", entity.getModalities().size());
		entity = this.repository.saveAndFlush(entity);

		// Test Save with modalities
		assertNotNull(entity.getId());
		assertEquals(3, entity.getModalities().size());
		assertTrue(entity.getModalities().contains(ModalityType.CT));
		assertTrue(entity.getModalities().contains(ModalityType.MR));
		assertTrue(entity.getModalities().contains(ModalityType.US));

		// Retrieve and verify
		Optional<ViewerSelectionEntity> foundEntity = this.repository.findById(entity.getId());
		assertTrue(foundEntity.isPresent());
		assertEquals(3, foundEntity.get().getModalities().size());
	}

	/**
	 * Test with empty modalities list
	 */
	@Test
	void shouldHandleEmptyModalities() {
		// Create entity with empty modalities
		ViewerSelectionEntity entity = new ViewerSelectionEntity();
		entity.setPriority(1);
		entity.setArchive("dcm4chee");
		entity.setViewer(ViewerType.OHIF);
		entity.setModalities(new ArrayList<>());

		// Save the entity
		LOG.info("Saving entity ViewerSelection with empty modalities");
		entity = this.repository.saveAndFlush(entity);

		// Test Save
		assertNotNull(entity.getId());
		assertNotNull(entity.getModalities());
		assertTrue(entity.getModalities().isEmpty());

		// Retrieve and verify
		Optional<ViewerSelectionEntity> foundEntity = this.repository.findById(entity.getId());
		assertTrue(foundEntity.isPresent());
		assertNotNull(foundEntity.get().getModalities());
		assertTrue(foundEntity.get().getModalities().isEmpty());
	}

}
