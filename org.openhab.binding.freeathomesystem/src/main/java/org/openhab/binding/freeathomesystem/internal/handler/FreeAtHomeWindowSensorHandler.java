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

import org.openhab.binding.freeathomesystem.internal.FreeAtHomeSystemBindingConstants;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ValueStateConverters.BooleanValueStateConverter;
import ValueStateConverters.DecimalValueStateConverter;

/**
 * The {@link FreeAtHomeActuatorHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Andras Uhrin - Initial contribution
 *
 */

public class FreeAtHomeWindowSensorHandler extends FreeAtHomeSystemBaseHandler {

    private String deviceID;
    private String deviceChannel;
    private String devicePosOdp;
    private String deviceStateOdp;

    private FreeAtHomeBridgeHandler freeAtHomeBridge = null;

    private final Logger logger = LoggerFactory.getLogger(FreeAtHomeWindowSensorHandler.class);

    public FreeAtHomeWindowSensorHandler(Thing thing) {
        super(thing);
    }

    // "outputs": {
    // "odp0000": {
    // "pairingID": 53, window door open 1 close 0 deviceStateOdp
    // "value": "0"
    // },
    // "odp0001": {
    // "pairingID": 41, Delivers position for Window/Door (Open / Tilted / Closed) devicePosOdp
    // "value": "0"
    // }

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
                if (deviceInterface.equalsIgnoreCase(FreeAtHomeDeviceDescription.DEVICE_INTERFACE_WIRELESS_TYPE)) {
                    deviceChannel = "ch0000";
                    deviceStateOdp = "odp0000";
                    devicePosOdp = "----";
                } else if (deviceInterface
                        .equalsIgnoreCase(FreeAtHomeDeviceDescription.DEVICE_INTERFACE_VIRTUAL_TYPE)) {
                    deviceChannel = "ch0000";
                    deviceStateOdp = "odp0000";
                    devicePosOdp = "odp0001";
                }

                logger.debug("Initialize window sensor - {}", deviceID);

                // Get initial state of the switch directly from the free@home sensor
                String valueString = freeAtHomeBridge.getDatapoint(deviceID, deviceChannel, deviceStateOdp);

                int value;

                try {
                    value = Integer.parseInt(valueString);
                } catch (NumberFormatException e) {
                    value = 0;
                }

                DecimalType decState = new DecimalType(value);

                // set the initial state
                updateState(FreeAtHomeSystemBindingConstants.WINDOWSENSOR_CHANNEL_STATE_ID, decState);

                // Get initial state of the switch directly from the free@home sensor
                valueString = freeAtHomeBridge.getDatapoint(deviceID, deviceChannel, devicePosOdp);

                try {
                    value = Integer.parseInt(valueString);
                } catch (NumberFormatException e) {
                    value = 0;
                }

                DecimalType decPos = new DecimalType(value);

                // set the initial state
                updateState(FreeAtHomeSystemBindingConstants.WINDOWSENSOR_CHANNEL_POS_ID, decPos);

                // Register device and specific channel for event based state updated
                if (null != freeAtHomeBridge.channelUpdateHandler) {
                    freeAtHomeBridge.channelUpdateHandler.registerChannel(deviceID, deviceChannel, deviceStateOdp, this,
                            new ChannelUID(this.getThing().getUID(),
                                    FreeAtHomeSystemBindingConstants.WINDOWSENSOR_CHANNEL_STATE_ID),
                            new BooleanValueStateConverter());

                    freeAtHomeBridge.channelUpdateHandler.registerChannel(deviceID, deviceChannel, devicePosOdp, this,
                            new ChannelUID(this.getThing().getUID(),
                                    FreeAtHomeSystemBindingConstants.WINDOWSENSOR_CHANNEL_POS_ID),
                            new DecimalValueStateConverter());

                    logger.debug("Device - online: {}", deviceID);

                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "No online updates are received");
                }

            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);

                logger.debug("Incorrect bridge class: {}", deviceID);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridge available");

            logger.debug("No bridge for device: {}", deviceID);
        }
    }

    @Override
    public void dispose() {
        // Unregister device and specific channel for event based state updated
        freeAtHomeBridge.channelUpdateHandler.unregisterChannel(deviceID, deviceChannel, deviceStateOdp);
        freeAtHomeBridge.channelUpdateHandler.unregisterChannel(deviceID, deviceChannel, devicePosOdp);

        logger.debug("Device removed {}", deviceID);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        updateStatus(ThingStatus.ONLINE);

        // update only via Websocket
        logger.debug("Handle command switch {} - at channel {} - full command {}", deviceID, channelUID.getAsString(),
                command.toFullString());
    }
}
