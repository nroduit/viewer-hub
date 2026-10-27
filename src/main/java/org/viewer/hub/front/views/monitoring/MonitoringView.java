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

package org.viewer.hub.front.views.monitoring;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import org.viewer.hub.back.model.monitoring.ActivityDay;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.annotation.Secured;
import org.vaadin.lineawesome.LineAwesomeIconUrl;
import org.viewer.hub.back.entity.DisplayRequestMonitoringEntity;
import org.viewer.hub.back.enums.ViewerType;
import org.viewer.hub.back.config.properties.ConnectorConfigurationProperties;
import org.viewer.hub.back.model.monitoring.ArchiveStatistics;
import org.viewer.hub.back.model.monitoring.DashboardData;
import org.viewer.hub.back.model.monitoring.DisplayRequestMonitoringFilter;
import org.viewer.hub.back.model.monitoring.ViewerStatistics;
import org.viewer.hub.back.model.property.ConnectorProperty;
import org.viewer.hub.back.service.DisplayRequestMonitoringService;
import org.viewer.hub.front.views.AbstractView;
import org.viewer.hub.front.views.monitoring.component.RequestsChart;

import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Monitoring of the viewer launch requests: activity (detail of each request) and
 * dashboard (statistics by viewer and chart). Reserved to the role admin.
 */
@PageTitle(MonitoringView.VIEW_NAME)
@Route(MonitoringView.ROUTE)
@Menu(order = 2, icon = LineAwesomeIconUrl.CHART_PIE_SOLID)
@Secured({ "ROLE_admin" })
public class MonitoringView extends AbstractView implements BeforeEnterObserver {

	public static final String VIEW_NAME = "Monitoring";

	public static final String ROUTE = "monitoring";

	private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

	private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

	private static final String ALL = "All";

	private static final String CUSTOM = "Custom range";

	private static final String ANY_STATUS = "Any status";

	private static final String SUCCESS = "Success";

	private static final String ERROR = "Error";

	private static final String TAB_PARAM = "tab";

	private static final String DASHBOARD_TAB = "dashboard";

	/** Quick ranges: label and duration (null = no time limit) */
	private enum QuickRange {

		LAST_15_MIN("Last 15 minutes", Duration.ofMinutes(15)), LAST_30_MIN("Last 30 minutes", Duration.ofMinutes(30)),
		LAST_HOUR("Last hour", Duration.ofHours(1)), LAST_6_HOURS("Last 6 hours", Duration.ofHours(6)),
		LAST_24_HOURS("Last 24 hours", Duration.ofHours(24)), LAST_7_DAYS("Last 7 days", Duration.ofDays(7)),
		LAST_30_DAYS("Last 30 days", Duration.ofDays(30)), ALL_TIME("All time", null), CUSTOM_RANGE(CUSTOM, null);

		private final String label;

		private final Duration duration;

		QuickRange(String label, Duration duration) {
			this.label = label;
			this.duration = duration;
		}

	}

	private final transient DisplayRequestMonitoringService displayRequestMonitoringService;

	// Filters
	private final Select<QuickRange> quickRange = new Select<>();

	private final DatePicker fromPicker = new DatePicker("From");

	private final DatePicker toPicker = new DatePicker("To");

	private final MultiSelectComboBox<String> viewers = new MultiSelectComboBox<>("Viewers");

	private final TextField user = new TextField("User");

	private final TextField patientId = new TextField("Patient ID");

	private final TextField studyUid = new TextField("Study UID");

	private final TextField seriesUid = new TextField("Series UID");

	private final TextField objectUid = new TextField("Instance UID");

	private final TextField accessionNumber = new TextField("Accession number");

	private final ComboBox<String> archive = new ComboBox<>("Archive");

	private final TextField host = new TextField("Host");

	private final TextField client = new TextField("Client");

	private final Select<String> queryLevel = new Select<>();

	private final Select<String> status = new Select<>();

	// Content
	private final TabSheet tabSheet = new TabSheet();

	private final TreeGrid<ActivityNode> activityGrid = new TreeGrid<>();

	private final Span activityCount = new Span();

	private final Grid<ViewerStatistics> viewerStatisticsGrid = new Grid<>(ViewerStatistics.class, false);

	private final Grid<ArchiveStatistics> archiveStatisticsGrid = new Grid<>(ArchiveStatistics.class, false);

	private final RequestsChart requestsChart = new RequestsChart();

	private final FlexLayout kpis = new FlexLayout();

	// Filter applied by the last search (the filter used by the grid, the dashboard and
	// the
	// exports)
	private transient DisplayRequestMonitoringFilter appliedFilter = new DisplayRequestMonitoringFilter();

	private LocalDateTime relativeFrom;

	private boolean updatingRange;

	@Autowired
	public MonitoringView(DisplayRequestMonitoringService displayRequestMonitoringService,
			ConnectorConfigurationProperties connectorConfigurationProperties) {
		this.displayRequestMonitoringService = displayRequestMonitoringService;
		this.archive.setItems(connectorConfigurationProperties.getConnectors()
			.values()
			.stream()
			.map(ConnectorProperty::getId)
			.sorted()
			.toList());
		this.archive.setClearButtonVisible(true);

		this.buildFilters();
		this.buildActivityGrid();
		this.buildViewerStatisticsGrid();
		this.buildArchiveStatisticsGrid();

		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.add(this.buildFilterPanel(), this.buildTabs(), this.buildToolbar());
		layout.setFlexGrow(1, this.tabSheet);
		this.add(layout);
		this.setSizeFull();

		this.search();
	}

	private void buildFilters() {
		this.quickRange.setLabel("Period");
		this.quickRange.setItems(QuickRange.values());
		this.quickRange.setItemLabelGenerator(range -> range.label);
		this.quickRange.setValue(QuickRange.LAST_24_HOURS);
		this.quickRange.addValueChangeListener(e -> {
			this.applyQuickRange();
			if (e.getValue() != QuickRange.CUSTOM_RANGE) {
				this.clearDates();
			}
		});
		DatePicker.DatePickerI18n dateFormat = new DatePicker.DatePickerI18n().setDateFormat("dd/MM/yyyy");
		this.fromPicker.setI18n(dateFormat);
		this.toPicker.setI18n(dateFormat);
		this.fromPicker.addValueChangeListener(e -> this.markCustomRange(e.isFromClient()));
		this.toPicker.addValueChangeListener(e -> this.markCustomRange(e.isFromClient()));

		this.applyQuickRange();

		this.viewers.setItems(Arrays.stream(ViewerType.values()).map(ViewerType::getCode).toList());
		this.viewers.setPlaceholder(ALL);

		this.queryLevel.setLabel("Query level");
		this.queryLevel.setItems(ALL, "PATIENT", "STUDY", "SERIES", "INSTANCE");
		this.queryLevel.setValue(ALL);

		this.status.setLabel("Status");
		this.status.setItems(ANY_STATUS, SUCCESS, ERROR);
		this.status.setValue(ANY_STATUS);

		this.user.setPlaceholder("JWT user or request user");
		for (TextField field : Arrays.asList(this.user, this.patientId, this.studyUid, this.seriesUid, this.objectUid,
				this.accessionNumber, this.host, this.client)) {
			field.setClearButtonVisible(true);
			field.addKeyPressListener(Key.ENTER, e -> this.search());
		}
	}

	private Component buildToolbar() {
		Button refresh = new Button("Refresh", VaadinIcon.REFRESH.create(), e -> this.search());
		refresh.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

		Button deleteAll = new Button("Delete all", VaadinIcon.TRASH.create(), e -> this.confirmDeleteAll());
		deleteAll.getElement().getThemeList().add("error primary");

		// The export depends on the active tab: activity => requests, dashboard =>
		// statistics
		Anchor exportCsv = this.createExportAnchor("Export CSV", "csv",
				this.displayRequestMonitoringService::exportActivityCsv,
				this.displayRequestMonitoringService::exportDashboardCsv);
		Anchor exportJson = this.createExportAnchor("Export JSON", "json",
				this.displayRequestMonitoringService::exportActivityJson,
				this.displayRequestMonitoringService::exportDashboardJson);

		HorizontalLayout toolbar = new HorizontalLayout(refresh, exportCsv, exportJson, deleteAll);
		toolbar.setWidthFull();
		toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
		// Buttons share the full width
		toolbar.getChildren().forEach(c -> {
			toolbar.setFlexGrow(1, c);
			((HasSize) c).setWidth("0");
			((HasSize) c).setMinWidth("0");
		});
		return toolbar;
	}

	private Anchor createExportAnchor(String label, String extension,
			Function<DisplayRequestMonitoringFilter, byte[]> activity,
			Function<DisplayRequestMonitoringFilter, byte[]> dashboard) {
		DownloadHandler handler = DownloadHandler.fromInputStream(event -> {
			boolean activityTab = this.tabSheet.getSelectedIndex() == 0;
			byte[] content = (activityTab ? activity : dashboard).apply(this.appliedFilter);
			String contentType = "csv".equals(extension) ? "text/csv" : "application/json";
			return new DownloadResponse(new ByteArrayInputStream(content), "monitoring." + extension, contentType,
					content.length);
		});
		Button button = new Button(label, VaadinIcon.DOWNLOAD.create());
		button.setWidthFull();
		Anchor anchor = new Anchor(handler, "");
		anchor.add(button);
		anchor.getElement().setAttribute("download", true);
		return anchor;
	}

	private Component buildFilterPanel() {
		HorizontalLayout period = new HorizontalLayout(this.quickRange, this.fromPicker, this.toPicker);
		period.setSpacing(true);
		period.setPadding(false);
		period.setAlignItems(FlexComponent.Alignment.END);
		period.setSpacing(false);
		period.getStyle().set("gap", "0.5rem");
		this.quickRange.setWidth("9rem");
		this.fromPicker.setWidth("8rem");
		this.toPicker.setWidth("8rem");
		this.viewers.setWidth("12rem");
		this.archive.setWidth("10rem");
		this.status.setWidth("7rem");

		// Additional filters, hidden until "More filters" is checked
		FormLayout moreForm = new FormLayout();
		moreForm.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("500px", 2),
				new FormLayout.ResponsiveStep("800px", 4), new FormLayout.ResponsiveStep("1100px", 5));
		moreForm.add(this.user, this.queryLevel, this.patientId, this.studyUid, this.seriesUid, this.objectUid,
				this.accessionNumber, this.host, this.client);
		moreForm.setVisible(false);
		moreForm.getStyle().set("margin-top", "0.5rem");

		Checkbox moreFilters = new Checkbox("More filters");
		moreFilters.addValueChangeListener(e -> moreForm.setVisible(e.getValue()));

		Button search = new Button("Search", VaadinIcon.SEARCH.create(), e -> this.search());
		search.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		Button reset = new Button("Reset", VaadinIcon.CLOSE_SMALL.create(), e -> this.resetFilters());
		HorizontalLayout actions = new HorizontalLayout(search, reset, moreFilters);
		actions.setPadding(false);
		actions.setAlignItems(FlexComponent.Alignment.CENTER);

		HorizontalLayout form = new HorizontalLayout(period, this.viewers, this.archive, this.status, actions);
		form.setPadding(false);
		form.setSpacing(false);
		form.setWidthFull();
		form.setAlignItems(FlexComponent.Alignment.END);
		form.getStyle().set("flex-wrap", "wrap").set("gap", "0.5rem");
		// The filters share the whole width, the buttons keep their size
		period.setFlexGrow(1, this.quickRange, this.fromPicker, this.toPicker);
		period.setWidth("auto");
		form.setFlexGrow(3, period);
		form.setFlexGrow(1, this.viewers, this.archive, this.status);
		form.setFlexGrow(0, actions);

		// Compact filters: small size for all the fields and buttons
		Stream
			.of(this.quickRange, this.fromPicker, this.toPicker, this.viewers, this.user, this.patientId, this.studyUid,
					this.seriesUid, this.objectUid, this.accessionNumber, this.archive, this.queryLevel, this.status,
					this.host, this.client, search, reset)
			.forEach(c -> c.getElement().getThemeList().add("small"));

		VerticalLayout panel = new VerticalLayout(form, moreForm);
		panel.setPadding(false);
		panel.setSpacing(false);
		panel.setWidthFull();
		return panel;
	}

	private Component buildTabs() {
		this.tabSheet.add(new Tab(VaadinIcon.LIST.create(), new Span("Activity")), this.buildActivityTab());
		this.tabSheet.add(new Tab(VaadinIcon.DASHBOARD.create(), new Span("Dashboard")), this.buildDashboardTab());
		this.tabSheet.setSizeFull();
		// Keep the selected tab in the URL so that a browser refresh restores it
		this.tabSheet.addSelectedChangeListener(e -> this.getUI()
			.ifPresent(ui -> ui.getPage()
				.getHistory()
				.replaceState(null, new Location(ROUTE, this.tabSheet.getSelectedIndex() == 1
						? QueryParameters.simple(Map.of(TAB_PARAM, DASHBOARD_TAB)) : QueryParameters.empty()))));
		return this.tabSheet;
	}

	private Component buildActivityTab() {
		VerticalLayout tab = new VerticalLayout(this.activityCount, this.activityGrid);
		tab.setSizeFull();
		tab.setPadding(false);
		tab.setFlexGrow(1, this.activityGrid);
		return tab;
	}

	private Component buildDashboardTab() {
		this.kpis.setFlexWrap(FlexLayout.FlexWrap.WRAP);
		this.kpis.getStyle().set("gap", "1rem");

		H3 chartTitle = new H3("Requests over time");
		chartTitle.getStyle().set("margin-top", "2rem").set("margin-bottom", "0");
		H3 viewersTitle = new H3("Requests by viewer");
		H3 archivesTitle = new H3("Requests by archive");

		VerticalLayout tab = new VerticalLayout(this.kpis, viewersTitle, this.viewerStatisticsGrid, archivesTitle,
				this.archiveStatisticsGrid, chartTitle, this.requestsChart);
		tab.setPadding(false);
		tab.setSizeFull();
		tab.setFlexGrow(1, this.requestsChart);
		return tab;
	}

	private void buildActivityGrid() {
		this.activityGrid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);
		this.activityGrid.setHeight("600px");
		// Left bar of the request rows: green on success, red on error
		this.activityGrid.setPartNameGenerator(
				n -> n.request() == null ? null : n.request().isSuccess() ? "request-success" : "request-error");

		// Hierarchy: one parent row per day, the requests of the day as children
		this.activityGrid
			.addHierarchyColumn(
					n -> n.request() == null ? n.day().format(DAY_FORMAT) + " (" + n.count() + " request(s))"
							: n.request().getCreatedAt().format(TIME_FORMAT))
			.setHeader("Date")
			.setSortProperty("createdAt")
			.setWidth("14rem")
			.setFlexGrow(0);
		this.activityColumn("Viewer", "viewer", 1, DisplayRequestMonitoringEntity::getViewer)
			.setWidth("7rem")
			.setFlexGrow(0);
		this.activityTextColumn("User", "jwtUser", 2, r -> {
			String jwt = StringUtils.trimToNull(r.getJwtUser());
			String req = StringUtils.trimToNull(r.getRequestUser());
			if (jwt != null && req != null) {
				return jwt.equals(req) ? jwt + " (JWT, request)" : jwt + " (JWT) / " + req + " (request)";
			}
			if (jwt != null) {
				return jwt + " (JWT)";
			}
			return req == null ? "" : req + " (request)";
		});
		this.buildCriteriaColumn();
		this.activityTextColumn("Archive", "archive", 2, DisplayRequestMonitoringEntity::getArchive);
		this.activityTextColumn("Client", "client", 2, DisplayRequestMonitoringEntity::getClient);
		this.activityGrid
			.addComponentColumn(n -> n.request() == null ? new Span()
					: stateIcon(n.request().isAuthenticated(), VaadinIcon.LOCK, VaadinIcon.UNLOCK, "Authenticated",
							"Not authenticated"))
			.setHeader("Authenticated")
			.setTextAlign(ColumnTextAlign.CENTER)
			.setSortProperty("authenticated")
			.setWidth("9rem")
			.setFlexGrow(0);
		this.activityColumn("Duration", "durationMs", 1, r -> formatDuration(r.getDurationMs()))
			.setWidth("6rem")
			.setFlexGrow(0);
		this.activityGrid
			.addComponentColumn(n -> n.request() == null ? new Span()
					: stateIcon(n.request().isSuccess(), VaadinIcon.CHECK_CIRCLE, VaadinIcon.CLOSE_CIRCLE, SUCCESS,
							ERROR))
			.setHeader("Status")
			.setTextAlign(ColumnTextAlign.CENTER)
			.setSortProperty("success")
			.setWidth("6rem")
			.setFlexGrow(0);

		this.activityGrid.setItemDetailsRenderer(new com.vaadin.flow.data.renderer.ComponentRenderer<>(node -> {
			DisplayRequestMonitoringEntity request = node.request();
			Div details = new Div();
			if (request == null) {
				return details;
			}
			details.getStyle().set("padding", "0.5rem").set("white-space", "pre-wrap").set("word-break", "break-all");
			Map<String, String> fields = new LinkedHashMap<>();
			fields.put("Method", request.getHttpMethod());
			fields.put("Type", request.getRequestType());
			fields.put("Level", request.getQueryLevel());
			fields.put("User (JWT)", request.getJwtUser());
			fields.put("User (request)", request.getRequestUser());
			fields.put("Patient ID", request.getPatientId());
			fields.put("Study UID", request.getStudyUid());
			fields.put("Series UID", request.getSeriesUid());
			fields.put("Instance UID", request.getObjectUid());
			fields.put("Accession", request.getAccessionNumber());
			fields.put("Archive", request.getArchive());
			fields.put("Host", request.getHost());
			fields.put("Client", request.getClient());
			fields.put("Selection rule", request.getSelectionRule());
			fields.put("Error", request.getErrorMessage());
			fields.forEach((label, value) -> {
				if (StringUtils.isNotBlank(value)) {
					Div line = new Div();
					Span name = new Span(label + ": ");
					name.getStyle().set("font-weight", "600");
					line.add(name, new Span(value));
					details.add(line);
				}
			});
			return details;
		}));
	}

	/** Row of the activity tree: a day (request == null) or one request of a day */
	private record ActivityNode(LocalDate day, long count, DisplayRequestMonitoringEntity request) {
	}

	/** Centered icon with a tooltip: green when on, red when off */
	private static Component stateIcon(boolean on, VaadinIcon onIcon, VaadinIcon offIcon, String onTitle,
			String offTitle) {
		com.vaadin.flow.component.icon.Icon icon = (on ? onIcon : offIcon).create();
		icon.setSize("1.1em");
		icon.getElement().setAttribute("title", on ? onTitle : offTitle);
		icon.getStyle().set("color", on ? "var(--aura-green)" : "var(--aura-red)");
		Div cell = new Div(icon);
		cell.getStyle().set("display", "flex").set("justify-content", "center");
		return cell;
	}

	/**
	 * One column with the search criteria, one labelled line per criterion, fully visible
	 */
	private void buildCriteriaColumn() {
		this.activityGrid.addComponentColumn(n -> {
			Div cell = new Div();
			if (n.request() == null) {
				return cell;
			}
			DisplayRequestMonitoringEntity r = n.request();
			this.addCriterion(cell, "Patient ID", r.getPatientId());
			this.addCriterion(cell, "Accession", r.getAccessionNumber());
			this.addCriterion(cell, "Study UID", r.getStudyUid());
			this.addCriterion(cell, "Series UID", r.getSeriesUid());
			this.addCriterion(cell, "Instance UID", r.getObjectUid());
			return cell;
		}).setHeader("Identifiers").setSortProperty("studyUid").setWidth("0").setFlexGrow(10).setResizable(true);
	}

	private void addCriterion(Div cell, String label, String value) {
		if (StringUtils.isBlank(value)) {
			return;
		}
		Span name = new Span(label + ": ");
		name.getStyle().set("font-weight", "600");
		Div line = new Div(name, new Span(value));
		line.getStyle().set("overflow-wrap", "anywhere");
		cell.add(line);
	}

	/**
	 * Long values (several UIDs...) are cut with "..." in the cell, the full value is in
	 * the row details
	 */
	private void activityTextColumn(String header, String sortProperty,
			Function<DisplayRequestMonitoringEntity, String> value) {
		this.activityTextColumn(header, sortProperty, 2, value);
	}

	private Grid.Column<ActivityNode> activityTextColumn(String header, String sortProperty, int weight,
			Function<DisplayRequestMonitoringEntity, String> value) {
		return this.activityGrid.addComponentColumn(n -> {
			Span text = new Span();
			if (n.request() != null) {
				String content = StringUtils.defaultString(value.apply(n.request()));
				text.setText(content);
				text.getElement().setAttribute("title", content);
			}
			text.getStyle()
				.set("display", "block")
				.set("overflow", "hidden")
				.set("text-overflow", "ellipsis")
				.set("white-space", "nowrap");
			return text;
			// Columns share the available width (no horizontal scrollbar), the text is
			// cut with "..."
		}).setHeader(header).setSortProperty(sortProperty).setWidth("0").setFlexGrow(weight).setResizable(true);
	}

	private void activityColumn(String header, String sortProperty,
			Function<DisplayRequestMonitoringEntity, Object> value) {
		this.activityColumn(header, sortProperty, 2, value);
	}

	private Grid.Column<ActivityNode> activityColumn(String header, String sortProperty, int weight,
			Function<DisplayRequestMonitoringEntity, Object> value) {
		return this.activityTextColumn(header, sortProperty, weight, r -> {
			Object v = value.apply(r);
			return v == null ? "" : v.toString();
		});
	}

	private void buildViewerStatisticsGrid() {
		this.viewerStatisticsGrid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);
		this.viewerStatisticsGrid.setAllRowsVisible(true);
		this.viewerStatisticsGrid.addColumn(ViewerStatistics::viewer).setHeader("Viewer").setAutoWidth(true);
		this.viewerStatisticsGrid.addColumn(ViewerStatistics::requests).setHeader("Requests").setAutoWidth(true);
		this.viewerStatisticsGrid.addColumn(ViewerStatistics::errors).setHeader("Errors").setAutoWidth(true);
		this.viewerStatisticsGrid.addColumn(s -> format("%.1f %%", s.errorRatePercent()))
			.setHeader("Error rate")
			.setAutoWidth(true);
		this.viewerStatisticsGrid.addColumn(s -> formatDuration(s.averageDurationMs()))
			.setHeader("Avg duration")
			.setAutoWidth(true);
		this.viewerStatisticsGrid.addColumn(s -> formatDuration(s.maxDurationMs()))
			.setHeader("Max duration")
			.setAutoWidth(true);
		this.viewerStatisticsGrid.addColumn(ViewerStatistics::distinctUsers)
			.setHeader("Distinct users")
			.setAutoWidth(true);
		this.viewerStatisticsGrid.addColumn(s -> s.lastRequest() != null ? s.lastRequest().format(DATE_TIME) : "-")
			.setHeader("Last request")
			.setAutoWidth(true);
	}

	private void buildArchiveStatisticsGrid() {
		this.archiveStatisticsGrid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);
		this.archiveStatisticsGrid.setAllRowsVisible(true);
		this.archiveStatisticsGrid.addColumn(ArchiveStatistics::archive).setHeader("Archive").setAutoWidth(true);
		this.archiveStatisticsGrid.addColumn(ArchiveStatistics::requests).setHeader("Requests").setAutoWidth(true);
		this.archiveStatisticsGrid.addColumn(ArchiveStatistics::errors).setHeader("Errors").setAutoWidth(true);
		this.archiveStatisticsGrid.addColumn(s -> format("%.1f %%", s.errorRatePercent()))
			.setHeader("Error rate")
			.setAutoWidth(true);
		this.archiveStatisticsGrid.addColumn(s -> formatDuration(s.averageDurationMs()))
			.setHeader("Avg duration")
			.setAutoWidth(true);
		this.archiveStatisticsGrid.addColumn(s -> formatDuration(s.maxDurationMs()))
			.setHeader("Max duration")
			.setAutoWidth(true);
		this.archiveStatisticsGrid.addColumn(ArchiveStatistics::distinctUsers)
			.setHeader("Distinct users")
			.setAutoWidth(true);
		this.archiveStatisticsGrid.addColumn(s -> s.lastRequest() != null ? s.lastRequest().format(DATE_TIME) : "-")
			.setHeader("Last request")
			.setAutoWidth(true);
	}

	/** Apply the filters of the form, then reload the activity and the dashboard */
	private void search() {
		// Relative period: recompute it from "now"
		this.applyQuickRange();
		this.appliedFilter = this.readFilter();
		DisplayRequestMonitoringFilter filter = this.appliedFilter;

		List<ActivityDay> days = this.displayRequestMonitoringService.activityDays(filter);
		this.activityGrid.setDataProvider(new AbstractBackEndHierarchicalDataProvider<ActivityNode, Void>() {
			@Override
			public int getChildCount(HierarchicalQuery<ActivityNode, Void> query) {
				ActivityNode parent = query.getParent();
				return parent == null ? days.size() : (int) Math.min(Integer.MAX_VALUE,
						MonitoringView.this.displayRequestMonitoringService.count(dayFilter(filter, parent.day())));
			}

			@Override
			protected Stream<ActivityNode> fetchChildrenFromBackEnd(HierarchicalQuery<ActivityNode, Void> query) {
				ActivityNode parent = query.getParent();
				if (parent == null) {
					// Days follow the direction of the sort on the date (newest first by
					// default)
					boolean ascending = query.getSortOrders()
						.stream()
						.anyMatch(
								o -> "createdAt".equals(o.getSorted()) && o.getDirection() == SortDirection.ASCENDING);
					List<ActivityDay> ordered = ascending ? days.reversed() : days;
					return ordered.stream()
						.skip(query.getOffset())
						.limit(query.getLimit())
						.map(d -> new ActivityNode(d.day(), d.requests(), null));
				}
				PageRequest page = VaadinSpringDataHelpers.toSpringPageRequest(query);
				if (page.getSort().isUnsorted()) {
					page = page.withSort(Sort.by(Sort.Direction.DESC, "createdAt"));
				}
				return MonitoringView.this.displayRequestMonitoringService.search(dayFilter(filter, parent.day()), page)
					.stream()
					.map(r -> new ActivityNode(parent.day(), 0, r));
			}

			@Override
			public boolean hasChildren(ActivityNode node) {
				return node.request() == null;
			}
		});
		if (!days.isEmpty()) {
			this.activityGrid.expand(new ActivityNode(days.get(0).day(), days.get(0).requests(), null));
		}
		this.activityCount.setText(this.displayRequestMonitoringService.count(filter) + " request(s)");

		DashboardData data = this.displayRequestMonitoringService.dashboard(filter);
		this.updateDashboard(data);
	}

	/** Criteria restricted to one day (and to the period of the filter) */
	private static DisplayRequestMonitoringFilter dayFilter(DisplayRequestMonitoringFilter filter, LocalDate day) {
		DisplayRequestMonitoringFilter dayFilter = filter.copy();
		LocalDateTime dayStart = day.atStartOfDay();
		LocalDateTime dayEnd = day.atTime(LocalTime.MAX);
		if (filter.getFrom() == null || filter.getFrom().isBefore(dayStart)) {
			dayFilter.setFrom(dayStart);
		}
		if (filter.getTo() == null || filter.getTo().isAfter(dayEnd)) {
			dayFilter.setTo(dayEnd);
		}
		return dayFilter;
	}

	private void updateDashboard(DashboardData data) {
		this.kpis.removeAll();
		double errorRate = data.totalRequests() == 0 ? 0 : data.totalErrors() * 100.0 / data.totalRequests();
		this.kpis.add(kpi("Total requests", String.valueOf(data.totalRequests()), false),
				kpi("Errors", data.totalErrors() + " (" + format("%.1f", errorRate) + " %)", data.totalErrors() > 0),
				kpi("Avg duration", formatDuration(data.averageDurationMs()), false),
				kpi("Distinct users", String.valueOf(data.distinctUsers()), false),
				kpi("Authenticated", data.authenticatedRequests() + " / " + data.totalRequests(), false),
				kpi("Viewers used", String.valueOf(data.viewers().size()), false));
		this.requestsChart.update(data);
		this.viewerStatisticsGrid.setItems(data.viewers());
		this.archiveStatisticsGrid.setItems(data.archives());
	}

	/** Milliseconds shown in the most readable unit: ms, s, min or h */
	private static String formatDuration(double ms) {
		if (ms < 1000) {
			return format("%.0f ms", ms);
		}
		if (ms < 60_000) {
			return format("%.1f s", ms / 1000);
		}
		long totalSeconds = Math.round(ms / 1000);
		if (totalSeconds < 3600) {
			return totalSeconds / 60 + " min " + totalSeconds % 60 + " s";
		}
		return totalSeconds / 3600 + " h " + totalSeconds % 3600 / 60 + " min";
	}

	/**
	 * Badge-like card, neutral unless alert is true (red)
	 */
	private static Component kpi(String label, String value, boolean alert) {
		String accent = alert ? "var(--aura-red)" : "color-mix(in srgb, currentColor 40%, transparent)";
		Span labelSpan = new Span(label);
		labelSpan.getStyle()
			.set("color", "color-mix(in srgb, currentColor 65%, transparent)")
			.set("font-size", "var(--aura-font-size-s)")
			.set("font-weight", "500");
		Span valueSpan = new Span(value);
		valueSpan.getStyle()
			.set("font-size", "1.75rem")
			.set("font-weight", "700")
			.set("color", alert ? "var(--aura-red-text)" : "inherit");
		VerticalLayout card = new VerticalLayout(labelSpan, valueSpan);
		card.setSpacing(false);
		card.setWidth("auto");
		card.setMinWidth("12rem");
		card.getStyle()
			.set("background",
					alert ? "color-mix(in srgb, var(--aura-red) 12%, transparent)"
							: "color-mix(in srgb, currentColor 5%, transparent)")
			.set("border",
					"1px solid " + (alert ? "color-mix(in srgb, var(--aura-red) 45%, transparent)"
							: "color-mix(in srgb, currentColor 20%, transparent)"))
			.set("border-left", "6px solid " + accent)
			.set("border-radius", "0.5rem")
			.set("box-shadow", "var(--aura-shadow-xs)");
		return card;
	}

	private DisplayRequestMonitoringFilter readFilter() {
		DisplayRequestMonitoringFilter filter = new DisplayRequestMonitoringFilter();
		if (this.quickRange.getValue() == QuickRange.CUSTOM_RANGE) {
			LocalDate from = this.fromPicker.getValue();
			LocalDate to = this.toPicker.getValue();
			filter.setFrom(from != null ? from.atStartOfDay() : null);
			filter.setTo(to != null ? to.atTime(LocalTime.MAX) : null);
		}
		else {
			filter.setFrom(this.relativeFrom);
		}
		filter.setViewers(this.viewers.getValue());
		filter.setUser(this.user.getValue());
		filter.setPatientId(this.patientId.getValue());
		filter.setStudyUid(this.studyUid.getValue());
		filter.setSeriesUid(this.seriesUid.getValue());
		filter.setObjectUid(this.objectUid.getValue());
		filter.setAccessionNumber(this.accessionNumber.getValue());
		filter.setArchive(this.archive.getValue());
		filter.setHost(this.host.getValue());
		filter.setClient(this.client.getValue());
		filter.setQueryLevel(ALL.equals(this.queryLevel.getValue()) ? null : this.queryLevel.getValue());
		filter.setSuccess(switch (this.status.getValue()) {
			case SUCCESS -> Boolean.TRUE;
			case ERROR -> Boolean.FALSE;
			case null, default -> null;
		});
		return filter;
	}

	private void applyQuickRange() {
		QuickRange range = this.quickRange.getValue();
		this.relativeFrom = (range != null && range.duration != null)
				? LocalDateTime.now(this.displayRequestMonitoringService.retrieveTimeZone()).minus(range.duration)
				: null;
	}

	private void clearDates() {
		this.updatingRange = true;
		try {
			this.fromPicker.clear();
			this.toPicker.clear();
		}
		finally {
			this.updatingRange = false;
		}
	}

	private void markCustomRange(boolean fromClient) {
		if (fromClient && !this.updatingRange) {
			this.quickRange.setValue(QuickRange.CUSTOM_RANGE);
		}
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		List<String> tab = event.getLocation().getQueryParameters().getParameters().get(TAB_PARAM);
		if (tab != null && tab.contains(DASHBOARD_TAB)) {
			this.tabSheet.setSelectedIndex(1);
		}
	}

	private void resetFilters() {
		this.clearDates();
		this.quickRange.setValue(QuickRange.LAST_24_HOURS);
		this.viewers.clear();
		this.queryLevel.setValue(ALL);
		this.status.setValue(ANY_STATUS);
		Arrays
			.asList(this.user, this.patientId, this.studyUid, this.seriesUid, this.objectUid, this.accessionNumber,
					this.host, this.client)
			.forEach(TextField::clear);
		this.archive.clear();
		this.search();
	}

	private void confirmDeleteAll() {
		ConfirmDialog dialog = new ConfirmDialog();
		dialog.setHeader("Delete all statistics");
		dialog.setText("All the recorded requests will be permanently deleted, whatever the current filters. "
				+ "This action cannot be undone.");
		dialog.setCancelable(true);
		dialog.setConfirmText("Delete all");
		dialog.setConfirmButtonTheme("error primary");
		dialog.addConfirmListener(e -> {
			long deleted = this.displayRequestMonitoringService.deleteAll();
			Notification.show(deleted + " request(s) deleted", 5000, Notification.Position.BOTTOM_END)
				.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
			this.search();
		});
		dialog.open();
	}

	private static String format(String pattern, double value) {
		return String.format(Locale.ROOT, pattern, value);
	}

	private static String nullToDash(String value) {
		return value == null || value.isBlank() ? "-" : value;
	}

}