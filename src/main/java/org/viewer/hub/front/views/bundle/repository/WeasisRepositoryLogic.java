package org.viewer.hub.front.views.bundle.repository;

import com.vaadin.flow.data.provider.QuerySortOrder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.viewer.hub.back.controller.exception.TechnicalException;
import org.viewer.hub.back.model.Message;
import org.viewer.hub.back.model.MessageFormat;
import org.viewer.hub.back.model.MessageLevel;
import org.viewer.hub.back.model.MessageType;
import org.viewer.hub.back.model.asset.WeasisAssetModel;
import org.viewer.hub.back.service.PackageService;
import org.viewer.hub.back.service.WeasisRepositoryService;
import org.viewer.hub.front.views.bundle.repository.component.WeasisAssetFilter;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Service for the front view of the Weasis nexus repository
 */
@Service
public class WeasisRepositoryLogic {

	// View
	@Setter
	@Getter
	private WeasisRepositoryView weasisRepositoryView;

	private final WeasisRepositoryService weasisRepositoryService;

	private final PackageService packageService;

	@Autowired
	public WeasisRepositoryLogic(WeasisRepositoryService weasisRepositoryService, PackageService packageService) {
		this.weasisRepositoryService = weasisRepositoryService;
		this.packageService = packageService;
	}

	/**
	 * Retrieve Page of Weasis assets from nexus repository
	 * @param filter Filters applied
	 * @param sortOrders Sorts applied
	 * @param pageable Pageable
	 * @return Page of Weasis assets from nexus repository
	 */
	public Page<WeasisAssetModel> retrieveWeasisAssets(WeasisAssetFilter filter, List<QuerySortOrder> sortOrders,
			Pageable pageable) {
		return weasisRepositoryService.retrieveWeasisAssets(filter, sortOrders, pageable);
	}

	/**
	 * Count Weasis assets from nexus repository depending on filters
	 * @param filter Filters applied
	 * @return number of Weasis assets from nexus repository
	 */
	public int countWeasisAssets(WeasisAssetFilter filter) {
		return weasisRepositoryService.countWeasisAssets(filter);
	}

	/**
	 * Import the package version selected
	 * @param weasisAssetModel Weasis Asset to import
	 */
	public void importPackageVersion(WeasisAssetModel weasisAssetModel) {
		try (InputStream fileDataInputStream = weasisRepositoryService.downloadWeasisAsset(weasisAssetModel)) {
			if (this.packageService.isImportCoherent(fileDataInputStream)) {

				// Determine the version to upload, if incorrect format return null
				String versionToUpload = this.packageService.checkWeasisNativeVersionToUpload(fileDataInputStream);

				if (versionToUpload != null) {
					this.packageService.handlePackageVersionToUpload(fileDataInputStream, versionToUpload);
					this.weasisRepositoryView
						.displayMessage(
								new Message(MessageLevel.INFO, MessageFormat.TEXT,
										"Package version %s has been uploaded"
											.formatted(weasisAssetModel.getVersion())),
								MessageType.NOTIFICATION_MESSAGE);
				}
				else {
					this.weasisRepositoryView.displayMessage(new Message(MessageLevel.ERROR, MessageFormat.TEXT,
							"Issue when importing: rebuild before importing your zip file with appropriate version in the property weasis.version of the file config.properties or base.json"),
							MessageType.NOTIFICATION_MESSAGE);
				}
			}
			else {
				this.weasisRepositoryView.displayMessage(new Message(MessageLevel.ERROR, MessageFormat.TEXT,
						"Issue when importing: incoherent import (version already present or compatibility version file not coherent)"),
						MessageType.NOTIFICATION_MESSAGE);
			}
		}
		catch (IOException e) {
			throw new TechnicalException(
					"Issue when getting the input stream of the Nexus Weasis repository:%s".formatted(e.getMessage()));
		}
	}

	/**
	 * Remove package version installed on the server
	 * @param weasisAssetModel WeasisAssetModel
	 */
	public void removePackageVersion(WeasisAssetModel weasisAssetModel) {
		this.weasisRepositoryService.removeResourcePackageVersion(weasisAssetModel);
		this.weasisRepositoryView.displayMessage(
				new Message(MessageLevel.INFO, MessageFormat.TEXT,
						"Package version %s has been removed".formatted(weasisAssetModel.getVersion())),
				MessageType.NOTIFICATION_MESSAGE);
	}

}
