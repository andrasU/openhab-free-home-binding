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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeathomesystem.internal.FreeAtHomeSystemBindingConstants;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link FreeAtHomeBridgeHandler} is responsible for determining the device type
 * based on the received json string
 *
 * @author Andras Uhrin - Initial contribution
 *
 */

public class FreeAtHomeDeviceDescription {

    public static final int FID_UNKNOWN = 0xFFFFAAFF; // Control element

    // // free@home constants
    public static final int FID_SWITCH_SENSOR = 0x0000; // Control element
    public static final int FID_DIMMING_SENSOR = 0x0001; // Dimming sensor
    public static final int FID_BLIND_SENSOR = 0x0003; // Blind sensor
    public static final int FID_STAIRCASE_LIGHT_SENSOR = 0x0004; // Stairwell light sensor
    public static final int FID_FORCE_ON_OFF_SENSOR = 0x0005;// Force On/Off sensor
    public static final int FID_SCENE_SENSOR = 0x0006; // Scene sensor
    public static final int FID_SWITCH_ACTUATOR = 0x0007;// Switch actuator
    public static final int FID_SHUTTER_ACTUATOR = 0x0009; // Blind actuator
    public static final int FID_ROOM_TEMPERATURE_CONTROLLER_MASTER_WITH_FAN = 0x000A;// Room temperature controller
    // // withpublic static final
    // // String
    // // Fan speed level
    // public static final int FID_ROOM_TEMPERATURE_CONTROLLER_SLAVE = 0x000B; // Room temperature controller extension
    // // unit
    // public static final int FID_WIND_ALARM_SENSOR = 0x000C; // Wind Alarm
    // public static final int FID_FROST_ALARM_SENSOR = 0x000D; // Frost Alarm
    // public static final int FID_RAIN_ALARM_SENSOR = 0x000E; // Rain Alarm
    public static final int FID_WINDOW_DOOR_SENSOR = 0x000F; // Window sensor
    // public static final int FID_MOVEMENT_DETECTOR = 0x0011; // Movement Detector
    public static final int FID_DIMMING_ACTUATOR = 0x0012; // Dim actuator
    // public static final int FID_RADIATOR_ACTUATOR = 0x0014; // Radiator
    // public static final int FID_UNDERFLOOR_HEATING = 0x0015; // Underfloor heating
    // public static final int FID_FAN_COIL = 0x0016; // Fan Coil
    // public static final int FID_TWO_LEVEL_CONTROLLER = 0x0017; // Two-level controller
    public static final int FID_DES_DOOR_OPENER_ACTUATOR = 0x001A; // Door opener
    // public static final int FID_PROXY = 0x001B;// Proxy
    // public static final int FID_DES_LEVEL_CALL_ACTUATOR = 0x001D;// Door Entry System Call Level Actuator
    public static final int FID_DES_LEVEL_CALL_SENSOR = 0x001E;// Door Entry System Call Level Sensor
    public static final int FID_DES_DOOR_RINGING_SENSOR = 0x001F;// Door call
    // public static final int FID_DES_AUTOMATIC_DOOR_OPENER_ACTUATOR = 0x0020;// Automatic door opener
    public static final int FID_DES_LIGHT_SWITCH_ACTUATOR = 0x0021;// Corridor light
    // public static final int FID_ROOM_TEMPERATURE_CONTROLLER_MASTER_WITHOUT_FAN = 0x0023;// Room temperature
    // // controller
    // public static final int FID_COOLING_ACTUATOR = 0x0024;// Cooling mode
    // public static final int FID_HEATING_ACTUATOR = 0x0027;// Heating mode
    // public static final int FID_FORCE_UP_DOWN_SENSOR = 0x0028;// Force-position blind
    // public static final int FID_HEATING_COOLING_ACTUATOR = 0x0029;// Auto. heating/cooling mode
    // public static final int FID_HEATING_COOLING_SENSOR = 0x002A;// Switchover heating/cooling
    // public static final int FID_DES_DEVICE_SETTINGS = 0x002B;// Device settings
    // public static final int FID_RGB_W_ACTUATOR = 0x002E;// Dim actuator
    // public static final int FID_RGB_ACTUATOR = 0x002F;// Dim actuator
    // public static final int FID_PANEL_SWITCH_SENSOR = 0x0030;// Control element
    // public static final int FID_PANEL_DIMMING_SENSOR = 0x0031;// Dimming sensor
    // public static final int FID_PANEL_BLIND_SENSOR = 0x0033;// Blind sensor
    // public static final int FID_PANEL_STAIRCASE_LIGHT_SENSOR = 0x0034;// Stairwell light sensor
    // public static final int FID_PANEL_FORCE_ON_OFF_SENSOR = 0x0035;// Force On/Off sensor
    // public static final int FID_PANEL_FORCE_UP_DOWN_SENSOR = 0x0036;// Force-position blind
    // public static final int FID_PANEL_SCENE_SENSOR = 0x0037;// Scene sensor
    // public static final int FID_PANEL_ROOM_TEMPERATURE_CONTROLLER_SLAVE = 0x0038;// Room temperature controller
    // extension unit
    // public static final int FID_PANEL_FAN_COIL_SENSOR = 0x0039;// Fan coil sensor
    // public static final int FID_PANEL_RGB_CT_SENSOR = 0x003A;// RGB + warm white/cold white sensor
    // public static final int FID_PANEL_RGB_SENSOR = 0x003B;// RGB sensor
    // public static final int FID_PANEL_CT_SENSOR = 0x003C;// Warm white/cold white sensor
    // public static final int FID_ADDITIONAL_HEATING_ACTUATOR = 0x003D;// Add. stagepublic static final String For
    // heating mode
    public static final int FID_RADIATOR_ACTUATOR_MASTER = 0x003E;// Radiator thermostate
    // public static final int FID_RADIATOR_ACTUATOR_SLAVE = 0x003F;// Room temperature controller extension unit
    // public static final int FID_BRIGHTNESS_SENSOR = 0x0041;// Brightness sensor
    // public static final int FID_RAIN_SENSOR = 0x0042;// Rain sensor
    // public static final int FID_TEMPERATURE_SENSOR = 0x0043;// Temperature sensor
    // public static final int FID_WIND_SENSOR = 0x0044;// Wind sensor
    public static final int FID_TRIGGER = 0x0045;// Trigger
    // public static final int FID_FCA_2_PIPE_HEATING = 0x0047;// Heating mode
    // public static final int FID_FCA_2_PIPE_COOLING = 0x0048;// Cooling mode
    // public static final int FID_FCA_2_PIPE_HEATING_COOLING = 0x0049;// Auto. heating/cooling mode
    // public static final int FID_FCA_4_PIPE_HEATING_AND_COOLING = 0x004A;// Two valvespublic static final String For
    // heating and cooling
    // public static final int FID_WINDOW_DOOR_ACTUATOR = 0x004B;// Window/Door
    // public static final int FID_INVERTER_INFO = 0x004E;// ABC
    // public static final int FID_METER_INFO = 0x004F;// ABD
    // public static final int FID_BATTERY_INFO = 0x0050;// ACD
    // public static final int FID_PANEL_TIMER_PROGRAM_SWITCH_SENSOR = 0x0051;// Timer program switch sensor
    // public static final int FID_DOMUSTECH_ZONE = 0x0055;// Zone
    // public static final int FID_CENTRAL_HEATING_ACTUATOR = 0x0056;// Central heating actuator
    // public static final int FID_CENTRAL_COOLING_ACTUATOR = 0x0057;// Central cooling actuator
    // public static final int FID_HOUSE_KEEPING = 0x0059;// Housekeeping
    // public static final int FID_MEDIA_PLAYER = 0x005A;// Media Player
    // public static final int FID_PANEL_ROOM_TEMPERATURE_CONTROLLER_SLAVE_FOR_BATTERY_DEVICE = 0x005B;// Panel Room
    // // Temperature
    // // Controller Slavepublic
    // // static final String For
    // // Battery Device
    //
    public static final int FID_PANEL_MUTE_ACTUATOR = 0x005D;// Panel Room
    // public static final int FID_PANEL_MEDIA_PLAYER_SENSOR = 0x0060;// Media Player Sensor
    // public static final int FID_BLIND_ACTUATOR = 0x0061;// Roller blind actuator
    // public static final int FID_ATTIC_WINDOW_ACTUATOR = 0x0062;// Attic window actuator
    // public static final int FID_AWNING_ACTUATOR = 0x0063;// Awning actuator
    public static final int FID_WINDOW_DOOR_POSITION_SENSOR = 0x0064; // WindowDoor Position Sensor
    // public static final int FID_WINDOW_DOOR_POSITION_ACTUATOR = 0x0065;// Window/Door position
    // public static final int FID_MEDIA_PLAYBACK_CONTROL_SENSOR = 0x0066;// Media playback control sensor
    // public static final int FID_MEDIA_VOLUME_SENSOR = 0x0067;// Media volume sensor
    // public static final int FID_DISHWASHER = 0x0068;// Dishwasher
    // public static final int FID_LAUNDRY = 0x0069;// Laundry
    // public static final int FID_DRYER = 0x006A;// Dryer
    // public static final int FID_OVEN = 0x006B;// Oven
    // public static final int FID_FRIDGE = 0x006C;// Fridge
    // public static final int FID_FREEZER = 0x006D;// Freezer
    // public static final int FID_HOOD = 0x006E;// Hood
    // public static final int FID_COFFEE_MACHINE = 0x006F;// Coffee machine
    // public static final int FID_FRIDGE_FREEZER = 0x0070;// Fridge/Freezer
    // public static final int FID_TIMER_PROGRAM_OR_ALERT_SWITCH_SENSOR = 0x0071;// Timer program switch sensor
    // public static final int FID_CEILING_FAN_ACTUATOR = 0x0073;// Ceilingpublic static final String Fan actuator
    // public static final int FID_CEILING_FAN_SENSOR = 0x0074;// Ceilingpublic static final String Fan sensor
    // public static final int FID_SPLIT_UNIT_GATEWAY = 0x0075;// Room temperature controller withpublic static final
    // // String Fan
    // // speed level
    //
    // public static final int FID_ZONE = 0x0076;// Zone
    // public static final int FID_24H_ZONE = 0x0077;// Safety
    // public static final int FID_EXTERNAL_IR_SENSOR_BX80 = 0x0078;// External IR Sensor BX80
    // public static final int FID_EXTERNAL_IR_SENSOR_VXI = 0x0079;// External IR Sensor VXI
    // public static final int FID_EXTERNAL_IR_SENSOR_MINI = 0x007A;// External IR Sensor Mini
    // public static final int FID_EXTERNAL_IR_SENSOR_HIGH_ALTITUDE = 0x007B;// External IR Sensor High Altitude
    // public static final int FID_EXTERNAL_IR_SENSOR_CURTAIN = 0x007C;// External IR Sensor Curtain
    // public static final int FID_SMOKE_DETECTOR = 0x007D;// Smoke Detector
    // public static final int FID_CARBON_MONOXIDE_SENSOR = 0x007E;// Carbon Monoxide Sensor
    // public static final int FID_METHANE_DETECTOR = 0x007F;// Methane Detector
    // public static final int FID_GAS_SENSOR_LPG = 0x0080;// Gas Sensor LPG
    // public static final int FID_FLOOD_DETECTION = 0x0081;// Flood Detection
    // public static final int FID_DOMUS_CENTRAL_UNIT_NEXTGEN = 0x0082;// secure@home Central Unit
    // public static final int FID_THERMOSTAT = 0x0083;// Thermostat
    // public static final int FID_PANEL_DOMUS_ZONE_SENSOR = 0x0084;// secure@home Zone Sensor
    // public static final int FID_THERMOSTAT_SLAVE = 0x0085;// Slave thermostat
    // public static final int FID_DOMUS_SECURE_INTEGRATION = 0x0086;// secure@home Integration Logic
    // public static final int FID_ADDITIONAL_COOLING_ACTUATOR = 0x0087;// Add. stagepublic static final String For
    // // cooling mode
    //
    // public static final int FID_TWO_LEVEL_HEATING_ACTUATOR = 0x0088;// Two Level Heating Actuator
    // public static final int FID_TWO_LEVEL_COOLING_ACTUATOR = 0x0089;// Two Level Cooling Actuator
    // public static final int FID_GLOBAL_ZONE = 0x008E;// Zone
    // public static final int FID_VOLUME_UP_SENSOR = 0x008F;// Volume up
    // public static final int FID_VOLUME_DOWN_SENSOR = 0x0090;// Volume down
    // public static final int FID_PLAY_PAUSE_SENSOR = 0x0091;// Play/pause
    // public static final int FID_NEXT_FAVORITE_SENSOR = 0x0092;// Nextpublic static final String Favorite
    // public static final int FID_NEXT_SONG_SENSOR = 0x0093;// Next song
    // public static final int FID_PREVIOUS_SONG_SENSOR = 0x0094;// Previous song
    // public static final int FID_HOME_APPLIANCE_SENSOR = 0x0095;// Home appliance sensor
    // public static final int FID_HEAT_SENSOR = 0x0096;// Heat sensor
    // public static final int FID_ZONE_SWITCHING = 0x0097;// Zone switching
    // public static final int FID_SECURE_AT_HOME_FUNCTION = 0x0098;// Buttonpublic static final String Function
    // public static final int FID_COMPLEX_CONFIGURATION = 0x0099;// Advanced configuration
    // public static final int FID_DOMUS_CENTRAL_UNIT_BASIC = 0x009A;// secure@home Central Unit Basic
    // public static final int FID_DOMUS_REPEATER = 0x009B;// Repeater
    // public static final int FID_DOMUS_SCENE_TRIGGER = 0x009C;// Remote scene control
    // public static final int FID_DOMUSWINDOWCONTACT = 0x009D;// Window sensor
    // public static final int FID_DOMUSMOVEMENTDETECTOR = 0x009E;// Movement Detector
    // public static final int FID_DOMUSCURTAINDETECTOR = 0x009F;// External IR Sensor Curtain
    // public static final int FID_DOMUSSMOKEDETECTOR = 0x00A0;// Smoke Detector
    // public static final int FID_DOMUSFLOODDETECTOR = 0x00A1;// Flood Detection
    // public static final int FID_PANEL_SUG_SENSOR = 0x00A3;// Sensorpublic static final String For air-conditioning
    // // unit
    // public static final int FID_TWO_LEVEL_HEATING_COOLING_ACTUATOR = 0x00A4;// Two-point controllerpublic static
    // // final
    // // String
    // // For heating or cooling
    //
    // public static final int FID_PANEL_THERMOSTAT_CONTROLLER_SLAVE = 0x00A5;// Slave thermostat
    // public static final int FID_WALLBOX = 0x00A6;// Wallbox
    // public static final int FID_PANEL_WALLBOX = 0x00A7;// Wallbox
    // public static final int FID_DOOR_LOCK_CONTROL = 0x00A8;// Door lock control
    // public static final int FID_VRV_GATEWAY = 0x00AA;// Room temperature controller withpublic static final String
    // // Fan
    // // speed
    // level

    public static final int FID_SCENE_TRIGGER = 0x4800; // Scene trigger
    public static final int FID_RULE_SWITCH = 0x4A00; // Scene trigger

    public static final String CHANNEL_TYPE_UNKNOWN = "Unknown Device";
    public static final String CHANNEL_TYPE_ACTUATOR = "Actuator";
    public static final String CHANNEL_TYPE_THERMOSTAT = "Thermostat";
    public static final String CHANNEL_TYPE_SCENE = "Scene";
    public static final String CHANNEL_TYPE_RULE = "Rule";
    public static final String CHANNEL_TYPE_WINDOWSENSOR = "Window Sensor";
    public static final String CHANNEL_TYPE_WINDOWPOSSENSOR = "Window Position Sensor";
    public static final String CHANNEL_TYPE_TRIGGER = "Trigger";
    public static final String CHANNEL_TYPE_DOOROPENERACTUATOR = "Door Opener Actuator";
    public static final String CHANNEL_TYPE_RINGSENSOR = "Ring Sesnor";
    public static final String CHANNEL_TYPE_CORRIDORLIGHTSWITCH = "Corridor Light Switch";
    public static final String CHANNEL_TYPE_MUTEACTUATOR = "Mute Actor";
    public static final String CHANNEL_TYPE_DIMMINGACTUATOR = "Dimming Actuator";
    public static final String CHANNEL_TYPE_SHUTTERACTUATOR = "Shutter Actuator";

    public static final String DEVICE_INTERFACE_UNKNOWN_TYPE = "unknown";
    public static final String DEVICE_INTERFACE_WIRELESS_TYPE = "wireless";
    public static final String DEVICE_INTERFACE_VIRTUAL_TYPE = "virtual";
    public static final String DEVICE_INTERFACE_WIRED_TYPE = "wired";
    public static final String DEVICE_INTERFACE_HUE_TYPE = "hue";

    public String deviceLabel;
    public String deviceTypeString;
    public String deviceId;
    public String interfaceType;
    public boolean validDevice;

    public @Nullable List<FreeAtHomeDeviceChannel> listOfThings;

    public FreeAtHomeDeviceDescription(JsonObject jsonObject, String id) {

        boolean sceneIsDetected = false;
        boolean ruleIsDetected = false;

        deviceLabel = new String();
        interfaceType = new String();
        deviceTypeString = new String();
        deviceId = id;
        validDevice = false;

        listOfThings = new ArrayList<>();
        if (null == listOfThings) {
            return;
        }

        sceneIsDetected = id.toLowerCase().startsWith("ffff48");
        ruleIsDetected = id.toLowerCase().startsWith("ffff4a");

        jsonObject = jsonObject.getAsJsonObject(id);
        JsonObject jsonObjectOfChannels = jsonObject.getAsJsonObject("channels");

        JsonElement jsonObjectOfInterface = jsonObject.get("interface");
        if (null != jsonObjectOfInterface) {
            String interfaceString = jsonObjectOfInterface.getAsString();

            if (interfaceString.toLowerCase().startsWith("vdev:")) {
                interfaceType = DEVICE_INTERFACE_VIRTUAL_TYPE;
            } else if (interfaceString.toLowerCase().startsWith("hue")) {
                interfaceType = DEVICE_INTERFACE_HUE_TYPE;
            } else if (interfaceString.toLowerCase().startsWith("rf")) {
                interfaceType = DEVICE_INTERFACE_WIRELESS_TYPE;
            } else if (interfaceString.toLowerCase().startsWith("tp")) {
                interfaceType = DEVICE_INTERFACE_WIRED_TYPE;
            } else {
                interfaceType = DEVICE_INTERFACE_UNKNOWN_TYPE;
            }
        } else {
            interfaceType = DEVICE_INTERFACE_UNKNOWN_TYPE;
        }

        if (null != jsonObjectOfChannels) {
            Set<String> keys = jsonObjectOfChannels.keySet();

            Iterator iter = keys.iterator();

            // Scan channels for functions
            while (iter.hasNext()) {
                String nextChannel = (String) iter.next();

                JsonObject channelObject = jsonObjectOfChannels.getAsJsonObject(nextChannel);

                this.deviceLabel = channelObject.get("displayName").getAsString();

                String channelFunctionID = channelObject.get("functionID").getAsString();

                if (true == sceneIsDetected) {
                    channelFunctionID = channelFunctionID.substring(0, channelFunctionID.length() - 1) + "0";
                }

                if (false == channelFunctionID.isEmpty()) {

                    FreeAtHomeDeviceChannel newChannel = new FreeAtHomeDeviceChannel();

                    switch (getIntegerFromHex(channelFunctionID)) {

                        // increment trigger channel - this handled identically as actuator
                        case FID_TRIGGER:
                        case FID_SWITCH_ACTUATOR: {
                            newChannel.channelId = nextChannel;
                            newChannel.thingTypeOfChannel = FreeAtHomeSystemBindingConstants.ACTUATOR_TYPE_ID;
                            deviceTypeString = CHANNEL_TYPE_ACTUATOR;
                            validDevice = true;

                            listOfThings.add(newChannel);

                            break;
                        }
                        // increment thermostat channels
                        case FID_RADIATOR_ACTUATOR_MASTER: {
                            newChannel.channelId = nextChannel;
                            newChannel.thingTypeOfChannel = FreeAtHomeSystemBindingConstants.THERMOSTAT_TYPE_ID;
                            deviceTypeString = CHANNEL_TYPE_THERMOSTAT;
                            validDevice = true;

                            listOfThings.add(newChannel);

                            break;
                        }
                        // increment window sensor channels
                        case FID_WINDOW_DOOR_SENSOR: {
                            deviceTypeString = CHANNEL_TYPE_WINDOWSENSOR;
                            newChannel.channelId = nextChannel;
                            newChannel.thingTypeOfChannel = FreeAtHomeSystemBindingConstants.WINDOWSENSOR_TYPE_ID;
                            validDevice = true;

                            listOfThings.add(newChannel);
                            break;
                        }

                        case FID_WINDOW_DOOR_POSITION_SENSOR: {
                            newChannel.channelId = nextChannel;
                            newChannel.thingTypeOfChannel = FreeAtHomeSystemBindingConstants.WINDOWSENSOR_TYPE_ID;
                            deviceTypeString = CHANNEL_TYPE_WINDOWPOSSENSOR;

                            listOfThings.add(newChannel);
                            break;
                        }

                        case FID_SCENE_TRIGGER: {
                            newChannel.channelId = nextChannel;
                            newChannel.thingTypeOfChannel = FreeAtHomeSystemBindingConstants.SCENE_TYPE_ID;
                            deviceTypeString = CHANNEL_TYPE_SCENE;

                            validDevice = true;

                            listOfThings.add(newChannel);
                            break;
                        }

                        case FID_RULE_SWITCH: {
                            newChannel.channelId = nextChannel;
                            newChannel.thingTypeOfChannel = FreeAtHomeSystemBindingConstants.RULE_TYPE_ID;
                            deviceTypeString = CHANNEL_TYPE_RULE;

                            validDevice = true;

                            listOfThings.add(newChannel);
                            break;
                        }

                        case FID_DES_DOOR_OPENER_ACTUATOR: {
                            newChannel.channelId = nextChannel;
                            newChannel.thingTypeOfChannel = FreeAtHomeSystemBindingConstants.DOOROPENER_TYPE_ID;
                            deviceTypeString = CHANNEL_TYPE_DOOROPENERACTUATOR;

                            validDevice = true;

                            listOfThings.add(newChannel);
                            break;
                        }

                        case FID_DES_LEVEL_CALL_SENSOR:
                        case FID_DES_DOOR_RINGING_SENSOR: {
                            newChannel.channelId = nextChannel;
                            newChannel.thingTypeOfChannel = FreeAtHomeSystemBindingConstants.DOORRINGSENSOR_TYPE_ID;
                            deviceTypeString = CHANNEL_TYPE_RINGSENSOR;

                            validDevice = true;

                            listOfThings.add(newChannel);
                            break;
                        }

                        case FID_DES_LIGHT_SWITCH_ACTUATOR: {
                            newChannel.channelId = nextChannel;
                            newChannel.thingTypeOfChannel = FreeAtHomeSystemBindingConstants.CORRIDORLIGHTSWITCH_TYPE_ID;
                            deviceTypeString = CHANNEL_TYPE_CORRIDORLIGHTSWITCH;

                            validDevice = true;

                            listOfThings.add(newChannel);
                            break;
                        }

                        case FID_PANEL_MUTE_ACTUATOR: {
                            newChannel.channelId = nextChannel;
                            newChannel.thingTypeOfChannel = FreeAtHomeSystemBindingConstants.MUTEACTUATOR_TYPE_ID;
                            deviceTypeString = CHANNEL_TYPE_MUTEACTUATOR;

                            validDevice = true;

                            listOfThings.add(newChannel);
                            break;
                        }

                        case FID_DIMMING_ACTUATOR: {
                            newChannel.channelId = nextChannel;
                            newChannel.thingTypeOfChannel = FreeAtHomeSystemBindingConstants.DIMMINGACTUATOR_TYPE_ID;
                            deviceTypeString = CHANNEL_TYPE_DIMMINGACTUATOR;

                            validDevice = true;

                            listOfThings.add(newChannel);
                            break;
                        }
                        case FID_SHUTTER_ACTUATOR: {
                            newChannel.channelId = nextChannel;
                            newChannel.thingTypeOfChannel = FreeAtHomeSystemBindingConstants.SHUTTERACTUATOR_TYPE_ID;
                            deviceTypeString = CHANNEL_TYPE_SHUTTERACTUATOR;

                            validDevice = true;

                            listOfThings.add(newChannel);
                            break;
                        }

                        default: {
                            newChannel.channelId = nextChannel;
                            newChannel.thingTypeOfChannel = FreeAtHomeSystemBindingConstants.UNKNOWN_TYPE_ID;

                            // listOfThings.add(newChannel);
                            break;
                        }
                    }
                }
            }
        }
    }

    public int numberOfThings() {
        return listOfThings.size();
    }

    public int getIntegerFromHex(String strHexValue) {
        String digits = "0123456789ABCDEF";
        String str = strHexValue.toUpperCase();
        int hexval = 0;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            int d = digits.indexOf(c);
            hexval = 16 * hexval + d;
        }

        return hexval;
    }
}
