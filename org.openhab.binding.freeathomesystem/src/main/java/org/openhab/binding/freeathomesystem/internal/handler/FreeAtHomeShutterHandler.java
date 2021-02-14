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
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ValueStateConverters.BooleanValueStateConverter;

/**
 * The {@link FreeAtHomeActuatorHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Andras Uhrin - Initial contribution
 *
 */

public class FreeAtHomeShutterHandler extends FreeAtHomeSystemBaseHandler {

    private String deviceID;
    private String deviceChannel;
    private String devicePosOdp;
    private String devicePosIdp;
    private String deviceStepIdp;
    private String deviceStopIdp;

    private FreeAtHomeBridgeHandler freeAtHomeBridge = null;

    private final Logger logger = LoggerFactory.getLogger(FreeAtHomeShutterHandler.class);

    public FreeAtHomeShutterHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        Map<String, String> properties = getThing().getProperties();

        Bridge bridge = this.getBridge();
        deviceID = properties.get("deviceId");

        String deviceInterface = properties.get("interface");
        deviceChannel = properties.get("channelId");

        if (null != bridge) {
            ThingHandler handler = bridge.getHandler();

            if (handler instanceof FreeAtHomeBridgeHandler) {
                freeAtHomeBridge = (FreeAtHomeBridgeHandler) handler;

                // Initialize the communication device channel properties
                devicePosOdp = "idp0001";
                deviceStepIdp = "idp0000";
                deviceStopIdp = "idp0001";
                devicePosIdp = "idp0002";

                logger.debug("Initialize switch - {}", deviceID);

                // Get initial state of the switch directly from the free@home switch
                String valueString = freeAtHomeBridge.getDatapoint(deviceID, deviceChannel, devicePosOdp);

                DecimalType dec = new DecimalType(valueString);
                updateState(FreeAtHomeSystemBindingConstants.SHUTTER_POS_CHANNEL_ID, dec);

                // Register device and specific channel for event based state updated
                if (null != freeAtHomeBridge.channelUpdateHandler) {
                    freeAtHomeBridge.channelUpdateHandler.registerChannel(deviceID, deviceChannel, devicePosOdp, this,
                            new ChannelUID(this.getThing().getUID(),
                                    FreeAtHomeSystemBindingConstants.SHUTTER_POS_CHANNEL_ID),
                            new BooleanValueStateConverter());

                    logger.debug("Device - online: {}", deviceID);

                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "No online updates are possible");
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
        // Unegister device and specific channel for event based state updated
        freeAtHomeBridge.channelUpdateHandler.unregisterChannel(deviceID, deviceChannel, devicePosOdp);

        logger.debug("Device removed {}", deviceID);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        updateStatus(ThingStatus.ONLINE);

        if (command instanceof RefreshType) {
            String valueString = freeAtHomeBridge.getDatapoint(deviceID, deviceChannel, devicePosOdp);

            DecimalType dec = new DecimalType(valueString);
            updateState(FreeAtHomeSystemBindingConstants.SHUTTER_POS_CHANNEL_ID, dec);
        }

        if (command instanceof UpDownType) {
            UpDownType locCommand = (UpDownType) command;

            if (locCommand.equals(UpDownType.UP)) {
                freeAtHomeBridge.setDatapoint(deviceID, deviceChannel, deviceStepIdp, "0");
                updateState(channelUID, UpDownType.UP);
            }

            if (locCommand.equals(UpDownType.DOWN)) {
                freeAtHomeBridge.setDatapoint(deviceID, deviceChannel, deviceStepIdp, "1");
                updateState(channelUID, UpDownType.DOWN);
            }
        }

        if (command instanceof StopMoveType) {
            freeAtHomeBridge.setDatapoint(deviceID, deviceChannel, deviceStopIdp, "0");
        }

        if (command instanceof PercentType) {
            PercentType locCommand = (PercentType) command;

            freeAtHomeBridge.setDatapoint(deviceID, deviceChannel, deviceStepIdp, locCommand.toString());
            updateState(channelUID, locCommand);
        }

        logger.debug("Handle command switch {} - at channel {} - full command {}", deviceID, channelUID.getAsString(),
                command.toFullString());
    }
}
