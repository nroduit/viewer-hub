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

package org.viewer.hub.front.views.monitoring.component;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.html.Div;
import org.viewer.hub.back.model.monitoring.DashboardData;
import org.viewer.hub.back.model.monitoring.TimeBucket;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Stacked bar chart (requests by viewer over the time) drawn in SVG: no external chart
 * library is needed and it follows the Lumo light/dark theme.
 */
public class RequestsChart extends Div {

	private static final String[] PALETTE = { "#1676f3", "#f5a623", "#2eb67d", "#e5484d", "#8e4ec6", "#12a594",
			"#d6409f", "#6e7781" };

	private static final String PLOT_HEIGHT = "100%";

	// Virtual width of the SVG plot area (stretched to the container, no text inside)
	private static final int VIEW_WIDTH = 1000;

	private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

	private static final DateTimeFormatter TIME_SECONDS = DateTimeFormatter.ofPattern("HH:mm:ss");

	private static final DateTimeFormatter DAY_TIME = DateTimeFormatter.ofPattern("dd/MM HH:mm");

	private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("dd/MM");

	private static final DateTimeFormatter FULL = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

	public RequestsChart() {
		setWidthFull();
		setHeightFull();
		setMinHeight("360px");
	}

	public void update(DashboardData data) {
		removeAll();
		List<TimeBucket> timeline = data.timeline();
		if (timeline.isEmpty()) {
			add(new Html(
					"<div style=\"padding:2rem;text-align:center;color:color-mix(in srgb, currentColor 65%, transparent)\">"
							+ "No requests for the selected criteria</div>"));
			return;
		}

		// Stable colors by viewer
		Map<String, String> colors = new TreeMap<>();
		data.viewers().forEach(v -> colors.put(v.viewer(), PALETTE[colors.size() % PALETTE.length]));

		long maxTotal = timeline.stream().mapToLong(TimeBucket::total).max().orElse(1);
		long yMax = niceMax(maxTotal);
		double slot = (double) VIEW_WIDTH / timeline.size();
		double barWidth = Math.max(1, slot * 0.8);

		StringBuilder svg = new StringBuilder();
		svg.append(
				"<svg viewBox=\"0 0 %d 100\" preserveAspectRatio=\"none\" style=\"display:block;width:100%%;height:%s\" role=\"img\" aria-label=\"Requests over time\">"
					.formatted(VIEW_WIDTH, PLOT_HEIGHT));

		// Horizontal grid lines + y labels (HTML so the text keeps the page font size)
		StringBuilder yLabels = new StringBuilder();
		for (int i = 0; i <= 5; i++) {
			double value = yMax * i / 5.0;
			double y = 100 - 20.0 * i;
			svg.append(
					"<line x1=\"0\" x2=\"%d\" y1=\"%s\" y2=\"%s\" stroke=\"color-mix(in srgb, currentColor 20%%, transparent)\" stroke-width=\"1\" vector-effect=\"non-scaling-stroke\"/>"
						.formatted(VIEW_WIDTH, num(y), num(y)));
			yLabels.append("<span style=\"position:absolute;right:0;bottom:%s%%;transform:translateY(50%%)\">%s</span>"
				.formatted(num(20.0 * i), yMax * i % 5 == 0 ? String.valueOf(yMax * i / 5) : num(value)));
		}

		// Bars
		Duration size = data.bucketSize();
		boolean multiDay = !timeline.get(0)
			.start()
			.toLocalDate()
			.equals(timeline.get(timeline.size() - 1).start().toLocalDate());
		for (int i = 0; i < timeline.size(); i++) {
			TimeBucket bucket = timeline.get(i);
			double x = i * slot + (slot - barWidth) / 2;
			double yBottom = 100;
			StringBuilder tooltip = new StringBuilder(formatRange(bucket.start(), size)).append(" - ")
				.append(bucket.total())
				.append(" requests");
			bucket.requestsByViewer().forEach((v, n) -> tooltip.append("\n").append(v).append(": ").append(n));

			for (Map.Entry<String, String> viewer : colors.entrySet()) {
				long requests = bucket.requestsByViewer().getOrDefault(viewer.getKey(), 0L);
				if (requests == 0) {
					continue;
				}
				double height = 100.0 * requests / yMax;
				yBottom -= height;
				svg.append("<rect x=\"%s\" y=\"%s\" width=\"%s\" height=\"%s\" fill=\"%s\"><title>%s</title></rect>"
					.formatted(num(x), num(yBottom), num(barWidth), num(height), viewer.getValue(),
							escape(tooltip.toString())));
			}
		}
		svg.append("</svg>");

		// X labels: about 10 labels maximum
		StringBuilder xLabels = new StringBuilder();
		int step = Math.max(1, (int) Math.ceil(timeline.size() / 10.0));
		for (int i = 0; i < timeline.size(); i += step) {
			double left = (i * slot + slot / 2) * 100.0 / VIEW_WIDTH;
			xLabels.append(
					"<span style=\"position:absolute;left:%s%%;transform:translateX(-50%%);white-space:nowrap\">%s</span>"
						.formatted(num(left), formatAxis(timeline.get(i).start(), size, multiDay)));
		}

		String chart = "<div style=\"flex:1 1 auto;min-height:0;display:grid;grid-template-columns:auto 1fr;grid-template-rows:1fr auto;column-gap:8px;padding-right:1.5em;font-size:var(--aura-font-size-xs);color:color-mix(in srgb, currentColor 65%, transparent)\">"
				+ "<div style=\"position:relative;height:" + PLOT_HEIGHT + ";min-width:2.5em\">" + yLabels + "</div>"
				+ svg + "<div></div>" + "<div style=\"position:relative;height:1.5em;margin-top:4px\">" + xLabels
				+ "</div></div>";
		// Legend
		StringBuilder legend = new StringBuilder(
				"<div style=\"display:flex;gap:1rem;flex-wrap:wrap;justify-content:center;font-size:var(--aura-font-size-s)\">");
		colors.forEach((viewer, color) -> legend.append(
				"<span><span style=\"display:inline-block;width:10px;height:10px;border-radius:2px;margin-right:4px;background:%s\"></span>%s</span>"
					.formatted(color, escape(viewer))));
		legend.append("</div>");

		add(new Html("<div style=\"display:flex;flex-direction:column;width:100%;height:100%\">" + chart + legend
				+ "</div>"));
	}

	private static String formatRange(LocalDateTime start, Duration size) {
		LocalDateTime end = start.plus(size);
		String endText = end.toLocalDate().equals(start.toLocalDate()) ? TIME_SECONDS.format(end) : FULL.format(end);
		return FULL.format(start) + " -> " + endText;
	}

	private static String formatAxis(LocalDateTime start, Duration size, boolean multiDay) {
		if (size.compareTo(Duration.ofDays(1)) >= 0) {
			return DAY.format(start);
		}
		return (multiDay ? DAY_TIME : TIME).format(start);
	}

	/** Round up to a value giving readable ticks (multiple of 5 for the 5 intervals) */
	private static long niceMax(long max) {
		if (max <= 5) {
			return 5;
		}
		for (long magnitude = 1;; magnitude *= 10) {
			for (long factor : new long[] { 1, 2, 5 }) {
				long step = magnitude * factor;
				if (step * 5 >= max) {
					return step * 5;
				}
			}
		}
	}

	/** Locale-independent number: a decimal comma would break the SVG attributes */
	private static String num(double value) {
		return String.format(java.util.Locale.ROOT, "%.1f", value);
	}

	private static String escape(String value) {
		return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
	}

}