package com.vegaasen.testing.spring3.run;

import com.vegaasen.testing.spring3.Config;
import com.vegaasen.testing.spring3.config.ServletInitializer;
import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.annotations.ClassInheritanceHandler;
import org.eclipse.jetty.plus.webapp.EnvConfiguration;
import org.eclipse.jetty.plus.webapp.PlusConfiguration;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Credential;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.*;
import org.springframework.web.WebApplicationInitializer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StartJetty {

    private static final Logger LOG = Logger.getLogger(StartJetty.class.getName());
    private static final String[] USER_ROLES = new String[]{"user"};
    private static final String ROOT = "/";
    public static final String DEFAULT_PROTOCOL = "http/1.1";

    private static int httpControlPort = Config.getControlPort();
    private static int httpPort = Config.getHttpPort();
    private static int httpsPort = Config.getHttpsPort();
    private static Server webAppServer;
    private static Server controlServer;
    //private static ApplicationContext applicationContext;

    private StartJetty() {
    }

    /**
     * Start the application with the following command (example):
     * JettyControl.java start -Dconfig.path=/home/t769765/develop/tss/telenor-security-server/dist/env.local.t769765.test.ast-win06/etc/tss.linux.properties
     *
     * @param args _
     */
    public static void main(final String[] args) {
        if (args.length == 0) {
            System.err.println(
                    "\r\nUsage: tss start\r\n"
                            + "       tss stop\r\n"
                            + "       tss jetty-hash password\r\n"
                            + "       tss prop-encrypt master-password value\r\n"
                            + "       tss prop-decrypt master-password encrypted-value\r\n"
            );
            return;
        }
        final String urlCharacterSet = "UTF-8";
        /* Jetty GET requests */
        System.setProperty("org.mortbay.util.URI.charset", urlCharacterSet);
        /* Jetty POST requests */
        System.setProperty("org.mortbay.util.UrlEncoding.charset", urlCharacterSet);
        //applicationContext = new AnnotationConfigApplicationContext("com.vegaasen.testing.spring3");//new ClassPathXmlApplicationContext(new String[]{"applicationContext.xml"});
        final String command = args[0];
        final String[] commandArguments = new String[args.length - 1];
        System.arraycopy(args, 1, commandArguments, 0, commandArguments.length);
        if ("start".equals(command)) {
            commandStart(command, commandArguments);
        } else if ("stop".equals(command)) {
            commandStop(command, commandArguments);
        } else {
            System.err.println("Invalid command `" + command + "'");
            System.exit(1);
        }
    }

    private static Object getBean(final String name) {
        return null;//applicationContext.getBean(name);
    }

    private static void setupBasicAuth(final WebAppContext context) {
        final String username = Config.getAuthUsername();
        if (username == null) {
            LOG.warning("No auth.username property set.  Basic auth will be disabled.");
            return;
        }
        final String password = Config.getAuthPassword();
        final Constraint constraint = new Constraint();
        constraint.setName(Constraint.__BASIC_AUTH);
        constraint.setRoles(USER_ROLES);
        constraint.setAuthenticate(true);

        final ConstraintMapping cm = new ConstraintMapping();
        cm.setConstraint(constraint);
        cm.setPathSpec("/*");

        final HashLoginService loginService = new HashLoginService("Telenor Security Server");
        loginService.putUser(username, Credential.getCredential(password), USER_ROLES);
        final ConstraintSecurityHandler handler = new ConstraintSecurityHandler();
        handler.setLoginService(loginService);

        final Constraint noAuthConstraint = new Constraint();
        noAuthConstraint.setAuthenticate(false);
        final ConstraintMapping noAuthAdmin = new ConstraintMapping();
        noAuthAdmin.setConstraint(noAuthConstraint);
        noAuthAdmin.setPathSpec("/admin/*");
        final ConstraintMapping noAuthAudit = new ConstraintMapping();
        noAuthAudit.setConstraint(noAuthConstraint);
        noAuthAudit.setPathSpec("/audit");

        handler.setConstraintMappings(new ConstraintMapping[]{noAuthAdmin, noAuthAudit, cm});

        context.setHandler(handler);
    }

    private static Connector[] initiateConnectors() {
        final List<Connector> connectors = new ArrayList<Connector>();
        if (httpPort >= 0) {
            final ServerConnector connector = new ServerConnector(webAppServer);
            connector.setPort(httpPort);
            connectors.add(connector);
        }
        if (httpsPort >= 0) {
            final SslContextFactory sslContextFactory = new SslContextFactory();
            sslContextFactory.setRenegotiationAllowed(true);
            SslConnectionFactory sslConnectionFactory = new SslConnectionFactory(sslContextFactory, DEFAULT_PROTOCOL);
            ServerConnector sslConnector = new ServerConnector(webAppServer);
            sslConnector.setPort(httpsPort);
            sslConnector.setReuseAddress(true);
            sslConnector.addConnectionFactory(sslConnectionFactory);
            connectors.add(sslConnector);
        }
        if (connectors.isEmpty()) {
            throw new RuntimeException("No listening ports specified in the configuration.");
        }
        return connectors.toArray(new Connector[connectors.size()]);
    }

    private static void startControlServer() {
        try {
            /* assert control secret is available */
            Config.getControlSecret();
            controlServer = new Server();
            final ServerConnector connector = new ServerConnector(controlServer);
            connector.setHost("localhost");
            connector.setPort(httpControlPort);
            controlServer.setConnectors(new Connector[]{connector});
            final ServletHandler handler = new ServletHandler();
            handler.addServletWithMapping(ControlServlet.class, "/");
            controlServer.setHandler(handler);
            controlServer.start();
        } catch (final Throwable t) {
            throw new RuntimeException("Unable to start control server", t);
        }
    }

    private static void startWebServer() {
        try {
            webAppServer = new Server();
            webAppServer.setConnectors(initiateConnectors());

            final WebAppContext context = new WebAppContext();

            context.setContextPath(ROOT);

            context.setBaseResource(Resource.newClassPathResource("/webapp/", true, true));

            context.setParentLoaderPriority(true);


            context.setConfigurations(
                    new Configuration[]{
                            new AnnotationConfiguration() {
                                @Override
                                public void preConfigure(WebAppContext context) throws Exception {
                                    MultiMap<String> map = new MultiMap<String>();
                                    map.add(WebApplicationInitializer.class.getName(), ServletInitializer.class.getName());
                                    context.setAttribute(CLASS_INHERITANCE_MAP, map);
                                    _classInheritanceHandler = new ClassInheritanceHandler(map);
                                }
                            }, new WebXmlConfiguration(),
                            new WebInfConfiguration(), new TagLibConfiguration(),
                            new PlusConfiguration(), new MetaInfConfiguration(),
                            new FragmentConfiguration(), new EnvConfiguration()}
            );

            webAppServer.setHandler(context);

            setupBasicAuth(context);

            webAppServer.start();
            if (httpPort >= 0) {
                System.out.println("Server available as http://localhost:" + httpPort + ROOT);
            }
            if (httpsPort >= 0) {
                System.out.println("SSL enabled server available as https://localhost:" + httpsPort + ROOT);
            }
        } catch (final Throwable t) {
            throw new RuntimeException("Unable to start web server", t);
        }
    }

    private static void stopServer(final Server server) {
        try {
            if (server != null) {
                server.stop();
            }
        } catch (final Exception e) {
            throw new RuntimeException("Error stopping Jetty Server", e);
        } finally {
            if (server != null) {
                server.destroy();
            }
        }
    }

    private static void stopWebServer() {
        LOG.info("Shutting down web server");
        stopServer(webAppServer);
        webAppServer = null;
    }

    private static void stopControlServer() {
        LOG.info("Shutting down control server");
        stopServer(controlServer);
        controlServer = null;
    }

    private static String sendControlCommand(final String command) {
        BufferedReader reader = null;
        try {
            final StringBuilder sb = new StringBuilder();
            final URL url = new URL("http", "localhost", Config.getControlPort(),
                    "/" + command + "?secret=" + Config.getControlSecret());
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            for (; ; ) {
                final String line = reader.readLine();
                if (line == null) {
                    break;
                }
                sb.append(line);
                sb.append('\n');
            }
            return sb.toString().trim();
        } catch (final IOException e) {
            throw new RuntimeException("Error communicating with control server.  Is the server running?", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    System.err.println("Error closing connection to control server.");
                }
            }
        }
    }

    private static boolean waitForServerToDie() {
        for (; ; ) {
            try {
                final String response = sendControlCommand("ping");
                if (!response.startsWith("OK")) {
                    System.out.println(response);
                    return false;
                }
                Thread.sleep(TimeUnit.SECONDS.toMillis(1));
            } catch (final Exception e) {
                /* expected */
                return true;
            }
        }
    }

    private static void assertArgsLength(final String command, final String[] args, final int length) {
        if (args.length == length) {
            return;
        }
        if (length == 0) {
            System.err.println("Command `" + command + "' accepts no arguments.");
            System.exit(1);
        }
        if (length == 1) {
            System.err.println("Command `" + command + "' accepts a single argument.");
            System.exit(1);
        }
        System.err.println("Command `" + command + "' accepts " + length + " arguments.");
        System.exit(1);
    }

    private static void commandStart(final String command, final String[] args) {
        assertArgsLength(command, args, 0);
        start();
    }

    private static void commandStop(final String command, final String[] args) {
        assertArgsLength(command, args, 0);
        System.out.println(sendControlCommand("stop"));
        if (!waitForServerToDie()) {
            System.err.println("There was an error.  Not sure if the server has stopped.");
        }
    }

    private static void commandJettyHash(final String command, final String[] args) {
        assertArgsLength(command, args, 1);
        System.out.println("Jetty-hashed password: " + Credential.MD5.digest(args[0]));
    }

    public static void start() {
        startControlServer();
        startWebServer();
    }

    public static void stop() {
        stopWebServer();
        stopControlServer();
    }

    public static final class ControlServlet extends HttpServlet {

        private static final long serialVersionUID = 1L;

        @Override
        protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
                throws ServletException, IOException {
            final PrintWriter out = resp.getWriter();
            resp.setContentType("text/plain");
            try {
                final String command = req.getRequestURI().substring(1);
                final String secret = req.getParameter("secret");
                if (secret == null || !secret.equals(Config.getControlSecret())) {
                    out.println("ERROR: Missing or incorrect secret.");
                    return;
                }
                if ("start".equals(command)) {
                    out.println("ERROR: Server already running.");
                } else if ("stop".equals(command)) {
                    out.println("OK: Shutdown scheduled.");
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            StartJetty.stop();
                            try {
                                Thread.sleep(2L * 1000L);
                            } catch (final InterruptedException e) {
                                System.out.println("Ignoring InterruptedException");
                            }
                            System.exit(0);
                        }
                    }, 2L * 1000L);
                } else if ("ping".equals(command)) {
                    out.println("OK: Still alive.");
                } else {
                    out.println("ERROR: Command must be either `start' or `stop'.");
                }
            } catch (final Throwable t) {
                LOG.log(Level.WARNING, "Unexpected error", t);
                out.println("ERROR: " + t.getMessage());
            }
        }

    }

}
