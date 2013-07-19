package com.vegaasen.testing.spring3;

import java.io.*;
import java.util.Map;
import java.util.Properties;

/**
 *
 */
public class Config {

    public static final String KEYSTORE_PATH = "keystore.path";
    public static final String AUDIT_FILE_DIRECTORY = "audit.file.directory";
    public static final String CONFIG_PATH_PROPERTY = "config.path";
    public static final String MASTER_PASSWORD_FILENAME_PROPERTY = "master.password.filename";
    private static final String MASTER_PASSWORD_PROPERTY = "master.password";
    private static Properties props = new Properties();
    private static String configPath;

    private Config() {
    }

    static {
        configPath = System.getProperty(CONFIG_PATH_PROPERTY);
        if (configPath == null) {
            throw new RuntimeException("No system property '" + CONFIG_PATH_PROPERTY
                    + "' found.  Please set it to the path of the configuration file.");
        }
        InputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(configPath));
            props.load(in);
            /* copy (encrypted values) to system properties as well. */
            /* need to set the following two before copying the rest, otherwise encrypted properties cannot be decrypted. */
            final String masterPassword = props.getProperty(MASTER_PASSWORD_PROPERTY);
            if (masterPassword != null) {
                System.setProperty(MASTER_PASSWORD_PROPERTY, masterPassword);
            }
            if (!systemPropertyAlreadyDefined(MASTER_PASSWORD_FILENAME_PROPERTY)) {
                final String masterPasswordFilename = props.getProperty(MASTER_PASSWORD_FILENAME_PROPERTY);
                if (masterPasswordFilename != null) {
                    System.setProperty(MASTER_PASSWORD_FILENAME_PROPERTY, masterPasswordFilename);
                }
            }
            for(Map.Entry<Object, Object> property : props.entrySet()) {
                if(property!=null) {
                    System.setProperty(String.valueOf(property.getKey()), String.valueOf(property.getValue()));
                }
            }
        } catch (final IOException e) {
            throw new RuntimeException("Error loading properties from '" + configPath + "'", e);
        }
    }

    private static boolean systemPropertyAlreadyDefined(final String propertyName) {
        return System.getProperty(propertyName) != null;
    }

    private static String getRequiredProperty(final String key) {
        final String value = props.getProperty(key);
        if (value == null) {
            throw new RuntimeException("Required property `" + key + "' not found in `" + configPath + "'.");
        }
        return value;
    }

    private static int getIntProperty(final String key) {
        final String value = props.getProperty(key);
        if (value == null) {
            return -1;
        }
        try {
            return Integer.valueOf(value);
        } catch (final NumberFormatException e) {
            throw new RuntimeException("Numeric value expected for property `" + key + "', but `" + value + "' was found.");
        }
    }

    private static int getRequiredIntProperty(final String key) {
        getRequiredProperty(key);
        return getIntProperty(key);
    }

    public static int getHttpPort() {
        return getIntProperty("http.port");
    }

    public static int getHttpsPort() {
        return getIntProperty("https.port");
    }

    public static String getAuthUsername() {
        return props.getProperty("auth.username");
    }

    public static String getAuthPassword() {
        return getRequiredProperty("auth.password");
    }

    public static String getKeystorePath() {
        final String keystorePath = System.getProperty(KEYSTORE_PATH);
        return keystorePath != null ? keystorePath : getRequiredProperty(KEYSTORE_PATH);
    }

    public static String getKeystorePassword() {
        return getRequiredProperty("keystore.password");
    }

    public static String getKeystoreKeyPassword() {
        return getRequiredProperty("keystore.key.password");
    }

    public static int getControlPort() {
        return getRequiredIntProperty("control.port");
    }

    public static String getControlSecret() {
        return getRequiredProperty("control.secret");
    }

    public static String getAssertionSigningKey() {
        return getRequiredProperty("assertion.signing-key");
    }

    public static String getIamUserName() {
        return getRequiredProperty("iam.username");
    }

    public static String getIamPassword() {
        return getRequiredProperty("iam.password");
    }

    /**
     * For SAMLv2, this is needed. It was not needed for the SAMLv1-part, as that identified itself by
     * "Entrust Attribute Authority"
     *
     * @return the correct saml issuer
     */
    public static String getSAMLIssuer() {
        return getRequiredProperty("saml.assertion.issuer");
    }

    public static class ServiceReference {

        public static String getServerUrl() {
            return props.getProperty("service.reference.url");
        }

        public static int getPortForWindowsTnuId() {
            return getIntProperty("paam.type.windows.tnuid.port");
        }

        public static int getPortForWindowsUsername() {
            return getIntProperty("paam.type.windows.username.port");
        }

    }

    public static String getAuditFilePrefix() {
        final String auditFileDirectorySystemProp = System.getProperty(AUDIT_FILE_DIRECTORY);
        final String auditFileDirectory =
                auditFileDirectorySystemProp != null ? auditFileDirectorySystemProp : getRequiredProperty(AUDIT_FILE_DIRECTORY);
        return auditFileDirectory + File.separatorChar + getRequiredProperty("audit.file.prefix");
    }

    public static int getCallStatLogDelayMinutes() {
        int i = getIntProperty("stat.call.delay.minutes");
        if (i < 0) {
            i = 30;
        }
        return i;
    }

    public static String getAllowedAuditIpAddress() {
        final String addr = props.getProperty("audit.allowed.address");
        if (addr == null) {
            return "127.0.0.1";
        }
        return addr;
    }

}
