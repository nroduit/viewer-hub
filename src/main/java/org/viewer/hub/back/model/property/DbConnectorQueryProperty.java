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

package org.viewer.hub.back.model.property;

import java.util.Objects;

public class DbConnectorQueryProperty {

	private String select;

	private String accessionNumberColumn;

	private String patientIdColumn;

	private String studyInstanceUidColumn;

	private String serieInstanceUidColumn;

	private String sopInstanceUidColumn;

	public DbConnectorQueryProperty() {
	}

	public DbConnectorQueryProperty(String select, String accessionNumberColumn, String patientIdColumn,
			String studyInstanceUidColumn, String serieInstanceUidColumn, String sopInstanceUidColumn) {
		this.select = select;
		this.accessionNumberColumn = accessionNumberColumn;
		this.patientIdColumn = patientIdColumn;
		this.studyInstanceUidColumn = studyInstanceUidColumn;
		this.serieInstanceUidColumn = serieInstanceUidColumn;
		this.sopInstanceUidColumn = sopInstanceUidColumn;
	}

	public String getSelect() {
		return this.select;
	}

	public void setSelect(String select) {
		this.select = select;
	}

	public String getAccessionNumberColumn() {
		return this.accessionNumberColumn;
	}

	public void setAccessionNumberColumn(String accessionNumberColumn) {
		this.accessionNumberColumn = accessionNumberColumn;
	}

	public String getPatientIdColumn() {
		return this.patientIdColumn;
	}

	public void setPatientIdColumn(String patientIdColumn) {
		this.patientIdColumn = patientIdColumn;
	}

	public String getStudyInstanceUidColumn() {
		return this.studyInstanceUidColumn;
	}

	public void setStudyInstanceUidColumn(String studyInstanceUidColumn) {
		this.studyInstanceUidColumn = studyInstanceUidColumn;
	}

	public String getSerieInstanceUidColumn() {
		return this.serieInstanceUidColumn;
	}

	public void setSerieInstanceUidColumn(String serieInstanceUidColumn) {
		this.serieInstanceUidColumn = serieInstanceUidColumn;
	}

	public String getSopInstanceUidColumn() {
		return this.sopInstanceUidColumn;
	}

	public void setSopInstanceUidColumn(String sopInstanceUidColumn) {
		this.sopInstanceUidColumn = sopInstanceUidColumn;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		DbConnectorQueryProperty that = (DbConnectorQueryProperty) o;
		return Objects.equals(this.select, that.select)
				&& Objects.equals(this.accessionNumberColumn, that.accessionNumberColumn)
				&& Objects.equals(this.patientIdColumn, that.patientIdColumn)
				&& Objects.equals(this.studyInstanceUidColumn, that.studyInstanceUidColumn)
				&& Objects.equals(this.serieInstanceUidColumn, that.serieInstanceUidColumn)
				&& Objects.equals(this.sopInstanceUidColumn, that.sopInstanceUidColumn);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.select, this.accessionNumberColumn, this.patientIdColumn, this.studyInstanceUidColumn,
				this.serieInstanceUidColumn, this.sopInstanceUidColumn);
	}

}
