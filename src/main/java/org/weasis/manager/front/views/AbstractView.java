/*
 *  Copyright (c) 2022-2024 Weasis Team and other contributors.
 *
 *  This program and the accompanying materials are made available under the terms of the Eclipse
 *  Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 *  License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *  SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package org.weasis.manager.front.views;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.weasis.manager.back.model.Message;
import org.weasis.manager.back.model.MessageLevel;
import org.weasis.manager.back.model.MessageType;
import org.weasis.manager.front.components.MessageBox;

import java.io.Serial;

@CssImport(value = "./styles/notification-theme.css", themeFor = "vaadin-notification-card")
public abstract class AbstractView extends Div {

	@Serial
	private static final long serialVersionUID = -6416630482130380828L;

	private static final int DURATION_MSG_INFO_SUCCESS = 5000;

	private static final int DURATION_MSG_ERROR = 10000;

	protected VerticalLayout mainLayout;

	private MessageBox messageBox;

	public void displayMessage(Message message, MessageType messageType) {
		this.messageBox = new MessageBox(message, messageType);

		if (messageType == MessageType.NOTIFICATION_MESSAGE) {
			Notification notification = new Notification(this.messageBox);
			notification.setPosition(Position.BOTTOM_END);

			if (message.getLevel() == MessageLevel.INFO || message.getLevel() == MessageLevel.SUCCESS) {
				notification.setDuration(DURATION_MSG_INFO_SUCCESS);
			}
			else if (message.getLevel() == MessageLevel.ERROR) {
				notification.setDuration(DURATION_MSG_ERROR);
			}
			else {
				notification.setDuration(DURATION_MSG_INFO_SUCCESS);
			}

			notification.open();
		}
		else if (messageType == MessageType.EMBEDDED_MESSAGE) {
			this.removeMessage();
			this.mainLayout.addComponentAtIndex(1, this.messageBox);
		}
	}

	public void removeMessage() {
		if (this.messageBox != null) {
			this.mainLayout.remove(this.messageBox);
		}
	}

}
