package fr.asvadia.guard.bukkit.util;

import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class HandShakeVerify {

    /**
     * The name of the BungeeGuard auth token.
     */
    private static final String TOKEN_NAME = "token";
    /**
     * The key used to define the name of properties in the handshake.
     */
    private static final String PROPERTY_NAME_KEY = "name";
    /**
     * The key used to define the value of properties in the handshake.
     */
    private static final String PROPERTY_VALUE_KEY = "value";

    /**
     * Shared Gson instance.
     */
    private static final Gson GSON = new Gson();

    /**
     * The type of the property list in the handshake.
     */
    private static final Type PROPERTY_LIST_TYPE = new TypeToken<List<JsonObject>>() {}.getType();

    private String enterToken;
    private Status status;
    private String serverHostname;
    private String socketAddressHostname;
    private UUID uniqueId;
    private String propertiesJson;
    private String connectionDescription;

    public String getServerHostname() {
        return serverHostname;
    }

    public String getSocketAddressHostname() {
        return socketAddressHostname;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public String getPropertiesJson() {
        return propertiesJson;
    }

    public String encode() {
        return this.serverHostname + "\00" + this.socketAddressHostname + "\00" + this.uniqueId + "\00" + this.propertiesJson;
    }


    public Status getStatus() {
        return this.status;
    }

    public String getConnectionDescription() {
        return this.connectionDescription;
    }

    public enum Status {
        INVALID_HANDSHAKE, NO_TOKEN, INCORRECT_TOKEN, SUCCESS;
    }


    /**
     *
     * Verif
     *
     */

    public void decodeAndVerify(String handshake, String token) {
        String[] split = handshake.split("\00");
        this.connectionDescription = handshake;
        if (split.length >= 3) {

            this.serverHostname = split[0];
            this.socketAddressHostname = split[1];
            this.uniqueId = UUID.fromString(split[2].replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));

            this.connectionDescription = uniqueId + " - " + socketAddressHostname;

            if(split.length == 4) {
                List<JsonObject> properties = new LinkedList<>(GSON.fromJson(split[3], PROPERTY_LIST_TYPE));
                if (!properties.isEmpty()) {
                    for (Iterator<JsonObject> iterator = properties.iterator(); iterator.hasNext(); ) {
                        JsonObject property = iterator.next();
                        if (property.get(PROPERTY_NAME_KEY).getAsString().equals(TOKEN_NAME)) {
                            this.enterToken = property.get(PROPERTY_VALUE_KEY).getAsString();
                            iterator.remove();
                        }
                    }
                    if (enterToken != null) {
                        if (Hashing.sha256().hashString(token, StandardCharsets.UTF_8).toString().equals(enterToken)) {
                            this.propertiesJson = GSON.toJson(properties, PROPERTY_LIST_TYPE);
                            this.status = Status.SUCCESS;
                        } else {
                            this.status = Status.INCORRECT_TOKEN;
                        }
                    } else {
                        this.status = Status.NO_TOKEN;
                    }
                } else {
                    this.status = Status.NO_TOKEN;
                }
            } else {
                this.status = Status.NO_TOKEN;
            }
        } else {
            this.status = Status.INVALID_HANDSHAKE;
        }
    }

}



