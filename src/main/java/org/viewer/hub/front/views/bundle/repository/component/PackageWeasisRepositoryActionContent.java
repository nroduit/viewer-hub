package org.viewer.hub.front.views.bundle.repository.component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.viewer.hub.back.model.asset.WeasisAssetModel;
import org.viewer.hub.front.components.UIUtil;
import org.viewer.hub.front.views.bundle.repository.WeasisRepositoryView;

/**
 * Component which allow to do display action buttons: used to handle the removal and
 * download of a package version in the nexus repository
 */
public class PackageWeasisRepositoryActionContent extends HorizontalLayout {

	// Tooltips
	public static final String TOOLTIP_REMOVE_VERSION = "Remove this version";

	public static final String TOOLTIP_DOWNLOAD_VERSION = "Download this version";

	// View
	private final WeasisRepositoryView weasisRepositoryView;

	public PackageWeasisRepositoryActionContent(WeasisRepositoryView weasisRepositoryView) {
		this.weasisRepositoryView = weasisRepositoryView;
	}

	/**
	 * Build action button
	 * @param weasisAssetModel WeasisAssetModel linked to these buttons
	 */
	public void buildActionContentToDisplay(WeasisAssetModel weasisAssetModel) {
		if (weasisAssetModel != null) {
			if (weasisAssetModel.isAlreadyInstalled()) {
				// Button Delete
				Button buttonRemove = new Button();
				buttonRemove.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_PRIMARY,
						ButtonVariant.LUMO_ERROR);
				buttonRemove.setIcon(new Icon(VaadinIcon.TRASH));
				this.weasisRepositoryView.addActionButtonRemoveClickListener(buttonRemove, weasisAssetModel);
				UIUtil.setTooltip(buttonRemove, TOOLTIP_REMOVE_VERSION);
				this.add(buttonRemove);
			}
			else {
				// Button download
				Button buttonDownload = new Button();
				buttonDownload.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_PRIMARY,
						ButtonVariant.LUMO_TERTIARY);
				buttonDownload.setIcon(new Icon(VaadinIcon.DOWNLOAD));
				this.weasisRepositoryView.addActionButtonImportClickListener(buttonDownload, weasisAssetModel);
				UIUtil.setTooltip(buttonDownload, TOOLTIP_DOWNLOAD_VERSION);
				this.add(buttonDownload);
			}
		}
	}

}
