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

package org.viewer.hub.front.components;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.viewer.hub.back.model.Message;
import org.viewer.hub.back.model.MessageLevel;
import org.viewer.hub.back.model.MessageType;

import java.io.Serial;

/**
 * Un MessageBox peut être affiché sous forme d'une notification ou alors dans une
 * fenêtre.
 */

@JsModule("@polymer/iron-icons/iron-icons.js")
@CssImport(value = "./styles/message-box.css")
public class MessageBox extends Composite<Div> {

	@Serial
	private static final long serialVersionUID = 8311860285796401834L;

	private static final String CLASSNAME_INFO = "info";

	private static final String CLASSNAME_SUCCESS = "success";

	private static final String CLASSNAME_WARN = "warn";

	private static final String CLASSNAME_ERROR = "error";

	private static final String ICONS_COLLECTION = "icons";

	private static final String INFO_OUTLINE_ICON = "info-outline";

	private static final String WARNING_ICON = "warning";

	private static final String ERROR_OUTLINE_ICON = "error-outline";

	public static final int DURATION_MSG_INFO_SUCCESS = 5000;

	public static final int DURATION_MSG_ERROR = 10000;

	// UI COMPONENTS
	private HorizontalLayout layout;

	private Div titleDiv;

	private Div contentDiv;

	private Icon icon;

	// DATA
	private Message message;

	private final MessageType type;

	public MessageBox(MessageType type) {
		this.type = type;

		this.createMessageBox();
		this.createLayout();
		this.createTitleDiv();
		this.createContentDiv();

		this.layout.add(this.titleDiv, this.contentDiv);

		this.getContent().add(this.layout);
	}

	public MessageBox(Message message, MessageType type) {
		this.message = message;
		this.type = type;

		this.createMessageBox();
		this.createLayout();
		this.createTitleDiv();
		this.createContentDiv();

		this.layout.add(this.titleDiv, this.contentDiv);

		this.getContent().add(this.layout);
	}

	public void setMessage(Message message) {
		this.message = message;

		this.updateLayout();
		this.updateTitleDiv();
		this.updateContentDiv();
	}

	private void createMessageBox() {
		this.getContent().addClassName("message-box");
	}

	private void createLayout() {
		this.layout = new HorizontalLayout();
		this.layout.addClassName("message-box-layout");
		if (this.type == MessageType.EMBEDDED_MESSAGE) {
			this.layout.addClassName("message-box-layout-with-margin");
		}

		if (this.message != null) {
			if (MessageLevel.INFO == this.message.getLevel()) {
				this.layout.addClassName(CLASSNAME_INFO);
			}
			else if (MessageLevel.SUCCESS == this.message.getLevel()) {
				this.layout.addClassName(CLASSNAME_SUCCESS);
			}
			else if (MessageLevel.WARN == this.message.getLevel()) {
				this.layout.addClassName(CLASSNAME_WARN);
			}
			else if (MessageLevel.ERROR == this.message.getLevel()) {
				this.layout.addClassName(CLASSNAME_ERROR);
			}
		}
	}

	private void createTitleDiv() {
		this.titleDiv = new Div();
		this.titleDiv.addClassName("message-box-title");

		if (this.message != null) {
			if (MessageLevel.INFO == this.message.getLevel()) {
				this.titleDiv.addClassName(CLASSNAME_INFO);
			}
			else if (MessageLevel.SUCCESS == this.message.getLevel()) {
				this.titleDiv.addClassName(CLASSNAME_SUCCESS);
			}
			else if (MessageLevel.WARN == this.message.getLevel()) {
				this.titleDiv.addClassName(CLASSNAME_WARN);
			}
			else if (MessageLevel.ERROR == this.message.getLevel()) {
				this.titleDiv.addClassName(CLASSNAME_ERROR);
			}

			this.createIcon();
			this.titleDiv.add(this.icon);
		}
	}

	private void createContentDiv() {
		this.contentDiv = new Div();
		this.contentDiv.addClassName("message-box-content");

		if (this.message != null) {
			switch (this.message.getFormat()) {
				case TEXT:
					this.contentDiv.setText(this.message.getText());
					break;
				case HTML:
					this.contentDiv.removeAll();
					this.contentDiv.add(new Html("<span>" + this.message.getText() + "</span>"));
					break;
				default:
					break;
			}
		}
	}

	private void createIcon() {
		if (this.message != null) {
			if (MessageLevel.INFO == this.message.getLevel()) {
				this.icon = new Icon(ICONS_COLLECTION, INFO_OUTLINE_ICON);
			}
			else if (MessageLevel.SUCCESS == this.message.getLevel()) {
				this.icon = new Icon(ICONS_COLLECTION, INFO_OUTLINE_ICON);
			}
			else if (MessageLevel.WARN == this.message.getLevel()) {
				this.icon = new Icon(ICONS_COLLECTION, WARNING_ICON);
			}
			else if (MessageLevel.ERROR == this.message.getLevel()) {
				this.icon = new Icon(ICONS_COLLECTION, ERROR_OUTLINE_ICON);
			}
			else {
				this.icon = new Icon(ICONS_COLLECTION, INFO_OUTLINE_ICON);
			}
		}
	}

	private void updateLayout() {
		if (this.message != null) {
			if (MessageLevel.INFO == this.message.getLevel()) {
				this.layout.addClassName(CLASSNAME_INFO);
			}
			else if (MessageLevel.SUCCESS == this.message.getLevel()) {
				this.layout.addClassName(CLASSNAME_SUCCESS);
			}
			else if (MessageLevel.WARN == this.message.getLevel()) {
				this.layout.addClassName(CLASSNAME_WARN);
			}
			else if (MessageLevel.ERROR == this.message.getLevel()) {
				this.layout.addClassName(CLASSNAME_ERROR);
			}
			else {
				this.layout.addClassName(CLASSNAME_INFO);
			}
		}
	}

	private void updateTitleDiv() {
		if (this.message != null) {
			if (MessageLevel.INFO == this.message.getLevel()) {
				this.titleDiv.addClassName(CLASSNAME_INFO);
			}
			else if (MessageLevel.SUCCESS == this.message.getLevel()) {
				this.titleDiv.addClassName(CLASSNAME_SUCCESS);
			}
			else if (MessageLevel.WARN == this.message.getLevel()) {
				this.titleDiv.addClassName(CLASSNAME_WARN);
			}
			else if (MessageLevel.ERROR == this.message.getLevel()) {
				this.titleDiv.addClassName(CLASSNAME_ERROR);
			}

			this.createIcon();
			this.titleDiv.add(this.icon);
		}
	}

	private void updateContentDiv() {
		if (this.message != null) {
			switch (this.message.getFormat()) {
				case TEXT:
					this.contentDiv.setText(this.message.getText());
					break;
				case HTML:
					this.contentDiv.removeAll();
					this.contentDiv.add(new Html("<span>" + this.message.getText() + "</span>"));
					break;
				default:
					break;
			}
		}
	}

}
