package fr.asvadia.guard.bungee.util;


import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.LoginResult;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * Extension of {@link LoginResult} which returns a modified Property array when
 * {@link #getProperties()} is called by the ServerConnector implementation.
 *
 * To achieve this, the stack trace is analyzed. This is kinda crappy, but is the only way
 * to modify the properties without leaking the token to other clients via the tablist.
 */
public class HandShakeEditor extends LoginResult {
    private static final String SERVER_CONNECTOR = "net.md_5.bungee.ServerConnector";
    private static final String SERVER_CONNECTOR_CONNECTED = "connected";
    private static final Field PROFILE_FIELD;
    private final String extraToken;
    private final boolean offline;

    private HandShakeEditor(LoginResult oldProfile, String extraToken) {
        super(oldProfile.getId(), oldProfile.getName(), oldProfile.getProperties());
        this.extraToken = extraToken;
        this.offline = false;
    }

    private HandShakeEditor(String extraToken) {
        super(null, null, new LoginResult.Property[0]);
        this.extraToken = extraToken;
        this.offline = true;
    }

    @Override
    public LoginResult.Property[] getProperties() {
        StackTraceElement[] trace = new Exception().getStackTrace();
        if (trace.length < 2) {
            return super.getProperties();
        }
        StackTraceElement callLocation = trace[1];
        if (callLocation.getClassName().equals(SERVER_CONNECTOR) && callLocation.getMethodName().equals(SERVER_CONNECTOR_CONNECTED)) {
            return this.addTokenProperty(super.getProperties());
        }
        return super.getProperties();
    }

    private LoginResult.Property[] addTokenProperty(LoginResult.Property[] properties) {
        LoginResult.Property[] newProperties = Arrays.copyOf(properties, properties.length + 1);
        newProperties[properties.length] = new LoginResult.Property("token", this.extraToken, "");
        return newProperties;
    }

    @Override
    public String getId() {
        if (this.offline) {
            throw new RuntimeException("getId called for offline variant of SpoofedLoginResult");
        }
        return super.getId();
    }

    @Override
    public String getName() {
        if (this.offline) {
            throw new RuntimeException("getId called for offline variant of SpoofedLoginResult");
        }
        return super.getId();
    }

    public static void inject(InitialHandler handler, String token) {
        LoginResult profile = handler.getLoginProfile();
        HandShakeEditor newProfile = profile == null ? new HandShakeEditor(token) : new HandShakeEditor(profile, token);
        try {
            PROFILE_FIELD.set(handler, newProfile);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    static {
        try {
            PROFILE_FIELD = InitialHandler.class.getDeclaredField("loginProfile");
            PROFILE_FIELD.setAccessible(true);
        }
        catch (NoSuchFieldException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}