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

package org.weasis.manager.back.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
public class InetUtil {

	private static final List<String> IP_HEADERS = Arrays.asList("X-Forwarded-For", "X_FORWARDED_FOR", "HTTP_CLIENT_IP",
			"HTTP_X_FORWARDED_FOR", "Proxy-Client-IP", "WL-Proxy-Client-IP");

	private InetUtil() {
	}

	/**
	 * @see {@link #getClientHost(HttpServletRequest,Optional, String)} <br>
	 */
	public static String getClientHost(HttpServletRequest request, Optional<String> optHostAddr, String bypass) {
		return optHostAddr.isPresent() ? getClientHostFromAddr(optHostAddr.get(), bypass)
				: getClientHostFromRequest(request);
	}

	/**
	 * Returns HostName from request remote ipAddress
	 *
	 * @see {@link #getClientIpAddr(HttpServletRequest)} <br>
	 * {@link #getClientHostFromAddr(String)}
	 * @param request
	 * @return <b>hostName</b> or <b>null</b> if host cannot be resolved.
	 */
	public static String getClientHostFromRequest(HttpServletRequest request) {
		return getClientHostFromAddr(getClientIpAddr(request));
	}

	/**
	 * Returns original Internet Protocol (IP) address of the client or last proxy that
	 * sent the request even if behind a proxy or a load balancer.
	 *
	 * @see <a href=
	 * "http://stackoverflow.com/questions/4678797/how-do-i-get-the-remote-address-of-a-client-in-servlet">...</a>
	 * @param request
	 * @return <b>non null</b> client IP address
	 */
	public static String getClientIpAddr(HttpServletRequest request) {
		Objects.requireNonNull(request, "HttpServletRequest cannot be null");

		String clientIP = null;
		Iterator<String> it = IP_HEADERS.iterator();
		while (it.hasNext()) {
			String header = it.next();
			Enumeration<String> remoteAddr = request.getHeaders(header);
			if (remoteAddr != null) {
				while (remoteAddr.hasMoreElements()) {
					clientIP = remoteAddr.nextElement(); // get the last element
				}
				if (!(isEmpty(clientIP))) {
					LOG.debug("Client IP is read from Request Header {} with [{}]", header, clientIP);
					break;
				}
			}
		}
		if (isEmpty(clientIP)) {
			clientIP = request.getRemoteAddr();
			LOG.debug("Client IP is read from HttpServlet Request [{}]", clientIP);
		}
		return clientIP;
	}

	/**
	 * Returns HostName from DNS if correctly set on the OS by providing a remote lookup
	 * on literal IP address, if not, the reverse lookup from "host" file will be used
	 * instead. <br>
	 *
	 * Depending on the underlying system configuration best effort method is applied,
	 * meaning a simple textual representation of the IP address may be returned.
	 *
	 * If valid, the found hostName is returned without any unneeded SUFFIX part <br>
	 *
	 * @see {@link #removeFQDN(String)}
	 * @param addr as literal IP address (IPV4/IPV6) or hostName (even in fully qualified
	 * domain name form)
	 * @return <b>hostName</b> or <b>null</b> if host cannot be resolved or bypassed.<br>
	 */
	public static String getClientHostFromAddr(String addr) {
		return getClientHostFromAddr(addr, null);
	}

	/**
	 * @see {@link #getClientHostFromAddr(String)}
	 */
	public static String getClientHostFromAddr(String addr, String bypass) {

		if (isEmpty(addr) || addr.equalsIgnoreCase(bypass))
			return null; // avoids resolving localhost loopback interface when supplied
							// clientIP is null

		String clientHost = null;

		try {
			// If a literal IP address is supplied, only the validity of the address
			// format is checked.
			InetAddress inetAddress = InetAddress.getByName(addr);
			if (inetAddress.isLoopbackAddress())
				clientHost = InetAddress.getLocalHost().getCanonicalHostName();
			else
				clientHost = inetAddress.getCanonicalHostName();

		}
		catch (Exception e) {
			LOG.warn("Can't get CanonicalHostName from IP [{}] : {} ", addr, e.getLocalizedMessage());
		}

		if (!(isEmpty(clientHost))) {
			String clientHostFQDN = clientHost;
			clientHost = removeFQDN(clientHost);
			LOG.debug("Client HOST is [{}] with orginal FQDN form [{}]", clientHost, clientHostFQDN);
		}

		return clientHost;
	}

	/**
	 * Removes the fully qualified domain name suffix <br>
	 *
	 * @note {host-name} would always be like {litteral}-{number} (ex : pc-001 or
	 * mac-001)<br>
	 * But a HostName may also have a suffix like {host-name}-{literal.ip.adress.suffix}
	 * (ex : pc-001-129195220186). This happened when through VPN, or after switching from
	 * wire network to wifi and vice versa. The trick allows to resolve HostName for the
	 * device even if 2 networks connections are simultaneously opened
	 * @param host
	 * @return <b>hostName</b> in its simplest form without any unneeded SUFFIX part
	 */
	private static String removeFQDN(String host) {

		if (isEmpty(host))
			return host;

		int index = host.indexOf('.');
		if (index != -1)
			host = host.substring(0, index); // get part before Domain Name

		int found = 0;
		index = -1;
		do {
			index = host.indexOf('-', index + 1);
			if (index < 0)
				return host;
			found++;
		}
		while (found < 2);

		return host.substring(0, index); // get part before second occurrence of DASH
	}

	private static boolean isEmpty(CharSequence cs) {
		return cs == null || cs.length() == 0;
	}

}
