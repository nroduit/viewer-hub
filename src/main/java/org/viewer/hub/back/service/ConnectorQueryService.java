package org.viewer.hub.back.service;

import org.springframework.security.core.Authentication;
import org.viewer.hub.back.model.patient.Patient;
import org.viewer.hub.back.model.searchcriteria.ArchiveSearchCriteria;
import org.viewer.hub.back.model.searchcriteria.IHESearchCriteria;

import java.util.Set;

public interface ConnectorQueryService {

    /**
     * Retrieve Patients with ArchiveSearchCriteria
     * @param searchCriteria ArchiveSearchCriteria
     * @param archives Archives to evaluate
     * @param authentication Authentication
     * @return patients found
     */
    Set<Patient> retrievePatientsWithoutIHESearchCriteria(ArchiveSearchCriteria searchCriteria, Set<String> archives, Authentication authentication);

    /**
     * Retrieve Patients with IHE Search Criteria
     * @param searchCriteria IHESearchCriteria
     * @param archives Archives to evaluate
     * @param authentication Authentication
     * @return patients found
     */
    Set<Patient> retrievePatientsWithIHESearchCriteria(IHESearchCriteria searchCriteria, Set<String> archives, Authentication authentication);

    /**
     * Fill list of patients from sop instance uids requests
     * @param patients Patients to fill
     * @param sopInstanceUids Sop instance uids numbers to look for
     * @param archives Archives
     */
    void buildFromSopInstanceUids(Set<Patient> patients, Set<String>  sopInstanceUids, Set<String>  archives, Authentication authentication);

    /**
     * Fill list of patients from serie instance uids requests
     * @param patients Patients to fill
     * @param seriesUids Serie instance uids numbers to look for
     * @param archives Archives
     */
    void buildFromSeriesInstanceUids(Set<Patient> patients, Set<String> seriesUids, Set<String>  archives, Authentication authentication);

    /**
     * Fill list of patients from study accession numbers requests
     * @param patients Patients to fill
     * @param accessionNumbers Study accession numbers to look for
     * @param archives Archives
     */
    void buildFromStudyAccessionNumbers(Set<Patient> patients,
                                            Set<String> accessionNumbers, Set<String>  archives, Authentication authentication);

    /**
     * Fill list of patients from study instance uids requests
     * @param patients Patients to fill
     * @param studyUids Study instance uids to look for
     * @param archives Archives
     */
    void buildFromStudyInstanceUids(Set<Patient> patients, Set<String> studyUids, Set<String>  archives, Authentication authentication);

    /**
     * Fill list of patients from patients ids requests
     * @param patients Patients to fill
     * @param patientIds Patient ids to look for
     * @param archives Archives
     */
    void buildFromPatientIds(Set<Patient> patients, Set<String> patientIds, Set<String> archives, Authentication authentication);
}
