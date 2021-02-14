/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.openhab.binding.freeathomesystem.internal.handler;

import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.net.util.Base64;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BasicAuthentication;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.freeathomesystem.internal.Configuration.FreeAtHomeBridgeHandlerConfiguration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

/**
 * The {@link FreeAtHomeBridgeHandler} is responsible for handling the free@home bridge and
 * its main communication.
 *
 * @author Andras Uhrin - Initial contribution
 *
 */

@NonNullByDefault
public class FreeAtHomeBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(FreeAtHomeBridgeHandler.class);

    public static @Nullable FreeAtHomeBridgeHandler freeAtHomeSystemHandler;

    public @Nullable ChannelUpdateHandler channelUpdateHandler;

    private @Nullable FreeAtHomeBridgeHandlerConfiguration config;

    // Clients for the network communication
    private @Nullable HttpClient httpClient = null;
    private @Nullable WebSocketClient websocketClient = null;
    private @Nullable EventSocket socket = null;
    private @Nullable FreeAtHomeWebsocketMonitorThread socketMonitor = null;
    private @Nullable QueuedThreadPool jettyThreadPool = null;

    private @Nullable String componentListString;

    private @Nullable List<String> listOfComponentId;

    private String sysApUID = "00000000-0000-0000-0000-000000000000";

    private String ipAddress = "192.168.178.20";
    private String username = "";
    private String password = "";

    private String baseUrl = "http://192.168.178.20/fhapi/v1/api";

    private String authField = "";

    private AtomicBoolean httpConnectionOK = new AtomicBoolean(false);

    int numberOfComponents;

    private final int BRIDGE_WEBSOCKET_RECONNECT_DELAY = 30;

    public FreeAtHomeBridgeHandler(Bridge thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        logger.warn("Unknown handle command for the bridge - channellUID {}, command {}", channelUID, command);
    }

    public String getDatapoint(@Nullable String deviceId, String channel, String datapoint) {

        String url = baseUrl + "/rest/datapoint/" + sysApUID + "/" + deviceId + "." + channel + "." + datapoint;

        if (null == httpClient) {
            return "0";
        }

        try {

            Request req = httpClient.newRequest(url);
            ContentResponse response = req.send();

            // Get component List
            String deviceString = new String(response.getContent());

            JsonReader reader = new JsonReader(new StringReader(deviceString));

            reader.setLenient(true);

            JsonParser parser = new JsonParser();
            JsonElement jsonTree = parser.parse(reader);

            // check the output
            if (jsonTree.isJsonObject()) {
                JsonObject jsonObject = jsonTree.getAsJsonObject();

                jsonObject = jsonObject.getAsJsonObject(sysApUID);
                JsonArray jsonValueArray = jsonObject.getAsJsonArray("values");

                JsonElement element = jsonValueArray.get(0);
                String value = element.getAsString();

                return value;
            }

        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Communication error by getDatapoint [{}]", e.getMessage());
        } catch (NullPointerException e) {
            logger.error("Invalid by JSON reponse by getDatapoint [{}]", e.getMessage());
        }

        return "0";
    }

    public boolean setDatapoint(@Nullable String deviceId, String channel, String datapoint, String valueString) {
        String url = baseUrl + "/rest/datapoint/" + sysApUID + "/" + deviceId + "." + channel + "." + datapoint;

        try {

            Request req = httpClient.newRequest(url);
            req.content(new StringContentProvider(valueString));
            req.method(HttpMethod.PUT);
            ContentResponse response = req.send();

        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Communication error by getDatapoint [{}]", e.getMessage());
        }

        return true;
    }

    public void processSocketEvent(String receivedText) {

        JsonReader reader = new JsonReader(new StringReader(receivedText));

        reader.setLenient(true);

        JsonParser parser = new JsonParser();
        JsonElement jsonTree = parser.parse(reader);

        // check the output
        if (jsonTree.isJsonObject()) {
            JsonObject jsonObject = jsonTree.getAsJsonObject();

            jsonObject = jsonObject.getAsJsonObject(sysApUID);
            jsonObject = jsonObject.getAsJsonObject("datapoints");

            Set<String> keys = jsonObject.keySet();

            Iterator iter = keys.iterator();

            while (iter.hasNext()) {
                String eventDatapointID = (String) iter.next();

                JsonElement element = jsonObject.get(eventDatapointID);
                String value = element.getAsString();

                channelUpdateHandler.updateChannelByDatapointEvent(eventDatapointID, value);
            }
        }
    }

    public void closeHttpConnection() {
        try {
            httpClient.stop();
        } catch (Exception e1) {
            logger.error("Error by closing Websocket connection [{}]", e1.getMessage());
        }
    }

    public boolean openHttpConnection() {
        boolean ret = false;

        // Instantiate HttpClient.
        if (null == httpClient) {
            httpClient = new HttpClient();
        }

        // Check the http client creation and configure it
        if (null == httpClient) {

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Cannot cannot create http client");

            logger.error("Cannot create internal httpclient");

            return false;
        } else {
            // Configure HttpClient
            httpClient.setFollowRedirects(false);
        }

        try {
            // Start HttpClient.

            switch (httpClient.getState()) {
                case AbstractLifeCycle.FAILED:
                case AbstractLifeCycle.STOPPED: {
                    httpClient.start();
                    break;
                }
                case AbstractLifeCycle.STARTING:
                case AbstractLifeCycle.STARTED:
                case AbstractLifeCycle.STOPPING: {
                    // nothing to do
                    break;
                }
            }
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Cannot cannot create http client");

            logger.error("Cannot start http connection - {}", e.getMessage());
        }

        // Add authentication credentials make a check.
        try {
            // Add authentication credentials.
            AuthenticationStore auth = httpClient.getAuthenticationStore();

            URI uri1 = new URI("http://" + ipAddress + "/fhapi/v1");
            auth.addAuthenticationResult(new BasicAuthentication.BasicResult(uri1, username, password));

            String url = "http://" + ipAddress + "/fhapi/v1/api/rest/devicelist";

            Request req = httpClient.newRequest(url);
            ContentResponse response = req.send();

            httpConnectionOK.set(true);

            ret = true;

        } catch (URISyntaxException | InterruptedException | ExecutionException | TimeoutException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Cannot open http connection, wrong passord");

            logger.error("Cannot open http connection {}", e.getMessage());
        }

        return ret;
    }

    //
    // Method to connect the websocket session
    //
    public boolean connectWebsocketSession() {
        boolean ret = false;

        URI uri = URI.create("ws://" + ipAddress + "/fhapi/v1/api/ws");

        if ((null != websocketClient) && (null != socket)) {
            try {
                // Start socket client
                websocketClient.start();
                ClientUpgradeRequest request = new ClientUpgradeRequest();
                request.setHeader("Authorization", authField);
                websocketClient.connect(socket, uri, request);

                ret = true;

            } catch (Exception e) {
                logger.error("Error by opening Websocket connection [{}]", e.getMessage());
                try {
                    websocketClient.stop();

                    ret = false;
                } catch (Exception e1) {
                    logger.error("Error by opening Websocket connection [{}]", e1.getMessage());

                    ret = false;
                }
            }
        }

        return ret;
    }

    //
    // Method to open the websocket connection
    //
    @SuppressWarnings("deprecation")
    public void closeWebSocketConnection() {
        if (null == socketMonitor) {
            socketMonitor.stop();
        }

        if (null == jettyThreadPool) {
            try {
                jettyThreadPool.stop();
            } catch (Exception e1) {
                logger.error("Error by closing Websocket connection [{}]", e1.getMessage());
            }
        }

        if (null != websocketClient) {
            try {
                websocketClient.stop();
            } catch (Exception e2) {
                logger.error("Error by closing Websocket connection [{}]", e2.getMessage());
            }
        }
    }

    //
    // Method to open the websocket connection
    //
    public boolean openWebSocketConnection() {
        boolean ret = false;

        URI uri = URI.create("ws://" + ipAddress + "/fhapi/v1/api/ws");

        String authString = username + ":" + password;
        byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
        String authStringEnc = new String(authEncBytes);

        authField = "Basic " + authStringEnc;

        if (null == jettyThreadPool) {
            jettyThreadPool = new QueuedThreadPool();
            jettyThreadPool.setName(FreeAtHomeBridgeHandler.class.getSimpleName());
            jettyThreadPool.setDaemon(true);
            jettyThreadPool.setStopTimeout(0);
        }

        if (null == websocketClient) {
            websocketClient = new WebSocketClient();
        }

        if (null != websocketClient) {
            websocketClient.setExecutor(jettyThreadPool);

            if (null == socket) {
                socket = new EventSocket();
            }

            if (null != socket) {
                // set bridge for the socket event handler
                socket.setBridge(this);

                if (null == socketMonitor) {
                    socketMonitor = new FreeAtHomeWebsocketMonitorThread();
                    socketMonitor.start();

                    ret = true;
                } else {
                    websocketClient = null;
                    socket = null;
                    ret = false;
                }
            } else {
                websocketClient = null;
                ret = false;
            }
        }
        return ret;
    }

    @Override
    public void thingUpdated(Thing thing) {
        dispose();
        this.thing = thing;
        initialize();
    }

    @Override
    public void initialize() {
        // TODO: Initialize the handler.
        // The framework requires you to return from this method quickly. Also, before leaving this method a thing
        // status from one of ONLINE, OFFLINE or UNKNOWN must be set. This might already be the real thing status in
        // case you can decide it directly.
        // In case you can not decide the thing status directly (e.g. for long running connection handshake using WAN
        // access or similar) you should set status UNKNOWN here and then decide the real status asynchronously in the
        // background.

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.
        // Store the class for use in thing handlers
        updateStatus(ThingStatus.UNKNOWN);

        httpConnectionOK.set(false);

        freeAtHomeSystemHandler = null;

        config = getConfigAs(FreeAtHomeBridgeHandlerConfiguration.class);

        // no configuration is given, abort initialization
        if (null == config) {
            return;
        }

        ipAddress = config.ipaddress;
        password = config.password;
        username = config.username;

        baseUrl = "http://" + ipAddress + "/fhapi/v1/api";

        // Open Http connection
        if (false == openHttpConnection()) {

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Cannot open http connection");

            logger.error("Cannot open websocket connection");

            return;
        }

        // Create channel update handler
        channelUpdateHandler = new ChannelUpdateHandler();

        if (null == channelUpdateHandler) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    "Cannot create internal structure");
            logger.error("Cannot create internal structure - ChannelUpdateHandler");
            return;
        }

        // Open the websocket connection for immediate status updates
        if (false == openWebSocketConnection()) {

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Cannot open websocket connection");

            logger.error("Cannot open websocket connection");

            return;
        }

        // Background initialization:
        scheduler.execute(() -> {
            boolean thingReachable = true; // <background task with long running initialization here>
            // when done do:
            if (thingReachable) {
                freeAtHomeSystemHandler = this;
                updateStatus(ThingStatus.ONLINE);

                try {
                    socket.getLatch().await();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    // e.printStackTrace();
                }
            } else {
                freeAtHomeSystemHandler = this;
                updateStatus(ThingStatus.OFFLINE);
            }
        });

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }

    @Override
    public void dispose() {
    }

    public FreeAtHomeDeviceList getDeviceDeviceList() {
        FreeAtHomeSysApDeviceList deviceList = new FreeAtHomeSysApDeviceList(httpClient, ipAddress, sysApUID);
        // FreeAtHomeTestDeviceList deviceList = new FreeAtHomeTestDeviceList(
        // "responsedevice.json",
        // "response_formatted.json", sysApUID);

        deviceList.buildComponentList();

        return deviceList;
    }

    /**
     * Thread that maintains connection via Websocket.
     *
     * @author Andras Uhrin
     *
     */
    private class FreeAtHomeWebsocketMonitorThread extends Thread {

        // initial delay to initiate connection
        private AtomicInteger reconnectDelay = new AtomicInteger();

        public FreeAtHomeWebsocketMonitorThread() {
        }

        @Override
        public void run() {
            // set initial connect delay to 0
            reconnectDelay.set(0);

            try {
                while (!isInterrupted()) {
                    if (true == httpConnectionOK.get()) {
                        if (connectSession()) {
                            socket.awaitEndCommunication();
                        }
                    } else {
                        TimeUnit.SECONDS.sleep(BRIDGE_WEBSOCKET_RECONNECT_DELAY);
                    }
                }
            } catch (InterruptedException e) {
                // logger.debug("Thread interrupted [{}]", e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Problem in websocket connection");
            }
        }

        private boolean connectSession() throws InterruptedException {
            int delay = reconnectDelay.get();

            if (delay > 0) {
                logger.debug("Delaying connect request by {} seconds.", reconnectDelay);
                TimeUnit.SECONDS.sleep(delay);
            }

            logger.debug("Server connecting to websocket");

            if (!connectWebsocketSession()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Problem in websocket connection");

                reconnectDelay.set(BRIDGE_WEBSOCKET_RECONNECT_DELAY);

                return false;
            }

            return true;
        }
    }
}
