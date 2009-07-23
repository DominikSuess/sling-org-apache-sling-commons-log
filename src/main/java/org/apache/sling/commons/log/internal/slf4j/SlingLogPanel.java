/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sling.commons.log.internal.slf4j;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class SlingLogPanel extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static ServiceRegistration panelRegistration;

    public static void registerPanel(BundleContext ctx) {
        if (panelRegistration == null) {
            Dictionary<String, Object> props = new Hashtable<String, Object>();
            props.put("felix.webconsole.label", "slinglog");
            props.put("felix.webconsole.title", "Sling Log Support");

            SlingLogPanel panel = new SlingLogPanel();
            panelRegistration = ctx.registerService("javax.servlet.Servlet",
                panel, props);
        }
    }

    public static void unregisterPanel() {
        if (panelRegistration != null) {
            panelRegistration.unregister();
            panelRegistration = null;
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        final PrintWriter pw = resp.getWriter();
        final LogConfigManager logConfigManager = LogConfigManager.getInstance();

        pw.println("<div class='table'>");

        pw.println("<h1>Logger</h1>");

        pw.println("<table class='tablelayout'>");

        pw.println("<thead>");
        pw.println("</tr>");
        pw.println("<tr>");
        pw.println("<th>PID</th>");
        pw.println("<th>Log Level</th>");
        pw.println("<th>Log File</th>");
        pw.println("<th>Logger</th>");
        pw.println("</tr>");
        pw.println("</thead>");
        pw.println("<tbody>");

        Iterator<SlingLoggerConfig> loggers = logConfigManager.getSlingLoggerConfigs();
        while (loggers.hasNext()) {
            final SlingLoggerConfig logger = loggers.next();
            pw.println("<tr>");
            pw.println("<td>" + logger.getConfigPid() + "</td>");
            pw.println("<td>" + logger.getLogLevel() + "</td>");
            pw.println("<td>" + getPath(logger.getLogWriter()) + "</td>");

            pw.println("<td>");
            String sep = "";
            for (String cat : logger.getCategories()) {
                pw.print(sep);
                pw.println(cat);
                sep = "<br />";
            }
            pw.println("</td>");
            pw.println("</tr>");
        }

        pw.println("</tbody>");
        pw.println("</table>");
        pw.println("</div>");

        pw.println("<div class='table'>");

        pw.println("<h1>Log Writer</h1>");

        pw.println("<table class='tablelayout'>");

        pw.println("<thead>");
        pw.println("<tr>");
        pw.println("<th>PID</th>");
        pw.println("<th>Log File</th>");
        pw.println("<th>Rotator</th>");
        pw.println("</tr>");
        pw.println("</thead>");
        pw.println("<tbody>");

        Iterator<SlingLoggerWriter> writers = logConfigManager.getSlingLoggerWriters();
        while (writers.hasNext()) {
            final SlingLoggerWriter writer = writers.next();
            final String pid = (writer.getConfigurationPID() != null)
                    ? writer.getConfigurationPID()
                    : "[implicit]";
            pw.println("<tr>");
            pw.println("<td>" + pid + "</td>");
            pw.println("<td>" + getPath(writer) + "</td>");
            pw.println("<td>" + writer.getFileRotator() + "</td>");
            pw.println("</tr>");
        }

        pw.println("</tbody>");
        pw.println("</table>");
        pw.println("</div>");
    }

    private static String getPath(SlingLoggerWriter writer) {
        final String path = writer.getPath();
        return (path != null) ? path : "[stdout]";
    }
}
