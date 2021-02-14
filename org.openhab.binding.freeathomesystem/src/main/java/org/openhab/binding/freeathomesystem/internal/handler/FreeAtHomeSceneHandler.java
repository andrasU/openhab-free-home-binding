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

import java.util.Map;

import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FreeAtHomeActuatorHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Andras Uhrin - Initial contribution
 *
 */

public class FreeAtHomeSceneHandler extends FreeAtHomeSystemBaseHandler {

    private String deviceID;
    private String deviceChannel;
    private String deviceOdp;

    private FreeAtHomeBridgeHandler freeAtHomeBridge = null;

    private final Logger logger = LoggerFactory.getLogger(FreeAtHomeSceneHandler.class);

    public FreeAtHomeSceneHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        Map<String, String> properties = getThing().getProperties();

        Bridge bridge = this.getBridge();
        deviceID = properties.get("deviceId");

        String deviceInterface = properties.get("interface");

        if (null != bridge) {
            ThingHandler handler = bridge.getHandler();

            if (handler instanceof FreeAtHomeBridgeHandler) {
                freeAtHomeBridge = (FreeAtHomeBridgeHandler) handler;

                // Initialize the communication device channel properties
                if (deviceInterface.equalsIgnoreCase(FreeAtHomeDeviceDescription.DEVICE_INTERFACE_UNKNOWN_TYPE)) {
                    deviceChannel = "ch0000";
                    deviceOdp = "odp0000";
                }

                logger.debug("Initialize scene handler - {}", deviceID);

                // Get initial state of the switch directly from the free@home sensor
                logger.debug("Device - online: {}", deviceID);

                updateStatus(ThingStatus.ONLINE);
            }
        }
    }

    @Override
    public void dispose() {
        // Unregister device and specific channel for event based state updated

        logger.debug("Device removed {}", deviceID);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        updateStatus(ThingStatus.ONLINE);

        if (command instanceof RefreshType) {
            updateState(channelUID, OnOffType.OFF);
        }

        if (command instanceof OnOffType) {
            OnOffType locCommand = (OnOffType) command;

            if (locCommand.equals(OnOffType.ON)) {
                freeAtHomeBridge.setDatapoint(deviceID, deviceChannel, deviceOdp, "1");
                updateState(channelUID, OnOffType.ON);
            }
        }

        // the scene can be triggered only therefore reset after 5 seconds
        scheduler.execute(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {

            }

            updateState(channelUID, OnOffType.OFF);
        });

        logger.debug("Handle command switch {} - at channel {} - full command {}", deviceID, channelUID.getAsString(),
                command.toFullString());
    }
}
