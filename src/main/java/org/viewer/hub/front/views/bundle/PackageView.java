package org.viewer.hub.front.views.bundle;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Component;
import org.vaadin.lineawesome.LineAwesomeIcon;
import org.vaadin.lineawesome.LineAwesomeIconUrl;
import org.viewer.hub.front.views.bundle.override.OverrideView;
import org.viewer.hub.front.views.bundle.repository.WeasisRepositoryView;

@PageTitle("Package")
@Route(value = "package")
@Menu(order = 2, icon = LineAwesomeIconUrl.BOX_OPEN_SOLID)
@Secured({ "ROLE_admin" })
@Component
@UIScope
public class PackageView extends Composite<VerticalLayout> {

	private final OverrideView overrideView;

	private final WeasisRepositoryView weasisRepositoryView;

	private final Div content = new Div();

	@Autowired
	public PackageView(OverrideView overrideView, WeasisRepositoryView weasisRepositoryView) {
		this.overrideView = overrideView;
		this.weasisRepositoryView = weasisRepositoryView;

		VerticalLayout packageLayout = getContent();
		packageLayout.setHeight(91, Unit.PERCENTAGE);

		Tab installedTab = new Tab(LineAwesomeIcon.SERVER_SOLID.create(), new Span("Installed"));
		Tab availableTab = new Tab(LineAwesomeIcon.DOWNLOAD_SOLID.create(), new Span("Available"));
		Tabs tabs = new Tabs(installedTab, availableTab);

		tabs.getStyle().set("flex-grow", "1");
		tabs.setWidth("100%");

		// Add a listener to change the content depending on the selected tab
		tabs.addSelectedChangeListener(event -> {
			content.removeAll();
			if (event.getSelectedTab().equals(installedTab)) {
				content.add(this.overrideView);
			}
			else if (event.getSelectedTab().equals(availableTab)) {
				content.add(this.weasisRepositoryView);
			}
		});

		// Show the view override by default
		content.add(this.overrideView);
		content.setSizeFull();

		packageLayout.add(tabs, content);
	}

}
