package com.konka.konkaim.bean;

/**
 * Created by HP on 2018-5-30.
 */

public class RoomRequestMessage {
    String roomName;
    String type;

    public RoomRequestMessage(String roomName, String type){
        this.roomName = roomName;
        this.type = type;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

}
